/*
 * DefaultTokenHandler.java - Builds a linked list of Token objects
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
 * Builds a linked list of tokens without any additional processing.
 *
 * @author Slava Pestov
 * @version $Id: DefaultTokenHandler.java 25244 2020-04-15 15:14:49Z kpouer $
 * @since jEdit 4.1pre1
 */
public class DefaultTokenHandler implements TokenHandler
{
	//{{{ reset() method
	/**
	 * Clears the list of tokens.
	 */
	public void init()
	{
		lastToken = firstToken = null;
	} //}}}

	//{{{ getTokens() method
	/**
	 * Returns the first syntax token.
	 * @since jEdit 4.1pre1
	 */
	public Token getTokens()
	{
		return firstToken;
	} //}}}

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
		TokenMarker.LineContext context)
	{
		Token token = createToken(id,offset,length,context);
		if(token != null)
			addToken(token,context);
	} //}}}

	//{{{ getLineContext() method
	/**
	 * The token handler can compare this object with the object
	 * previously given for this line to see if the token type at the end
	 * of the line has changed (meaning subsequent lines might need to be
	 * retokenized).
	 * @since jEdit 4.2pre6
	 */
	public TokenMarker.LineContext getLineContext()
	{
		return lineContext;
	} //}}}

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
		this.lineContext = lineContext;
	} //}}}

	//{{{ Protected members
	protected Token firstToken, lastToken;
	protected TokenMarker.LineContext lineContext;

	//{{{ getParserRuleSet() method
	protected ParserRuleSet getParserRuleSet(TokenMarker.LineContext context)
	{
		while(context != null)
		{
			if(!context.rules.isBuiltIn())
				return context.rules;

			context = context.parent;
		}

		return null;
	} //}}}

	//{{{ createToken() method
	protected Token createToken(byte id, int offset, int length,
		TokenMarker.LineContext context)
	{
		return new Token(id,offset,length,getParserRuleSet(context));
	} //}}}

	//{{{ addToken() method
	protected void addToken(Token token, TokenMarker.LineContext context)
	{
		if(firstToken == null)
		{
			firstToken = lastToken = token;
		}
		else
		{
			lastToken.next = token;
			lastToken = lastToken.next;
		}
	} //}}}

	//}}}
}
