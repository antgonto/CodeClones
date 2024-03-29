/*
 * DisplayManager.java - Low-level text display
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

//{{{ Imports
import java.util.*;
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.Debug;
import org.gjt.sp.util.Log;
//}}}

/**
 * Manages low-level text display tasks, such as folding.
 * 
 * @since jEdit 4.2pre1
 * @author Slava Pestov
 * @version $Id: DisplayManager.java 25246 2020-04-15 19:17:33Z kpouer $
 */
public class DisplayManager
{
	//{{{ Static part

	//{{{ getDisplayManager() method
	static DisplayManager getDisplayManager(JEditBuffer buffer,
		TextArea textArea)
	{
		List<DisplayManager> l = bufferMap.computeIfAbsent(buffer, k -> new LinkedList<>());

		/* An existing display manager's fold visibility map
		that a new display manager will inherit */
		DisplayManager copy = null;
		Iterator<DisplayManager> liter = l.iterator();
		DisplayManager dmgr;
		while(liter.hasNext())
		{
			dmgr = liter.next();
			copy = dmgr;
			if(!dmgr.inUse && dmgr.textArea == textArea)
			{
				dmgr.inUse = true;
				return dmgr;
			}
		}

		// if we got here, no unused display manager in list
		dmgr = new DisplayManager(buffer,textArea,copy);
		dmgr.inUse = true;
		l.add(dmgr);

		return dmgr;
	} //}}}

	//{{{ release() method
	void release()
	{
		inUse = false;
	} //}}}

	//{{{ bufferClosed() method
	public static void bufferClosed(JEditBuffer buffer)
	{
		bufferMap.remove(buffer);
	} //}}}

	//{{{ textAreaDisposed() method
	static void textAreaDisposed(TextArea textArea)
	{
		for (List<DisplayManager> l : bufferMap.values())
		{
			Iterator<DisplayManager> liter = l.iterator();
			while(liter.hasNext())
			{
				DisplayManager dmgr = liter.next();
				if(dmgr.textArea == textArea)
				{
					dmgr.dispose();
					liter.remove();
				}
			}
		}
	} //}}}

	private static final Map<JEditBuffer, List<DisplayManager>> bufferMap = new HashMap<JEditBuffer, List<DisplayManager>>();
	//}}}

	//{{{ getBuffer() method
	/**
	 * @since jEdit 4.3pre3
	 */
	public JEditBuffer getBuffer()
	{
		return buffer;
	} //}}}

	//{{{ isLineVisible() method
	/**
	 * Returns if the specified physical line is visible.
	 * @param line A physical line index
	 * @since jEdit 4.2pre1
	 */
	public boolean isLineVisible(int line)
	{
		return folds.search(line) % 2 == 0;
	} //}}}

	//{{{ isOutsideNarrowing() method
	/**
	 * Returns true if the display is narrowed and the specified line is
	 * outside of the narrowing.
	 * @param line A physical line index
	 * @since jEdit 4.5
	 */
	public boolean isOutsideNarrowing(int line)
	{
		if (line < getFirstVisibleLine())
			return true;
		
		// If the line is beyond the last visible line, it may still be within the narrowed
		// display if the last visible line is a root for the fold containing the given
		// line.
		if (line > getLastVisibleLine())
		{
			int lastVisible = getLastVisibleLine();
			int lastVisibleLevel = buffer.getFoldLevel(lastVisible);
			if (buffer.getFoldLevel(line) <= lastVisibleLevel)
				return true;
			
			// Any line between the last visible and the given line with a fold level
			// <= that of the last visible will break the root-child relationship
			for (int i = lastVisible + 1; i <= line; i++)
			{
				if (buffer.getFoldLevel(i) <= lastVisibleLevel)
					return true;
			}
			
			// If we get to this point, it means that the last visible line is a
			// fold-level root for the given line
			return false;
		}
		
		return false;
	} //}}}
	
	//{{{ getFirstVisibleLine() method
	/**
	 * Returns the physical line number of the first visible line.
	 * @since jEdit 4.2pre1
	 */
	public int getFirstVisibleLine()
	{
		return folds.first();
	} //}}}

	//{{{ getLastVisibleLine() method
	/**
	 * Returns the physical line number of the last visible line.
	 * @since jEdit 4.2pre1
	 */
	public int getLastVisibleLine()
	{
		return folds.last();
	} //}}}

