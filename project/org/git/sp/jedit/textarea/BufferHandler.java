/*
 * BufferHandler.java
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2005 Slava Pestov
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

import java.util.Iterator;
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.Debug;

/**
 * Note that in this class we take great care to defer complicated
 * calculations to the end of the current transaction if the buffer
 * informs us a compound edit is in progress
 * (<code>isTransactionInProgress()</code>).
 *
 * This greatly speeds up replace all for example, by only doing certain
 * things once, particularly in <code>moveCaretPosition()</code>.
 *
 * Try doing a replace all in a large file, for example. It is very slow
 * in 3.2, faster in 4.0 (where the transaction optimization was
 * introduced) and faster still in 4.1 (where it was further improved).
 *
 * There is still work to do; see TODO.txt.
 */
class BufferHandler implements BufferListener
{
	private final DisplayManager displayManager;
	private final TextArea textArea;
	private final JEditBuffer buffer;
	private boolean delayedUpdate;
	private boolean delayedMultilineUpdate;
	private int delayedUpdateStart;
	private int delayedUpdateEnd;

	//{{{ BufferChangeHandler constructor
	BufferHandler(DisplayManager displayManager,
		TextArea textArea,
		JEditBuffer buffer)
	{
		this.displayManager = displayManager;
		this.textArea = textArea;
		this.buffer = buffer;
	} //}}}

	//{{{ bufferLoaded() method
	@Override
	public void bufferLoaded(JEditBuffer buffer)
	{
		displayManager.bufferLoaded();
		textArea._finishCaretUpdate();
	} //}}}

	//{{{ foldHandlerChanged() method
	@Override
	public void foldHandlerChanged(JEditBuffer buffer)
	{
		displayManager.foldHandlerChanged();
	} //}}}

	//{{{ foldLevelChanged() method
	@Override
	public void foldLevelChanged(JEditBuffer buffer, int start, int end)
	{
		//System.err.println("foldLevelChanged " + (start-1) + " to " + textArea.getLastPhysicalLine() + "," + end);

		if(textArea.getDisplayManager() == displayManager
			&& end != 0 && !buffer.isLoading())
		{
			textArea.invalidateLineRange(start - 1,
				textArea.getLastPhysicalLine());
		}
	} //}}}

	//{{{ contentInserted() method
	@Override
	public void contentInserted(JEditBuffer buffer, int startLine, int offset, int numLines, int length)
	{
		if(buffer.isLoading())
			return;

		displayManager.screenLineMgr.contentInserted(startLine,numLines);

		int endLine = startLine + numLines;

		if(numLines != 0)
			delayedMultilineUpdate = true;

		displayManager.folds.contentInserted(startLine,numLines);

		FirstLine firstLine = displayManager.firstLine;
		ScrollLineCount scrollLineCount = displayManager.scrollLineCount;

		if(textArea.getDisplayManager() == displayManager)
		{
			firstLine.contentInserted(startLine,numLines);
			scrollLineCount.contentInserted(startLine,numLines);

			if(delayedUpdateEnd >= startLine)
				delayedUpdateEnd += numLines;
			delayUpdate(startLine,endLine);

			//{{{ resize selections if necessary
			
			Iterator<Selection> iter = textArea.getSelectionIterator();
			while(iter.hasNext())
			{
				Selection s = iter.next();

				if(s.contentInserted(buffer,startLine,offset,
					numLines,length))
				{
					delayUpdate(s.startLine,s.endLine);
				}
			} //}}}

			int caret = textArea.getCaretPosition();
			if(caret >= offset)
			{
				int scrollMode = textArea.caretAutoScroll()
					? TextArea.ELECTRIC_SCROLL
					: TextArea.NO_SCROLL;
				textArea.moveCaretPosition(
					caret + length,scrollMode);
			}
			else
			{
				int scrollMode = textArea.caretAutoScroll()
					? TextArea.NORMAL_SCROLL
					: TextArea.NO_SCROLL;
				textArea.moveCaretPosition(
					caret,scrollMode);
			}
		}
		else
		{
			firstLine.setCallReset(true);
			scrollLineCount.setCallReset(true);
		}
	} //}}}

