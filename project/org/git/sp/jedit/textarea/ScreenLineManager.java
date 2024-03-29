/*
 * ScreenLineManager.java - Manage screen line counts
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2004 Slava Pestov
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
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.Debug;
import org.gjt.sp.util.Log;
//}}}

/**
 * Performs the Mapping between physical lines and screen lines.
 * 
 * @since jEdit 4.3pre1
 * @author Slava Pestov
 * @version $Id: ScreenLineManager.java 25262 2020-04-18 08:12:07Z kpouer $
 */
class ScreenLineManager
{
	//{{{ ScreenLineManager constructor
	ScreenLineManager(JEditBuffer buffer)
	{
		this.buffer = buffer;
		if(!buffer.isLoading())
			reset();
	} //}}}
	
	//{{{ isScreenLineCountValid() method
	boolean isScreenLineCountValid(int line)
	{
		if (screenLines == null || line < 0 || line >= screenLines.length)
			return false;
		return screenLines[line] > 0;
	} //}}}

	//{{{ getScreenLineCount() method
	/**
	 * Returns how many screen lines contains the given physical line.
	 * It can be greater than 1 when using soft wrap
	 *
	 * @param line the physical line
	 * @return the screen line count
	 */
	int getScreenLineCount(int line)
	{
		assert isScreenLineCountValid(line);
		return screenLines[line];
	} //}}}

	//{{{ setScreenLineCount() method
	/**
	 * Sets the number of screen lines that the specified physical line
	 * is split into.
	 * @param line the physical line number
	 * @param count the line count (1 if no wrap)
	 */
	void setScreenLineCount(int line, int count)
	{
		assert count > 0 : "New line count is bogus!";

		if(count > Short.MAX_VALUE)
		{
			// limitations...
			count = Short.MAX_VALUE;
			Log.log(Log.ERROR,this,new Exception("Max screen line count hit!"));
		}

		if(Debug.SCREEN_LINES_DEBUG)
			Log.log(Log.DEBUG,this,"setScreenLineCount(" + line + ',' + count + ')');
		if (screenLines == null) 
			reset();
		screenLines[line] = (char)count;
	} //}}}

	//{{{ invalidateScreenLineCounts() method
	/**
	 * Invalidate all screenlines
	 */
	void invalidateScreenLineCounts()
	{
		invalidateScreenLineCountRange(0, buffer.getLineCount());
	} //}}}

	//{{{ invalidateScreenLineCounts() method
	private void invalidateScreenLineCount(int physicalLineNo)
	{
		screenLines[physicalLineNo] = 0;
	} //}}}

	//{{{ invalidateScreenLineCounts() method
	private void invalidateScreenLineCountRange(int physicalLineStart, int physicalLineEnd)
	{
		for (int i = physicalLineStart; i < physicalLineEnd; i++)
			screenLines[i] = 0;
	} //}}}

	//{{{ reset() method
	void reset()
	{
		screenLines = new char[buffer.getLineCount()];
	} //}}}

	//{{{ contentInserted() method
	public void contentInserted(int startLine, int numLines)
	{
		int lineCount = buffer.getLineCount();
		if(numLines > 0)
		{
			int nbLinesBeforeInsert = lineCount - numLines;
			if(screenLines.length <= lineCount)
			{
				// the array is too small for the new buffer length
				// create a bigger one and copy data into it
				char[] screenLinesN = new char[((lineCount + 1) << 1)];
				if (startLine != 0)
					System.arraycopy(screenLines, 0, screenLinesN, 0, startLine);


				int lengthToCopy = nbLinesBeforeInsert - startLine - 1;
				if (lengthToCopy > 0)
					System.arraycopy(screenLines, startLine + 1, screenLinesN,
						startLine + numLines + 1, lengthToCopy);

				screenLines = screenLinesN;
			}
			else
			{

				int lengthToCopy = nbLinesBeforeInsert - startLine - 1;
				if (lengthToCopy > 0)
					System.arraycopy(screenLines, startLine + 1, screenLines,
						startLine + numLines + 1, lengthToCopy);

				invalidateScreenLineCountRange(startLine, startLine + numLines + 1);
			}
		}
		else
		{
			// the current line count becomes invalid
			invalidateScreenLineCount(startLine);
		}
	} //}}}

	//{{{ contentRemoved() method
	public void contentRemoved(int startLine, int numLines)
	{
		int endLine = startLine + numLines;
		invalidateScreenLineCount(startLine);

		if(numLines > 0 && endLine != screenLines.length)
		{
			// copy the lines after removed lines to their new position
			System.arraycopy(screenLines,endLine + 1,screenLines,
				startLine + 1,screenLines.length - endLine - 1);
		}
	} //}}}

	//{{{ Private members
	private final JEditBuffer buffer;

	/** This array contains the screen line count for each physical line.
	 * screenLines[physicalLineNo] == 0 -> invalid entry - No. of screen lines not calculated yet
	 * screenLines[physicalLineNo] > 0 -> valid entry - No. of screen lines already calculated 
	 */
	private char[] screenLines;
	//}}}
}