	//{{{ getNextVisibleLine() method
	/**
	 * Returns the next visible line after the specified line index,
	 * or (-1) if there is no next visible line.
	 * @param line A physical line index
	 * @since jEdit 4.0pre1
	 */
	public int getNextVisibleLine(int line)
	{
		if(line < 0 || line >= buffer.getLineCount())
			throw new ArrayIndexOutOfBoundsException(line);

		return folds.next(line);
	} //}}}

	//{{{ getPrevVisibleLine() method
	/**
	 * Returns the previous visible line before the specified line index.
	 * @param line a physical line index
	 * @return the previous visible physical line or -1 if there is no visible line
	 * @since jEdit 4.0pre1
	 */
	public int getPrevVisibleLine(int line)
	{
		if(line < 0 || line >= buffer.getLineCount())
			throw new ArrayIndexOutOfBoundsException(line);

		return folds.prev(line);
	} //}}}

	//{{{ getScreenLineCount() method
	/**
	 * Returns how many screen lines contains the given physical line.
	 * It can be greater than 1 when using soft wrap
	 *
	 * @param line the physical line
	 * @return the screen line count
	 */
	public int getScreenLineCount(int line)
	{
		updateScreenLineCount(line);
		return screenLineMgr.getScreenLineCount(line);
	} //}}}

	//{{{ getScrollLineCount() method
	/**
	 * Returns the number of displayable lines
	 * It can be greater than the number of lines of the buffer when using
	 * soft wrap (a line can count for n lines), or when using folding, if
	 * the foldings are collapsed
	 * @return the number of displayable lines
	 */
	public int getScrollLineCount()
	{
		return scrollLineCount.getScrollLine();
	} //}}}

	//{{{ collapseFold() method
	/**
	 * Collapses the fold at the specified physical line index.
	 * @param line A physical line index
	 * @since jEdit 4.2pre1
	 */
	public void collapseFold(int line)
	{
		int lineCount = buffer.getLineCount();
		int end = lineCount - 1;

		// if the caret is on a collapsed fold, collapse the
		// parent fold
		if(line != 0
			&& line != lineCount - 1
			&& buffer.isFoldStart(line)
			&& !isLineVisible(line + 1))
		{
			line--;
		}

		int initialFoldLevel = buffer.getFoldLevel(line);

		//{{{ Find fold start and end...
		int start = 0;
		if(line != lineCount - 1
			&& buffer.getFoldLevel(line + 1) > initialFoldLevel)
		{
			// this line is the start of a fold
			start = line + 1;

			for(int i = line + 1; i < lineCount; i++)
			{
				if(buffer.getFoldLevel(i) <= initialFoldLevel)
				{
					end = i - 1;
					break;
				}
			}
		}
		else
		{
			boolean ok = false;

			// scan backwards looking for the start
			for(int i = line - 1; i >= 0; i--)
			{
				if(buffer.getFoldLevel(i) < initialFoldLevel)
				{
					start = i + 1;
					ok = true;
					break;
				}
			}

			if(!ok)
			{
				// no folds in buffer
				return;
			}

			for(int i = line + 1; i < lineCount; i++)
			{
				if(buffer.getFoldLevel(i) < initialFoldLevel)
				{
					end = i - 1;
					break;
				}
			}
		} //}}}

		// Collapse the fold...
		hideLineRange(start,end);

		notifyScreenLineChanges();
		textArea.foldStructureChanged();
	} //}}}

	//{{{ expandFold() method
	/**
	 * Expands the fold at the specified physical line index.
	 * @param line A physical line index
	 * @param fully If true, all subfolds will also be expanded
	 * @return the line number of the first subfold, or -1 if none
	 * @since jEdit 4.2pre1
	 */
	public int expandFold(int line, boolean fully)
	{
		MutableInteger firstSubfold = new MutableInteger(-1);
		boolean unfolded = _expandFold(line, fully, firstSubfold);
		
		if (unfolded)
			textArea.foldStructureChanged();
		
		return firstSubfold.get();
	} //}}}

	//{{{ expandAllFolds() method
	/**
	 * Expands all folds.
	 * @since jEdit 4.2pre1
	 */
	public void expandAllFolds()
	{
		showLineRange(0,buffer.getLineCount() - 1);
		notifyScreenLineChanges();
		textArea.foldStructureChanged();
	} //}}}

