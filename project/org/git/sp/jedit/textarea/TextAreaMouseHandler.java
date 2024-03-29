/*
 * TextAreaMouseHandler.java - standalone mouse handler for textarea
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2006 Matthieu Casanova
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.gjt.sp.jedit.textarea;

import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.TextUtilities;
import org.gjt.sp.util.StandardUtilities;
import org.gjt.sp.jedit.Registers;

import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;
import static java.awt.event.InputEvent.*;
import static java.awt.event.MouseEvent.BUTTON1;
import static java.awt.event.MouseEvent.BUTTON2;
import static java.awt.event.MouseEvent.BUTTON3;

import java.awt.*;

/** Standalone TextArea MouseHandler.
 *
 * @author Matthieu Casanova
 * @version $Id: TextAreaMouseHandler.java 24938 2019-08-18 15:30:20Z vampire0 $
 */
public class TextAreaMouseHandler extends MouseInputAdapter
{
	//{{{ MouseHandler constructor
	TextAreaMouseHandler(TextArea textArea)
	{
		this.textArea = textArea;
	} //}}}

	//{{{ mousePressed() method
	@Override
	public void mousePressed(MouseEvent evt)
	{
		showCursor();

		int btn = evt.getButton();
		if (btn != BUTTON1 && btn != BUTTON2 && btn != BUTTON3)
		{
			// Suppress presses with unknown button, to avoid
			// problems due to horizontal scrolling.
			return;
		}

		control = (OperatingSystem.isMacOS() && evt.isMetaDown())
			|| (!OperatingSystem.isMacOS() && evt.isControlDown());

		ctrlForRectangularSelection = textArea.isCtrlForRectangularSelection();

		// so that Home <mouse click> Home is not the same
		// as pressing Home twice in a row
		textArea.getInputHandler().resetLastActionCount();

		quickCopyDrag = (textArea.isQuickCopyEnabled() &&
			isMiddleButton(evt));

		if(!quickCopyDrag)
		{
			textArea.requestFocus();
			TextArea.focusedComponent = textArea;
		}

		if(textArea.getBuffer().isLoading())
			return;

		int x = evt.getX();
		int y = evt.getY();

		dragStart = textArea.xyToOffset(x,y,
			!(textArea.getPainter().isBlockCaretEnabled()
			|| textArea.isOverwriteEnabled()));
		dragStartLine = textArea.getLineOfOffset(dragStart);
		dragStartOffset = dragStart - textArea.getLineStartOffset(
			dragStartLine);

		if(isPopupTrigger(evt)
			&& textArea.getRightClickPopup() != null)
		{
			if(textArea.isRightClickPopupEnabled())
				textArea.handlePopupTrigger(evt);
			return;
		}

		dragged = false;

		textArea.blink = true;
		textArea.invalidateLine(textArea.getCaretLine());

		clickCount = evt.getClickCount();

		if(textArea.isDragEnabled()
			&& textArea.selectionManager.insideSelection(x,y)
			&& clickCount == 1 && !evt.isShiftDown())
		{
			maybeDragAndDrop = true;
			textArea.moveCaretPosition(dragStart,false);
			return;
		}
		maybeDragAndDrop = false;

		if(quickCopyDrag)
		{
			// ignore double clicks of middle button
			doSingleClick(evt);
		}
		else
		{
			switch(clickCount)
			{
			case 1:
				doSingleClick(evt);
				break;
			case 2:
				doDoubleClick();
				break;
			default: //case 3:
				doTripleClick();
				break;
			}
		}
	} //}}}