	//{{{ preContentInserted() method
	/**
	 * Called when text is about to be inserted in the buffer.
	 * @param buffer The buffer in question
	 * @param startLine The first line
	 * @param offset The start offset, from the beginning of the buffer
	 * @param numLines The number of lines inserted
	 * @param length The number of characters inserted
	 * @since jEdit 4.3pre11
	 */
	@Override
	public void preContentInserted(JEditBuffer buffer, int startLine, int offset, int numLines, int length)
	{
		if(buffer.isLoading())
			return;

		if(textArea.getDisplayManager() == displayManager)
		{
			getReadyToBreakFold(startLine);
			displayManager.firstLine.preContentInserted(startLine, numLines);
			displayManager.scrollLineCount.preContentInserted(startLine, numLines);
		}
	} //}}}

	//{{{ preContentRemoved() method
	/**
	 * Called when text is about to be removed from the buffer, but is
	 * still present.
	 * @param buffer The buffer in question
	 * @param startLine The first line
	 * @param offset The start offset, from the beginning of the buffer
	 * @param numLines The number of lines to be removed
	 * @param length The number of characters to be removed
	 * @since jEdit 4.3pre3
	 */
	@Override
	public void preContentRemoved(JEditBuffer buffer, int startLine, int offset, int numLines, int length)
	{
		if(buffer.isLoading())
			return;

		FirstLine firstLine = displayManager.firstLine;
		ScrollLineCount scrollLineCount = displayManager.scrollLineCount;

		if(textArea.getDisplayManager() == displayManager)
		{
			if(numLines == 0)
			{
				getReadyToBreakFold(startLine);
			}
			else
			{
				int lastLine = startLine + numLines;
				if( !displayManager.isLineVisible(startLine)
				 || !displayManager.isLineVisible(lastLine)
			 	 || offset != buffer.getLineStartOffset(startLine)
				 || offset + length != buffer.getLineStartOffset(lastLine))
				{
					getReadyToBreakFold(startLine);
					getReadyToBreakFold(lastLine);
				}
				else
				{
					// The removal will not touch
					// inside of folds and will not
					// modify any remaining lines.
				}
			}

			firstLine.preContentRemoved(startLine,offset, numLines);
			scrollLineCount.preContentRemoved(startLine, offset, numLines);

			if(delayedUpdateEnd >= startLine)
				delayedUpdateEnd -= numLines;
			delayUpdate(startLine,startLine);
		}
		else
		{
			firstLine.setCallReset(true);
			scrollLineCount.setCallReset(true);
		}

		displayManager.screenLineMgr.contentRemoved(startLine,numLines);

		if(numLines == 0)
			return;

		delayedMultilineUpdate = true;

		if(displayManager.folds.preContentRemoved(startLine,numLines))
		{
			displayManager.folds.reset(buffer.getLineCount());
			firstLine.setCallReset(true);
			scrollLineCount.setCallReset(true);
		}

		if(firstLine.getPhysicalLine() > displayManager.getLastVisibleLine() ||
		   firstLine.getPhysicalLine() < displayManager.getFirstVisibleLine() )
		{
			// will be handled later.
			// see comments at the end of
			// transactionComplete().
		}
	} //}}}

