/*
 * DummyTokenHandler.java - Ignores tokens
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2002 Slava Pestov
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

package org.gjt.sp.jedit.syntax;

import javax.swing.text.Segment;

/**
 * A dummy token handler that discards tokens.
 *
 * @author Slava Pestov
 * @version $Id: DummyTokenHandler.java 25244 2020-04-15 15:14:49Z kpouer $
 * @since jEdit 4.1pre1
 */
public class DummyTokenHandler implements TokenHandler
{
	/**
	 * To avoid having to create new instances of this class, use
	 * this variable. This is allowed because instances of this
	 * class do not store any state.
	 */
	public static final DummyTokenHandler INSTANCE = new DummyTokenHandler();

	//{{{ handleToken() method
	/**
	 * Called by the token marker when a syntax token has been parsed.
	 * @param seg The segment containing the text
	 * @param id The token type (one of the constants in the
	 * {@link Token} class).
	 * @param offset The start offset of the token
	 * @param length The number of characters in the token
	 * @param context The line context
	 * @since jEdit 4.2pre3
	 */
	@Override
	public void handleToken(Segment seg, byte id, int offset, int length,
		TokenMarker.LineContext context) {} //}}}

	//{{{ setLineContext() method
	/**
	 * The token handler can compare this object with the object
	 * previously given for this line to see if the token type at the end
	 * of the line has changed (meaning subsequent lines might need to be
	 * retokenized).
	 * @since jEdit 4.2pre6
	 */
	@Override
	public void setLineContext(TokenMarker.LineContext lineContext)
	{
	} //}}}
}