	//{{{ doSingleClick() method
	protected void doSingleClick(MouseEvent evt)
	{
		int x = evt.getX();

		int extraEndVirt = 0;
		if(textArea.chunkCache.getLineInfo(textArea.getLastScreenLine()).lastSubregion)
		{
			int dragStart = textArea.xyToOffset(x,evt.getY(),
				!textArea.getPainter().isBlockCaretEnabled()
				&& !textArea.isOverwriteEnabled());
			int screenLine = textArea.getScreenLineOfOffset(dragStart);
			ChunkCache.LineInfo lineInfo = textArea.chunkCache.getLineInfo(screenLine);
			int offset = textArea.getScreenLineEndOffset(screenLine);
			if ((1 != offset - dragStart) || (lineInfo.lastSubregion))
			{
				offset--;
			}
			float dragStartLineWidth = textArea.offsetToXY(offset).x;
			if(x > dragStartLineWidth)
			{
				extraEndVirt = (int)(
					(x - dragStartLineWidth)
					/ textArea.charWidth);
				if(!textArea.getPainter().isBlockCaretEnabled()
					&& !textArea.isOverwriteEnabled()
					&& (x - textArea.getHorizontalOffset())
					% textArea.charWidth > textArea.charWidth / 2)
				{
					extraEndVirt++;
				}
			}
		}

		if(((control && ctrlForRectangularSelection) ||
		    textArea.isRectangularSelectionEnabled())
			&& textArea.isEditable())
		{
			int screenLine = (evt.getY() / textArea.getPainter().getLineHeight());
			if(screenLine > textArea.getLastScreenLine())
				screenLine = textArea.getLastScreenLine();
			ChunkCache.LineInfo info = textArea.chunkCache.getLineInfo(screenLine);
			if(info.lastSubregion && extraEndVirt != 0)
			{
				// control-click in virtual space inserts
				// whitespace and moves caret
				String whitespace = StandardUtilities
					.createWhiteSpace(extraEndVirt,0);
				textArea.getBuffer().insert(dragStart,whitespace);

				dragStart += whitespace.length();
			}
		}

		if(evt.isShiftDown())
		{
			textArea.resizeSelection(
				getSelectionPivotCaret(),dragStart,extraEndVirt,
				textArea.isRectangularSelectionEnabled()
				|| (control && ctrlForRectangularSelection));

			if(!quickCopyDrag)
				textArea.moveCaretPosition(dragStart,false);

			// so that shift-click-drag works
			dragStartLine = getSelectionPivotLine();
			dragStart = getSelectionPivotCaret();
			dragStartOffset = dragStart
				- textArea.getLineStartOffset(dragStartLine);

			// so that quick copy works
			dragged = true;

			return;
		}

		if(!quickCopyDrag)
		{
			Point p = textArea.offsetToXY(dragStart);
			// defer scrolling until mouserelease if result is off-screen
			textArea.moveCaretPosition(dragStart, (p.x < 0) ? TextArea.NO_SCROLL : TextArea.NORMAL_SCROLL);
		}

		if(!(textArea.isMultipleSelectionEnabled()
			|| quickCopyDrag))
			textArea.selectNone();
	} //}}}

	//{{{ doDoubleClick() method
	protected void doDoubleClick()
	{
		// Ignore empty lines
		if(textArea.getLineLength(dragStartLine) == 0)
			return;

		String lineText = textArea.getLineText(dragStartLine);
		String noWordSep = textArea.getBuffer()
			.getStringProperty("noWordSep");
		if(dragStartOffset == textArea.getLineLength(dragStartLine))
			dragStartOffset--;

		boolean joinNonWordChars = textArea.getJoinNonWordChars();
		int wordStart = TextUtilities.findWordStart(lineText,dragStartOffset,
			noWordSep,joinNonWordChars,false,false);
		int wordEnd = TextUtilities.findWordEnd(lineText,
			dragStartOffset+1,noWordSep,
			joinNonWordChars,false,false);

		int lineStart = textArea.getLineStartOffset(dragStartLine);
		Selection sel = new Selection.Range(
			lineStart + wordStart,
			lineStart + wordEnd);
		if(textArea.isMultipleSelectionEnabled())
			textArea.addToSelection(sel);
		else
			textArea.setSelection(sel);

		if(quickCopyDrag)
			quickCopyDrag = false;

		textArea.moveCaretPosition(lineStart + wordEnd,false);

		dragged = true;
	} //}}}