	//{{{ contentRemoved() method
	@Override
	public void contentRemoved(JEditBuffer buffer, int startLine, int start, int numLines, int length)
	{
		if(buffer.isLoading())
			return;

		FirstLine firstLine = displayManager.firstLine;
		ScrollLineCount scrollLineCount = displayManager.scrollLineCount;

		if(textArea.getDisplayManager() == displayManager)
		{
			firstLine.contentRemoved(startLine,start,numLines);
			scrollLineCount.contentRemoved(startLine,start,numLines);

			//{{{ resize selections if necessary
			int nSel = textArea.getSelectionCount();
			Iterator<Selection> iter = textArea.getSelectionIterator();
			while(iter.hasNext())
			{
				Selection s = iter.next();

				if(s.contentRemoved(buffer,startLine,
					start,numLines,length))
				{
					delayUpdate(s.startLine,s.endLine);
					if(nSel == 1 && s.start == s.end)
						iter.remove();
				}
			} //}}}

			int caret = textArea.getCaretPosition();

			if(caret >= start + length)
			{
				int scrollMode = textArea.caretAutoScroll()
					? TextArea.ELECTRIC_SCROLL
					: TextArea.NO_SCROLL;
				textArea.moveCaretPosition(
					caret - length,
					scrollMode);
			}
			else if(caret >= start)
			{
				int scrollMode = textArea.caretAutoScroll()
					? TextArea.ELECTRIC_SCROLL
					: TextArea.NO_SCROLL;
				textArea.moveCaretPosition(
					start,scrollMode);
			}
			else
			{
				int scrollMode = textArea.caretAutoScroll()
					? TextArea.NORMAL_SCROLL
					: TextArea.NO_SCROLL;
				textArea.moveCaretPosition(caret,scrollMode);
			}
		}
	}
	//}}}

	//{{{ transactionComplete() method
	@Override
	public void transactionComplete(JEditBuffer buffer)
	{
		if(textArea.getDisplayManager() != displayManager)
		{
			delayedUpdate = false;
			return;
		}

		if(delayedUpdate)
			doDelayedUpdate();

		textArea._finishCaretUpdate();

		delayedUpdate = false;

		//{{{ Debug code
		if(Debug.SCROLL_VERIFY)
		{
			int line = delayedUpdateStart;
			if(!displayManager.isLineVisible(line))
				line = displayManager.getNextVisibleLine(line);
			System.err.println(delayedUpdateStart + ":" + delayedUpdateEnd + ':' + textArea.getLineCount());
			int scrollLineCount = 0;
			while(line != -1 && line <= delayedUpdateEnd)
			{
				scrollLineCount += displayManager.getScreenLineCount(line);
				line = displayManager.getNextVisibleLine(line);
			}

			if(scrollLineCount != displayManager.getScrollLineCount())
			{
				throw new InternalError(scrollLineCount
					+ " != "
					+ displayManager.getScrollLineCount());
			}
		} //}}}
	} //}}}

	//{{{ doDelayedUpdate() method
	private void doDelayedUpdate()
	{
		// must be done before the below call
		// so that the chunk cache is not
		// updated with an invisible first
		// line (see above)
		displayManager.notifyScreenLineChanges();

		if(delayedMultilineUpdate)
		{
			textArea.invalidateScreenLineRange(
				textArea.chunkCache
				.getScreenLineOfOffset(
				delayedUpdateStart,0),
				textArea.getVisibleLines());
			delayedMultilineUpdate = false;
		}
		else
		{
			textArea.invalidateLineRange(
				delayedUpdateStart,
				delayedUpdateEnd);
		}

		// update visible lines
		int visibleLines = textArea.getVisibleLines();
		if(visibleLines != 0)
		{
			textArea.chunkCache.getLineInfo(
				visibleLines - 1);
		}

		// force the fold levels to be
		// updated.

		// when painting the last line of
		// a buffer, Buffer.isFoldStart()
		// doesn't call getFoldLevel(),
		// hence the foldLevelChanged()
		// event might not be sent for the
		// previous line.

		buffer.getFoldLevel(delayedUpdateEnd);
	} //}}}

	//{{{ delayUpdate() method
	private void delayUpdate(int startLine, int endLine)
	{
		textArea.chunkCache.invalidateChunksFromPhys(startLine);

		if(!delayedUpdate)
		{
			delayedUpdateStart = startLine;
			delayedUpdateEnd = endLine;
			delayedUpdate = true;
		}
		else
		{
			delayedUpdateStart = Math.min(
				delayedUpdateStart,
				startLine);
			delayedUpdateEnd = Math.max(
				delayedUpdateEnd,
				endLine);
		}
	} //}}}

	//{{{ getReadyToBreakFold() method
	// This is a fix for black hole bug.
	// If you modify a part of folded lines, like {{{ (followed by }}}),
	// the fold is removed so it must be expanded otherwise the text
	// remains invisible.
	private void getReadyToBreakFold(int line)
	{
		displayManager.expandFold(line, false);
	} //}}}
}
