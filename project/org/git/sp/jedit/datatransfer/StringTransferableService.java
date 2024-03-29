/*
 * StringTransferableService.java
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2010 Matthieu Casanova
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
package org.gjt.sp.jedit.datatransfer;

import org.gjt.sp.jedit.textarea.TextArea;

import java.awt.datatransfer.*;

/**
 * @author Matthieu Casanova
 * @since jEdit 4.4.x
 */
public class StringTransferableService implements JEditTransferableService
{
	@Override
	public boolean accept(TextArea textArea, String text)
	{
		return true;
	}

	@Override
	public Transferable getTransferable(TextArea textArea, String text)
	{
		return new StringSelection(text);
	}
}