	//{{{ doTripleClick() method
	protected void doTripleClick()
	{
		int newCaret = textArea.getLineEndOffset(dragStartLine);
		if(dragStartLine == textArea.getLineCount() - 1)
			newCaret--;

		Selection sel = new Selection.Range(
			textArea.getLineStartOffset(dragStartLine),
			newCaret);
		if(textArea.isMultipleSelectionEnabled())
			textArea.addToSelection(sel);
		else
			textArea.setSelection(sel);

		if(quickCopyDrag)
			quickCopyDrag = false;

		textArea.moveCaretPosition(newCaret,false);

		dragged = true;
	} //}}}

	//{{{ mouseMoved() method
	@Override
	public void mouseMoved(MouseEvent evt)
	{
		showCursor();
	} //}}}

	//{{{ mouseDragged() method
	@Override
	public void mouseDragged(MouseEvent evt)
	{
		if (isPopupTrigger(evt))
			return;

		if(maybeDragAndDrop)
		{
			textArea.startDragAndDrop(evt,control);
			return;
		}

		if(textArea.getBuffer().isLoading())
			return;

		TextAreaPainter painter = textArea.getPainter();
		if(evt.getY() < 0)
		{
			int delta = Math.min(-1,evt.getY() / painter.getLineHeight());
			textArea.setFirstLine(textArea.getFirstLine() + delta);
		}
		else if(evt.getY() >= painter.getHeight())
		{
			int delta = Math.max(1,(evt.getY() - painter.getHeight()) / painter.getLineHeight());
			if(textArea.lastLinePartial)
				delta--;
			textArea.setFirstLine(textArea.getFirstLine() + delta);
		}

		switch(clickCount)
		{
		case 1:
			doSingleDrag(evt);
			break;
		case 2:
			doDoubleDrag(evt);
			break;
		default: //case 3:
			doTripleDrag(evt);
			break;
		}
	} //}}}

	//{{{ doSingleDrag() method
	private void doSingleDrag(MouseEvent evt)
	{
		dragged = true;

		TextAreaPainter painter = textArea.getPainter();

		int x = evt.getX();
		int y = evt.getY();
		if(y < 0)
			y = 0;
		else if(y >= painter.getHeight())
			y = painter.getHeight() - 1;

		int dot = textArea.xyToOffset(x,y,
			(!painter.isBlockCaretEnabled()
			&& !textArea.isOverwriteEnabled())
			|| quickCopyDrag);
		int dotLine = textArea.getLineOfOffset(dot);
		int extraEndVirt = 0;

		if(textArea.chunkCache.getLineInfo(	textArea.getLastScreenLine()).lastSubregion)
		{
			int screenLine = textArea.getScreenLineOfOffset(dot);
			ChunkCache.LineInfo lineInfo = textArea.chunkCache.getLineInfo(screenLine);
			int offset = textArea.getScreenLineEndOffset(screenLine);
			if ((1 != offset - dot) || (lineInfo.lastSubregion))
			{
				offset--;
			}
			float dotLineWidth = textArea.offsetToXY(offset).x;
			if(x > dotLineWidth)
			{
				extraEndVirt = (int)((x - dotLineWidth) / textArea.charWidth);
				if(!painter.isBlockCaretEnabled()
					&& !textArea.isOverwriteEnabled()
					&& (x - textArea.getHorizontalOffset()) % textArea.charWidth > textArea.charWidth / 2)
					extraEndVirt++;
			}
		}

		textArea.resizeSelection(dragStart,dot,extraEndVirt,
			textArea.isRectangularSelectionEnabled()
			|| (control && ctrlForRectangularSelection));

		if(quickCopyDrag)
		{
			// just scroll to the dragged location
			textArea.scrollTo(dotLine,dot - textArea.getLineStartOffset(dotLine),false);
		}
		else
		{
			Point p = textArea.offsetToXY(dot);
			if(dot != textArea.getCaretPosition())
			{
				// defer scroll to mouserelease if result is offscreen left without dragging that direction
				textArea.moveCaretPosition(dot, (p.x < 0 && x > 1) ? TextArea.NO_SCROLL : TextArea.NORMAL_SCROLL);
			}
			else if(p.x < 0 && x < 1)
			{
				// caret already offscreen left, user now attempting to drag left
				textArea.scrollToCaret(false);
			}
			if(textArea.isRectangularSelectionEnabled()
				&& extraEndVirt != 0)
			{
				textArea.scrollTo(dotLine,dot - textArea.getLineStartOffset(dotLine)
					+ extraEndVirt,false);
			}
		}
	} //}}}

