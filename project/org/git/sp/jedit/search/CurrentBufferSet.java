/*
 * CurrentBufferSet.java - Current buffer matcher
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2001 Slava Pestov
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

package org.gjt.sp.jedit.search;

import org.gjt.sp.jedit.*;

/**
 * A file set for searching the current buffer.
 * @author Slava Pestov
 * @version $Id: CurrentBufferSet.java 25108 2020-03-31 22:41:49Z kpouer $
 */
public class CurrentBufferSet implements SearchFileSet
{
	//{{{ getFirstFile() method
	@Override
	public String getFirstFile(View view)
	{
		return view.getBuffer().getPath();
	} //}}}
	
	//{{{ getLastFile() method
	@Override
	public String getLastFile(View view)
	{
		return view.getBuffer().getPath();
	} //}}}

	//{{{ getNextFile() method
	@Override
	public String getNextFile(View view, String file)
	{
		if(file == null)
			return view.getBuffer().getPath();
		else
			return null;
	} //}}}
	
	//{{{ getPrevFile() method
	@Override
	public String getPrevFile(View view, String file)
	{
		if(file == null)
			return view.getBuffer().getPath();
		else
			return null;
	} //}}}

	//{{{ getFiles() method
	@Override
	public String[] getFiles(View view)
	{
		return new String[] { view.getBuffer().getPath() };
	} //}}}

	//{{{ getFileCount() method
	@Override
	public int getFileCount(View view)
	{
		return 1;
	} //}}}

	//{{{ getCode() method
	@Override
	public String getCode()
	{
		return "new CurrentBufferSet()";
	} //}}}
}
