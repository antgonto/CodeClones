/*
 * BufferSaveRequest.java - I/O request
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2005 Slava Pestov
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

package org.gjt.sp.jedit.bufferio;

//{{{ Imports
import java.io.*;
import java.util.zip.*;

import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.*;
import java.nio.charset.UnsupportedCharsetException;
//}}}

/**
 * A buffer save request.
 * @author Slava Pestov
 * @version $Id: BufferSaveRequest.java 25003 2020-03-22 08:40:48Z kpouer $
 */
public class BufferSaveRequest extends BufferIORequest
{
	//{{{ BufferSaveRequest constructor
	/**
	 * Creates a new buffer I/O request.
	 * @param view The view
	 * @param buffer The buffer
	 * @param session The VFS session
	 * @param vfs The VFS
	 * @param path The path
	 */
	public BufferSaveRequest(View view, Buffer buffer,
		Object session, VFS vfs, String path)
	{
		super(view,buffer,session,vfs,path);
	} //}}}

	//{{{ run() method
	@Override
	public void _run()
	{
		/* if the VFS supports renaming files, we first
		 * save to #<filename>#save#, then rename that
		 * to <filename>, so that if the save fails,
		 * data will not be lost.
		 *
		 * as of 4.1pre7 we now call vfs.getTwoStageSaveName()
		 * instead of constructing the path directly
		 * since some VFS's might not allow # in filenames.
		 */

		boolean twoStageSave = willTwoStageSave(buffer);

		try
		{
			String[] args = { vfs.getFileName(path) };
			setStatus(jEdit.getProperty("vfs.status.save",args));

			// the entire save operation can be aborted...
			setCancellable(true);

			String originalPath = path;
			path = vfs._canonPath(session,path,view);
			if(!MiscUtilities.isURL(path))
				path = MiscUtilities.resolveSymlinks(path);

			String savePath;
			if(twoStageSave)
			{
				savePath = vfs.getTwoStageSaveName(path);
				if (savePath == null)
				{
					throw new IOException(
						"Can't get a temporary path for two-stage save: "
						+ path);
				}
				if (!doSave(savePath))
					return;

				makeBackup();
				if(!vfs._rename(session,savePath,path,view))
					throw new IOException("Rename failed: " + savePath);
			}
			else
			{
				makeBackup();
				savePath = path;
				if (!doSave(savePath))
					return;

				VFSManager.sendVFSUpdate(vfs, originalPath, true);
			}
		}
		catch (FileNotFoundException e)
		{
			Log.log(Log.ERROR,this,"Unable to save buffer " + e);
			String[] pp = { e.getMessage() };
			VFSManager.error(view,path,"ioerror.write-error",pp);

			buffer.setBooleanProperty(ERROR_OCCURRED,true);
		}
		catch(UnsupportedCharsetException e)
		{
			Log.log(Log.ERROR, this, e, e);
			String[] pp = { e.getCharsetName() };
			VFSManager.error(view,path,"ioerror.unsupported-encoding-error",pp);

			buffer.setBooleanProperty(ERROR_OCCURRED,true);
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR,this,e);
			String[] pp = { e.toString() };
			VFSManager.error(view,path,"ioerror.write-error",pp);

			buffer.setBooleanProperty(ERROR_OCCURRED,true);
		}
		finally
		{
			try
			{
				vfs._saveComplete(session,buffer,path,view);
				if( twoStageSave )
				{
					vfs._finishTwoStageSave(session,buffer,path,view);
				}
				// clean up left-over markers file
				if(!jEdit.getBooleanProperty("persistentMarkers"))
					vfs._delete(session,Buffer.getMarkersPath(vfs, path),view);
				vfs._endVFSSession(session,view);
			}
			catch(Exception e)
			{
				Log.log(Log.ERROR,this,e);
				String[] pp = { e.toString() };
				VFSManager.error(view,path,"ioerror.write-error",pp);

				buffer.setBooleanProperty(ERROR_OCCURRED,true);
			}
		}
	} //}}}

	//{{{ run() method

	/**
	 * Save the file
	 *
	 * @param savePath the save path to save the file
	 * @return false if we were unable to create output stream.
	 * @throws IOException
	 */
	private boolean doSave(String savePath) throws IOException
	{
		OutputStream out = vfs._createOutputStream(session,savePath,view);
		if(out == null)
		{
			buffer.setBooleanProperty(ERROR_OCCURRED,true);
			return false;
		}
		try
		{
			// this must be after the stream is created or
			// we deadlock with SSHTools.
			buffer.readLock();
			try
			{
				// Can't use buffer.getName() here because
				// it is not changed until the save is
				// complete
				if(path.endsWith(".gz"))
					buffer.setBooleanProperty(Buffer.GZIPPED,true);
				else if (buffer.getName().endsWith(".gz"))
				{
					// The path do not ends with gz.
					// The buffer name was .gz.
					// So it means it's blabla.txt.gz -> blabla.txt, I remove
					// the gz property
					buffer.setBooleanProperty(Buffer.GZIPPED, false);
				}

				if(buffer.getBooleanProperty(Buffer.GZIPPED))
					out = new GZIPOutputStream(out);

				write(buffer,out);
			}
			finally
			{
				buffer.readUnlock();
			}
		}
		catch(InterruptedException e)
		{
			buffer.setBooleanProperty(ERROR_OCCURRED,true);
			Thread.currentThread().interrupt();
		}
		finally
		{
			IOUtilities.closeQuietly(out);
		}
		return true;
	} //}}}

	//{{{ Private members

	//{{{ makeBackup() method
	/**
	 * Make the backup.
	 * Only one backup per jEdit session is done unless if you choosed "backup at every save"
	 */
	private void makeBackup()
	{
		try
		{
			// Only backup once per session
			if(!buffer.hasProperty(Buffer.BACKED_UP) || jEdit.getBooleanProperty("backupEverySave"))
			{
				if (jEdit.getIntegerProperty("backups",1) > 0)
					vfs._backup(session,path,view);
				buffer.setBooleanProperty(Buffer.BACKED_UP, true);
			}
		}
		catch (IOException ioe)
		{
			// Backup failure shouldn't stop caller's activity, like
			// saving a buffer, so catching the exception.
			// Most backup failures are reported directly in _backup,
			// with lesser severity.
			// Only rare cases manifest with IOException, like #3574500 
			String[] pp = { ioe.getMessage() };
			VFSManager.error(view,path,"ioerror.backup-failed",pp);
		}
	} //}}}

	//{{{ wantTwoStageSave() method
	private boolean willTwoStageSave(Buffer buffer)
	{
		boolean vfsRenameCap = (vfs.getCapabilities() & VFS.RENAME_CAP) != 0;
		return vfsRenameCap &&
				!buffer.getBooleanProperty("forbidTwoStageSave") &&
				(buffer.getBooleanProperty("overwriteReadonly") ||
						jEdit.getBooleanProperty("twoStageSave"));
	}//}}}

	//}}}
}