	//{{{ doDoubleDrag() method
	private void doDoubleDrag(MouseEvent evt)
	{
		int markLineStart = textArea.getLineStartOffset(dragStartLine);
		int markLineLength = textArea.getLineLength(dragStartLine);
		int mark = dragStartOffset;

		TextAreaPainter painter = textArea.getPainter();

		int pos = textArea.xyToOffset(evt.getX(),
			Math.max(0,Math.min(painter.getHeight(),evt.getY())),
			!(painter.isBlockCaretEnabled()
			|| textArea.isOverwriteEnabled()));
		int line = textArea.getLineOfOffset(pos);
		int lineStart = textArea.getLineStartOffset(line);
		int lineLength = textArea.getLineLength(line);
		int offset = pos - lineStart;

		String lineText = textArea.getLineText(line);
		String markLineText = textArea.getLineText(dragStartLine);
		String noWordSep = textArea.getBuffer()
			.getStringProperty("noWordSep");
		boolean joinNonWordChars = textArea.getJoinNonWordChars();

		if(markLineStart + dragStartOffset > lineStart + offset)
		{
			if(offset != 0 && offset != lineLength)
			{
				offset = TextUtilities.findWordStart(
					lineText,offset,noWordSep,
					joinNonWordChars);
			}

			if(markLineLength != 0)
			{
				mark = TextUtilities.findWordEnd(
					markLineText,mark,noWordSep,
					joinNonWordChars);
			}
		}
		else
		{
			if(offset != 0 && lineLength != 0)
			{
				offset = TextUtilities.findWordEnd(
					lineText,offset,noWordSep,
					joinNonWordChars);
			}

			if(mark != 0 && mark != markLineLength)
			{
				mark = TextUtilities.findWordStart(
					markLineText,mark,noWordSep,
					joinNonWordChars);
			}
		}

		if(lineStart + offset == textArea.getCaretPosition())
			return;

		textArea.resizeSelection(markLineStart + mark,
			lineStart + offset,0,false);
		textArea.moveCaretPosition(lineStart + offset,false);

		dragged = true;
	} //}}}

	//{{{ doTripleDrag() method
	private void doTripleDrag(MouseEvent evt)
	{
		TextAreaPainter painter = textArea.getPainter();

		int offset = textArea.xyToOffset(evt.getX(),
			Math.max(0,Math.min(painter.getHeight(),evt.getY())),
			false);
		int mouseLine = textArea.getLineOfOffset(offset);
		int mark;
		int mouse;
		if(dragStartLine > mouseLine)
		{
			mark = textArea.getLineEndOffset(dragStartLine) - 1;
			if(offset == textArea.getLineEndOffset(mouseLine) - 1)
				mouse = offset;
			else
				mouse = textArea.getLineStartOffset(mouseLine);
		}
		else
		{
			mark = textArea.getLineStartOffset(dragStartLine);
			if(offset == textArea.getLineStartOffset(mouseLine))
				mouse = offset;
			else if(offset == textArea.getLineEndOffset(mouseLine) - 1
				&& mouseLine != textArea.getLineCount() - 1)
				mouse = textArea.getLineEndOffset(mouseLine);
			else
				mouse = textArea.getLineEndOffset(mouseLine) - 1;
		}

		mouse = Math.min(textArea.getBuffer().getLength(),mouse);

		if(mouse == textArea.getCaretPosition())
			return;

		textArea.resizeSelection(mark,mouse,0,false);
		textArea.moveCaretPosition(mouse,false);

		dragged = true;
	} //}}}

