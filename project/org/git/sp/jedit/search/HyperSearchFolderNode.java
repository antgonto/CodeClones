/*
 * HyperSearchFolderNode - HyperSearch Folder Tree Node
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2005 Slava Pestov
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

import java.io.File;

/**
 * A folder in the HyperSearch results window.
 *
 * used when using the expanded view
 * @author Slava Pestov
 */
public class HyperSearchFolderNode 
{
	private File nodeFile;
	private boolean showFullPath;
	private static String fileSep = System.getProperty("file.separator");
	static
	{
		if (fileSep.equals("\\"))
			fileSep = "\\\\";
	}
	
	public File getNodeFile()
	{
		return nodeFile;
	}
	
	public HyperSearchFolderNode(File nodeFile, boolean showFullPath) 
	{
		this.nodeFile = nodeFile;
		this.showFullPath = showFullPath;
	}
	
	public String toString()
	{
		if (showFullPath)
			return nodeFile.getAbsolutePath();
		String paths[] = nodeFile.getAbsolutePath().split(fileSep);
		return paths[paths.length - 1];
		
	}
}