	//{{{ expandFolds() method
	/**
	 * This method should only be called from <code>actions.xml</code>.
	 * @since jEdit 4.2pre1
	 */
	public void expandFolds(char digit)
	{
		if(digit < '1' || digit > '9')
		{
			javax.swing.UIManager.getLookAndFeel().provideErrorFeedback(null); 
		}
		else
			expandFolds((digit - '1') + 1);
	} //}}}

	//{{{ expandFolds() method
	/**
	 * Expands all folds with the specified fold level.
	 * @param foldLevel The fold level
	 * @param update If true, notify the text area of a fold level change. Since this will
	 *   automatically move the caret if still inside a fold, this may not be what we want.
	 * @since jEdit 4.5
	 */
	public void expandFolds(int foldLevel, boolean update)
	{
		if(buffer.getFoldHandler() instanceof IndentFoldHandler)
			foldLevel = (foldLevel - 1) * buffer.getIndentSize() + 1;

		int lineCount = buffer.getLineCount();
		int end = lineCount - 1;
		showLineRange(0,end);

		int leastFolded = -1;
		int firstInvisible = 0;

		for(int i = 0; i < lineCount; i++)
		{
			int level = buffer.getFoldLevel(i);
			// Keep track of the least fold level up to this point in the file,
			// because we can't hide a line at this level since there will be no "root"
			// line to unfold it from
			if (leastFolded == -1 || level < leastFolded)
			{
				leastFolded = level;
			}
		
			if (level < foldLevel || level == leastFolded)
			{
				if(firstInvisible != i)
				{
					hideLineRange(firstInvisible,
						i - 1);
				}
				firstInvisible = i + 1;
			}
		}

		if(firstInvisible != lineCount)
			hideLineRange(firstInvisible,end);

		notifyScreenLineChanges();
		if(update && textArea.getDisplayManager() == this)
		{
			textArea.foldStructureChanged();
		}
	} //}}}
	
	//{{{ expandFolds() method
	/**
	 * Expands all folds with the specified fold level.
	 * @param foldLevel The fold level
	 * @since jEdit 4.2pre1
	 */
	public void expandFolds(int foldLevel)
	{
		expandFolds(foldLevel, true);
	} //}}}

	//{{{ narrow() method
	/**
	 * Narrows the visible portion of the buffer to the specified
	 * line range.
	 * @param start The first line
	 * @param end The last line
	 * @since jEdit 4.2pre1
	 */
	public void narrow(int start, int end)
	{
		int lineCount = buffer.getLineCount();
		if(start > end || start < 0 || end >= lineCount)
			throw new ArrayIndexOutOfBoundsException(start + ", " + end);

		if(start < getFirstVisibleLine() || end > getLastVisibleLine())
			expandAllFolds();

		if(start != 0)
			hideLineRange(0,start - 1);
		if(end != lineCount - 1)
			hideLineRange(end + 1,lineCount - 1);

		// if we narrowed to a single collapsed fold
		if(start != lineCount - 1 && !isLineVisible(start + 1))
			expandFold(start,false);

		textArea.fireNarrowActive();

		notifyScreenLineChanges();
		textArea.foldStructureChanged();
	} //}}}

	//{{{ Package-private members
	final FirstLine firstLine;
	final ScrollLineCount scrollLineCount;
	final ScreenLineManager screenLineMgr;
	final RangeMap folds;

	//{{{ init() method
	void init()
	{
		// Needs information available in textArea only when a
		// DisplayManager is active in it.
		assert textArea.getDisplayManager() == this;

		if(buffer.isLoading())
			// init() will be called later from bufferLoaded().
			return;

		if(!initialized)
		{
			folds.reset(buffer.getLineCount());
			resetAnchors();
			int collapseFolds = buffer.getIntegerProperty(
				"collapseFolds",0);
			if(collapseFolds != 0)
				expandFolds(collapseFolds);
			initialized = true;
		}
		else
		{
			// Already initialized.
			// Just make the scroll bar updated.
			resetAnchors();
		}
	} //}}}

