/*
 * UrlVFS.java - URL VFS
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000 Slava Pestov
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

package org.gjt.sp.jedit.io;

//{{{ Imports
import java.awt.Component;
import java.io.*;
import java.net.*;
import org.gjt.sp.util.Log;
//}}}

/**
 * URL VFS.
 * @author Slava Pestov
 * @version $Id: UrlVFS.java 25000 2020-03-21 19:40:09Z kpouer $
 */
public class UrlVFS extends VFS
{
	//{{{ UrlVFS constructor
	public UrlVFS()
	{
		super("url", READ_CAP | NON_AWT_SESSION_CAP);
	} //}}}

	//{{{ constructPath() method
	@Override
	public String constructPath(String parent, String path)
	{
		if(parent.endsWith("/"))
			return parent + path;
		else
			return parent + '/' + path;
	} //}}}

	//{{{ _createInputStream() method
	@Override
	public InputStream _createInputStream(Object session, String path, boolean ignoreErrors, Component comp)
		throws IOException
	{
		try
		{
			return new URL(path).openStream();
		}
		catch(MalformedURLException mu)
		{
			Log.log(Log.ERROR,this,mu);
			String[] args = { mu.getMessage() };
			VFSManager.error(comp,path,"ioerror.badurl",args);
			return null;
		}
	} //}}}

	//{{{ _createOutputStream() method
	@Override
	public OutputStream _createOutputStream(Object session, String path, Component comp) throws IOException
	{
		try
		{
			return new URL(path).openConnection()
				.getOutputStream();
		}
		catch(MalformedURLException mu)
		{
			Log.log(Log.ERROR,this,mu);
			String[] args = { mu.getMessage() };
			VFSManager.error(comp,path,"ioerror.badurl",args);
			return null;
		}
	} //}}}

	//{{{ getFilePath() method
	@Override
	public String getFilePath(String vfsPath)
	{
		try
		{
			return new URL(vfsPath).getPath();
		}
		catch (MalformedURLException mue)
		{
			Log.log(Log.ERROR,this,mue);
			return super.getFilePath(vfsPath);
		}
	} //}}}
}