	//{{{ mouseReleased() method
	@Override
	public void mouseReleased(MouseEvent evt)
	{
		int btn = evt.getButton();
		if (btn != BUTTON1 && btn != BUTTON2 && btn != BUTTON3)
		{
			// Suppress releases with unknown button, to avoid
			// problems due to horizontal scrolling.
			return;
		}

		// middle mouse button drag inserts selection
		// at caret position
		Selection sel = textArea.getSelectionAtOffset(dragStart);
		if(dragged && sel != null)
		{
			Registers.setRegister('%',textArea.getSelectedText(sel));
			if(quickCopyDrag)
			{
				textArea.removeFromSelection(sel);
				Registers.paste(TextArea.focusedComponent,
					'%',sel instanceof Selection.Rect);

				TextArea.focusedComponent.requestFocus();
			}
		}
		else if(!dragged && textArea.isQuickCopyEnabled() &&
			isMiddleButton(evt))
		{
			textArea.requestFocus();
			TextArea.focusedComponent = textArea;

			textArea.setCaretPosition(dragStart,false);
			if(!textArea.isEditable())
				UIManager.getLookAndFeel().provideErrorFeedback(textArea);
			else
				Registers.paste(textArea,'%',control);
		}
		else if(maybeDragAndDrop
			&& !textArea.isMultipleSelectionEnabled())
		{
			textArea.selectNone();
		}

		maybeDragAndDrop = false;
		dragged = false;
		if(!(textArea.isRectangularSelectionEnabled()
			|| (control && ctrlForRectangularSelection)))
			// avoid scrolling away from rectangular selection
			textArea.scrollToCaret(false);
	} //}}}

	//{{{ isPopupTrigger() method
	/**
	 * Returns if the specified event is the popup trigger event.
	 * This implements precisely defined behavior, as opposed to
	 * MouseEvent.isPopupTrigger().
	 * @param evt The event
	 * @since jEdit 4.3pre7
	 */
	public static boolean isPopupTrigger(MouseEvent evt)
	{
		return isRightButton(evt);
	} //}}}

	//{{{ isLeftButton() method
	/**
	 * @param evt A mouse event
	 * @return true if the mouse event is due to the left button
	 * @since jEdit 5.6
	 */
	public static boolean isLeftButton(MouseEvent evt)
	{
		return evt.getButton() == BUTTON1;
	} //}}}

	//{{{ isMiddleButton() method
	/**
	 * @param modifiers The modifiers flag from a mouse event
	 * @return true if the modifier match the middle button
	 * @since jEdit 4.3pre7
	 * @deprecated use {@link #isMiddleButton(MouseEvent)}
	 */
	@Deprecated
	public static boolean isMiddleButton(int modifiers)
	{
		if (OperatingSystem.isMacOS())
		{
			if((modifiers & BUTTON1_MASK) == BUTTON1_MASK)
				return (modifiers & ALT_MASK) == ALT_MASK;
			else
				return (modifiers & BUTTON2_MASK) == BUTTON2_MASK;
		}
		else
			return (modifiers & BUTTON2_MASK) == BUTTON2_MASK;
	}