	//{{{ notifyScreenLineChanges() method
	/**
	 * FirstLine or ScrollLineCount changed
	 * Update ScrollBar, etc.
	 */
	void notifyScreenLineChanges()
	{
		if(Debug.SCROLL_DEBUG)
			Log.log(Log.DEBUG,this,"notifyScreenLineChanges()");

		// Screen line change must be issued when the textArea
		// has information of wrap mode for the buffer.
		// Otherwise, the screen line calculation will be incorrect.
		assert textArea.getDisplayManager() == this;

		try
		{
			if(firstLine.isCallReset())
				firstLine.reset();
			else if(firstLine.isCallChanged())
				firstLine.changed();

			if(scrollLineCount.isCallReset())
			{
				scrollLineCount.reset();
				//FIXME: Why here?
				firstLine.ensurePhysicalLineIsVisible();
			} else if(scrollLineCount.isCallChanged())
				scrollLineCount.changed();

			if(firstLine.isCallChanged() ||
			   scrollLineCount.isCallReset() ||
			   scrollLineCount.isCallChanged())
			{
				textArea.updateScrollBar();
				textArea.recalculateLastPhysicalLine();
			}
		}
		finally
		{
			firstLine.resetCallState();
			scrollLineCount.resetCallState();
		}
	} //}}}

	//{{{ setFirstLine() method
	/**
	 * Sets the vertical scroll bar position
	 *
	 * @param currentFirstLine the current scroll bar position
	 * @param newFirstLine The to-be scroll bar position
	 */
	void setFirstLine(int currentFirstLine, int newFirstLine)
	{
		int visibleLines = textArea.getVisibleLines();

		if(newFirstLine >= currentFirstLine + visibleLines)
		{
			this.firstLine.scrollDown(newFirstLine - currentFirstLine);
			textArea.chunkCache.invalidateAll();
		}
		else if(newFirstLine <= currentFirstLine - visibleLines)
		{
			this.firstLine.scrollUp(currentFirstLine - newFirstLine);
			textArea.chunkCache.invalidateAll();
		}
		else if(newFirstLine > currentFirstLine)
		{
			this.firstLine.scrollDown(newFirstLine - currentFirstLine);
			textArea.chunkCache.scrollDown(newFirstLine - currentFirstLine);
		}
		else if(newFirstLine < currentFirstLine)
		{
			this.firstLine.scrollUp(currentFirstLine - newFirstLine);
			textArea.chunkCache.scrollUp(currentFirstLine - newFirstLine);
		} else
			assert true;

		notifyScreenLineChanges();
	} //}}}

	//{{{ setFirstPhysicalLine() method
	/**
	 * Scroll from a given amount of lines.
	 *
	 * @param amount the amount of lines that must be scrolled
	 * @param skew a skew within the given line
	 */
	void setFirstPhysicalLine(int amount, int skew)
	{
		int currentFirstLine = textArea.getFirstLine();

		if(amount == 0)
		{
			skew -= this.firstLine.getSkew();

			// JEditTextArea.scrollTo() needs this to simplify
			// its code
			if(skew < 0)
				this.firstLine.scrollUp(-skew);
			else if(skew > 0)
				this.firstLine.scrollDown(skew);
			else
			{
				// nothing to do
				return;
			}
		}
		else if(amount > 0)
			this.firstLine.physDown(amount,skew);
		else // amount < 0;
			this.firstLine.physUp(-amount,skew);

		int firstLine = textArea.getFirstLine();
		int visibleLines = textArea.getVisibleLines();

		if(firstLine == currentFirstLine)
			/* do nothing */;
		else if(firstLine >= currentFirstLine + visibleLines
			|| firstLine <= currentFirstLine - visibleLines)
		{
			textArea.chunkCache.invalidateAll();
		}
		else if(firstLine > currentFirstLine)
		{
			textArea.chunkCache.scrollDown(firstLine - currentFirstLine);
		}
		else // firstLine < currentFirstLine
			textArea.chunkCache.scrollUp(currentFirstLine - firstLine);

		// we have to be careful
		notifyScreenLineChanges();
	} //}}}

	//{{{ invalidateScreenLineCounts() method
	void invalidateScreenLineCounts()
	{
		screenLineMgr.invalidateScreenLineCounts();
		firstLine.setCallReset(true);
		scrollLineCount.setCallReset(true);
	} //}}}

	//{{{ updateScreenLineCount() method
	void updateScreenLineCount(int line)
	{
		// If this DisplayManager is not current visible one,
		// screen line count can't be determined since the wrap
		// mode and the wrap margin (and chunkCache) for the
		// buffer are not available.
		// Maybe, those information should be in DisplayManager
		// instead of textArea.
		assert textArea.getDisplayManager() == this;

		if(!screenLineMgr.isScreenLineCountValid(line))
		{
			// reset chunk cache here
			textArea.chunkCache.reset();
			int newCount = textArea.chunkCache.getLineSubregionCount(line);

			assert newCount > 0;
			setScreenLineCount(line,newCount);
		}
	} //}}}

	//{{{ bufferLoaded() method
	void bufferLoaded()
	{
		initialized = false;
		folds.reset(buffer.getLineCount());
		screenLineMgr.reset();
		if(textArea.getDisplayManager() == this)
		{
			textArea.propertiesChanged();
			init();
		}
		else
		{
			// init() will be called later when the buffer
			// is set in the textArea.
		}
	} //}}}

	//{{{ foldHandlerChanged() method
	void foldHandlerChanged()
	{
		if(buffer.isLoading())
			// Happens once before bufferLoaded() is called.
			// It seems violating the javadoc on BufferListener,
			// but it's not a problem in DisplayManager because
			// this is called later on init(), possibly via
			// bufferLoaded().
			return;

		initialized = false;
		folds.reset(buffer.getLineCount());
		if(textArea.getDisplayManager() == this)
		{
			init();
		}
		else
		{
			// init() will be called later when the buffer
			// is set in the textArea.
		}
	} //}}}

	//}}}

	//{{{ Private members
	private boolean initialized;
	private boolean inUse;
	private final JEditBuffer buffer;
	private final TextArea textArea;
	private final BufferHandler bufferHandler;
	private final ElasticTabStopBufferListener elasticTabStopListener;

	//{{{ DisplayManager constructor
	private DisplayManager(JEditBuffer buffer, TextArea textArea,
		DisplayManager copy)
	{
		this.buffer = buffer;
		this.screenLineMgr = new ScreenLineManager(buffer);
		this.textArea = textArea;

		scrollLineCount = new ScrollLineCount(this,textArea);
		firstLine = new FirstLine(this,textArea);
		bufferHandler = new BufferHandler(this,textArea,buffer);
		//TODO:invoke ElasticTabStopBufferListener methods from inside BufferHandler to avoid chunking same line twice
		elasticTabStopListener = new ElasticTabStopBufferListener(textArea);
		buffer.addBufferListener(elasticTabStopListener, JEditBuffer.HIGH_PRIORITY);
		// this listener priority thing is a bad hack...
		buffer.addBufferListener(bufferHandler, JEditBuffer.HIGH_PRIORITY);

		if(copy != null)
		{
			folds = new RangeMap(copy.folds);
			initialized = true;
		}
		else
		{
			folds = new RangeMap();
			folds.reset(0);
		}
	} //}}}

	//{{{ resetAnchors() method
	private void resetAnchors()
	{
		firstLine.setCallReset(true);
		scrollLineCount.setCallReset(true);
		notifyScreenLineChanges();
	} //}}}

	//{{{ dispose() method
	private void dispose()
	{
		buffer.removeBufferListener(bufferHandler);
		buffer.removeBufferListener(elasticTabStopListener);
	} //}}}

	//{{{ showLineRange() method
	// for folding
	private void showLineRange(int start, int end)
	{
		if(Debug.FOLD_VIS_DEBUG)
		{
			Log.log(Log.DEBUG,this,"showLineRange(" + start
				+ ',' + end + ')');
		}

		for(int i = start; i <= end; i++)
		{
			//XXX
			if(!isLineVisible(i))
			{
				// important: not screenLineMgr.getScreenLineCount()
				int screenLines = getScreenLineCount(i);
				if(firstLine.getPhysicalLine() >= i)
				{
					firstLine.moveScrollLine(screenLines);
				}
				scrollLineCount.moveScrollLine(screenLines);
			}
		}

		/* update fold visibility map. */
		folds.show(start,end);
	} //}}}

	//{{{ hideLineRange() method
	private void hideLineRange(int start, int end)
	{
		if(Debug.FOLD_VIS_DEBUG)
		{
			Log.log(Log.DEBUG,this,"hideLineRange(" + start
				+ ',' + end + ')');
		}

		int physicalLine = start;
		if(!isLineVisible(physicalLine))
			physicalLine = getNextVisibleLine(physicalLine);

		int scrollLines = 0;
		while(physicalLine != -1 && physicalLine <= end)
		{
			int screenLines = getScreenLineCount(physicalLine);
			if(physicalLine < firstLine.getPhysicalLine())
			{
				firstLine.setSkew(0);
				firstLine.moveScrollLine(-screenLines);
			}

			scrollLines -= screenLines;
			physicalLine = getNextVisibleLine(physicalLine);
		}
		scrollLineCount.moveScrollLine(scrollLines);

		/* update fold visibility map. */
		folds.hide(start,end);

		if(!isLineVisible(firstLine.getPhysicalLine()))
		{
			int firstVisible = getFirstVisibleLine();
			if(firstLine.getPhysicalLine() < firstVisible)
			{
				firstLine.setPhysicalLine(firstVisible);
				firstLine.setScrollLine(0);
			}
			else
			{
				firstLine.setPhysicalLine(getPrevVisibleLine(firstLine.getPhysicalLine()));
				firstLine.moveScrollLine(-getScreenLineCount(firstLine.getPhysicalLine()));
			}
		}
	} //}}}