	/**
	 * @param evt A mouse event
	 * @return true if the mouse event is due to the middle button
	 * @since jEdit 5.6
	 */
	public static boolean isMiddleButton(MouseEvent evt)
	{
		if (OperatingSystem.isMacOS())
		{
			if(evt.getButton() == BUTTON1)
				return (evt.getModifiersEx() & ALT_DOWN_MASK) == ALT_DOWN_MASK;
			else
				return evt.getButton() == BUTTON2;
		}
		else
			return evt.getButton() == BUTTON2;
	} //}}}

	//{{{ isRightButton() method
	/**
	 * @param modifiers The modifiers flag from a mouse event
	 * @return true if the modifier match the right button
	 * @since jEdit 4.3pre7
	 * @deprecated use {@link #isRightButton(MouseEvent)}
	 */
	@Deprecated
	public static boolean isRightButton(int modifiers)
	{
		if (OperatingSystem.isMacOS())
		{
			if((modifiers & BUTTON1_MASK) == BUTTON1_MASK)
				return (modifiers & CTRL_MASK) == CTRL_MASK;
			else
				return (modifiers & BUTTON3_MASK) == BUTTON3_MASK;
		}
		else
			return (modifiers & BUTTON3_MASK) == BUTTON3_MASK;
	}

	/**
	 * @param evt A mouse event
	 * @return true if the mouse event is due to the right button
	 * @since jEdit 5.6
	 */
	public static boolean isRightButton(MouseEvent evt)
	{
		if (OperatingSystem.isMacOS())
		{
			if(evt.getButton() == BUTTON1)
				return (evt.getModifiersEx() & CTRL_DOWN_MASK) == CTRL_DOWN_MASK;
			else
				return evt.getButton() == BUTTON3;
		}
		else
			return evt.getButton() == BUTTON3;
	} //}}}

	//{{{ Private methods

	//{{{ getSelectionPivotCaret() method
	/*
	 * Dynamically get the "pivot" point associated with a current
	 * selection.  See inline comments for details.
	 */
	private int getSelectionPivotCaret()
	{
		int caret = textArea.caret;

		Selection s = textArea.getSelectionAtOffset(textArea.caret);
		if (s == null)
			return caret;

		// The mental model: an existing selection, and then a shift+click
		// somewhere else.  What happens to the selection?  Because a selection
		// exists, we need a "pivot" point.  If the caret is at the start of a
		// selection, the end of the selection pivot point.  So, a click above
		// the start of the caret will enlarge the selection, and a click below
		// the end will reverse the selection around the pivot point: the text
		// before the pivot will no longer be selected, and the text after it
		// and up to the click will be newly selected.  Vice versa holds true
		// when the caret is at the end of the selection.  If the caret is
		// somewhere else, just give up, and let the user fix it.

		caret = ( caret == s.start ? s.end   :
		          caret == s.end   ? s.start :
		          caret );

		return caret;
	} //}}}

	//{{{ getSelectionPivotLine() method
	/*
	 * See getSelectionPivotCaret for an explanation of this function
	 */
	private int getSelectionPivotLine()
	{
		int c  = textArea.caret;
		int cl = textArea.caretLine;

		if(textArea.getSelectionCount() != 1)
			return cl;

		Selection s = textArea.getSelection(0);
		cl = ( c == s.start ? s.endLine   :
		       c == s.end   ? s.startLine :
		       cl );

		return cl;
	} //}}}
	//}}}

	//{{{ Private members
	protected final TextArea textArea;
	protected int dragStartLine;
	protected int dragStartOffset;
	protected int dragStart;
	protected int clickCount;
	protected boolean dragged;
	protected boolean quickCopyDrag;
	protected boolean control;
	protected boolean ctrlForRectangularSelection;
	/* with drag and drop on, a mouse down in a selection does not
	immediately deselect */
	protected boolean maybeDragAndDrop;

	//{{{ showCursor() method
	protected void showCursor()
	{
		textArea.getPainter().showCursor();
	} //}}}

	//}}}
}