	//{{{ setScreenLineCount() method
	/**
	 * Sets the number of screen lines that the specified physical line
	 * is split into.
	 * @param line the physical line number
	 * @param count the line count (== 1 if no wrap, > 1 if soft wrap)
	 * @since jEdit 4.2pre1
	 */
	private void setScreenLineCount(int line, int count)
	{
		assert count > 0;
		screenLineMgr.setScreenLineCount(line,count);

	} //}}}

	//{{{ _expandFold() method
	/**
	 * Expands the fold at the specified physical line index.
	 * @param line A physical line index
	 * @param fully If true, all subfolds will also be expanded
	 * @param firstSubfold Will be set to the line number of the first
	 * subfold, or -1 if there is none.
	 * @return True if some line was unfolded, false otherwise.
	 */
	private boolean _expandFold(int line, boolean fully, MutableInteger firstSubfold)
	{
		boolean unfolded = false;

		int lineCount = buffer.getLineCount();
		int end = lineCount - 1;

		while (!isLineVisible(line))
		{
			int prevLine = folds.lookup(folds.search(line)) - 1;
			if (!isLineVisible(prevLine))
			{
				return unfolded;
			}
			
			// If any fold farther up was unfolded, then the text
			// area needs to be updated
			unfolded |= _expandFold(prevLine, fully, firstSubfold);
			
			if (!isLineVisible(prevLine + 1))
			{
				return unfolded;
			}
		}
		
		// At this point, line is already visible, but if the fully flag is set, we may
		// still need to look for and show its subfolds.
		if (line == (lineCount - 1) || (isLineVisible(line + 1) && !fully))
		{
			return unfolded;
		}

		//{{{ Find fold start and fold end...
		int start;
		int initialFoldLevel = buffer.getFoldLevel(line);
		if (buffer.getFoldLevel(line + 1) > initialFoldLevel)
		{
			// this line is the start of a fold
			start = line;
			if (!isLineVisible(line + 1) && folds.search(line + 1) != folds.count() - 1)
			{
				int index = folds.search(line + 1);
				end = folds.lookup(index + 1) - 1;
			}
			else
			{
				for (int i = line + 1; i < lineCount; i++)
				{
					if (buffer.getFoldLevel(i) <= initialFoldLevel)
					{
						end = i - 1;
						break;
					}
				}
			}
		}
		else
		{
			if (!fully)
			{
				return unfolded;
			}
			start = line;
			while (start > 0 && buffer.getFoldLevel(start) >= initialFoldLevel)
			{
				start--;
			}
			initialFoldLevel = buffer.getFoldLevel(start);
			for (int i = line + 1; i < lineCount; i++)
			{
				if (buffer.getFoldLevel(i) <= initialFoldLevel)
				{
					end = i - 1;
					break;
				}
			}
		} // }}}

		//{{{ Expand the fold...
		if(fully)
		{
			showLineRange(start,end);
		}
		else
		{
			boolean foundSubfold = false;
			for (int i = start + 1; i <= end;)
			{
				if (!foundSubfold && buffer.isFoldStart(i))
				{
					firstSubfold.set(i);
					foundSubfold = true;
				}

				showLineRange(i, i);
				int fold = buffer.getFoldLevel(i);
				i++;
				while (i <= end && buffer.getFoldLevel(i) > fold)
				{
					i++;
				}
			}
		}
		
		unfolded = true;
		// }}}
		
		notifyScreenLineChanges();
		return unfolded;
	} //}}}
	
	//{{{ MutableInteger class
	private class MutableInteger
	{
		MutableInteger(int value)
		{
			this.value = value;
		}
		
		public void set(int value)
		{
			this.value = value;
		}
		
		public int get()
		{
			return value;
		}
		
		private int value;
	} //}}}
	
	//}}}
}
