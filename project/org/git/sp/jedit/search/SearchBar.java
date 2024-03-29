/*
 * SearchBar.java - Search & replace toolbar
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2001, 2002 Slava Pestov
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

//{{{ Imports
import java.awt.event.*;
import java.awt.*;
import javax.swing.event.*;
import javax.swing.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.SyntaxUtilities;
//}}}

/**
 * Incremental search tool bar.
 * @version $Id: SearchBar.java 25207 2020-04-12 14:36:56Z kpouer $
 */
public class SearchBar extends JToolBar
{
	//{{{ SearchBar constructor
	public SearchBar(final View view, boolean temp)
	{
		this.view = view;

		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

		setFloatable(false);
		add(Box.createHorizontalStrut(2));

		JLabel label = new JLabel(jEdit.getProperty("view.search.find"));
		add(label);
		
		add(Box.createHorizontalStrut(12));
		
		add(find = new HistoryTextField("find"));
		find.setSelectAllOnFocus(true);

		SyntaxStyle style = SyntaxUtilities.parseStyle(jEdit.getProperty("view.style.invalid"), "Dialog", 12, true);
		errorBackground = style.getBackgroundColor();
		errorForeground = style.getForegroundColor();
		defaultBackground = find.getBackground();
		defaultForeground = find.getForeground();
		Dimension max = find.getPreferredSize();
		max.width = Integer.MAX_VALUE;
		find.setMaximumSize(max);
		find.addKeyListener(new KeyHandler());
		find.addActionListener(e -> find(false));
		find.getDocument().addDocumentListener(new DocumentHandler());

		Insets margin = new Insets(1,1,1,1);

		addSeparator(new Dimension(12, 12));
		
		add(ignoreCase = new JCheckBox(jEdit.getProperty(
			"search.case")));
		ignoreCase.addActionListener(e -> SearchAndReplace.setIgnoreCase(ignoreCase.isSelected()));
		ignoreCase.setMargin(margin);
		ignoreCase.setOpaque(false);
		ignoreCase.setRequestFocusEnabled(false);
		add(Box.createHorizontalStrut(2));
		
		add(regexp = new JCheckBox(jEdit.getProperty(
			"search.regexp")));
		regexp.addActionListener(e -> SearchAndReplace.setRegexp(regexp.isSelected()));
		regexp.setMargin(margin);
		regexp.setOpaque(false);
		regexp.setRequestFocusEnabled(false);
		add(Box.createHorizontalStrut(2));
		
		add(hyperSearch = new JCheckBox(jEdit.getProperty(
			"search.hypersearch")));
		hyperSearch.addActionListener(e ->
		{
			jEdit.setBooleanProperty("view.search.hypersearch.toggle", hyperSearch.isSelected());
			update();
		});
		hyperSearch.setMargin(margin);
		hyperSearch.setOpaque(false);
		hyperSearch.setRequestFocusEnabled(false);

		add(wholeWord = new JCheckBox(jEdit.getProperty(
			"search.word.bar")));
		wholeWord.addActionListener(e -> SearchAndReplace.setWholeWord(wholeWord.isSelected()));
		wholeWord.setMargin(margin);
		wholeWord.setOpaque(false);
		wholeWord.setRequestFocusEnabled(false);
		update();

		//{{{ Create the timer used by incremental search
		timer = new Timer(0,new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent evt)
			{
				if(!incrementalSearch(searchStart,searchReverse))
				{
					if(!incrementalSearch(
						(searchReverse
						? view.getBuffer().getLength()
						: 0),searchReverse))
					{
						// not found at all.
						view.getStatus().setMessageAndClear(
							jEdit.getProperty(
							"view.status.search-not-found"));
					}
				}
			}
		}); //}}}

		// if 'temp' is true, hide search bar after user is done with it
		this.isRemovable = temp;

		setCloseButtonVisibility();
	} //}}}

	//{{{ getField() method
	public HistoryTextField getField()
	{
		return find;
	} //}}}

	//{{{ setHyperSearch() method
	public void setHyperSearch(boolean hyperSearch)
	{
		jEdit.setBooleanProperty("view.search.hypersearch.toggle",hyperSearch);
		this.hyperSearch.setSelected(hyperSearch);
	} //}}}

	//{{{ update() method
	public void update()
	{
		ignoreCase.setSelected(SearchAndReplace.getIgnoreCase());
		regexp.setSelected(SearchAndReplace.getRegexp());
		wholeWord.setSelected(SearchAndReplace.getWholeWord());
		hyperSearch.setSelected(jEdit.getBooleanProperty(
			"view.search.hypersearch.toggle"));
	} //}}}

	//{{{ propertiesChanged() method
	public void propertiesChanged()
	{
		// Option may have been changed
		isRemovable = !jEdit.getBooleanProperty("view.showSearchbar");
		
		Log.log(Log.DEBUG, this, "in SearchBar.propertiesChanged(), isRemovable = " + isRemovable);
		
		setCloseButtonVisibility();
		
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private final View view;
	private final HistoryTextField find;
	private final JCheckBox ignoreCase;
	private final JCheckBox regexp;
	private final JCheckBox hyperSearch;
	private final JCheckBox wholeWord;
	private final Timer timer;
	private boolean wasError;
	private final Color defaultBackground;
	private final Color defaultForeground;
	private final Color errorForeground;
	private final Color errorBackground;
	// close button only there if 'isRemovable' is true
	private RolloverButton close;

	private int searchStart;
	private boolean searchReverse;
	private boolean isRemovable;
	//}}}

	//{{{ find() method
	private void find(boolean reverse)
	{
		timer.stop();

		String text = find.getText();
		//{{{ If nothing entered, show search and replace dialog box
		if(text.isEmpty())
		{
			jEdit.setBooleanProperty("search.hypersearch.toggle",
				hyperSearch.isSelected());
			SearchDialog.showSearchDialog(view,null,SearchDialog.CURRENT_BUFFER);
		} //}}}
		//{{{ HyperSearch
		else if(hyperSearch.isSelected())
		{
			if(isRemovable)
			{
				view.removeToolBar(this);
			}
			else
				find.setText(null);

			SearchAndReplace.setSearchString(text);
			SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
			SearchAndReplace.hyperSearch(view);
		} //}}}
		//{{{ Incremental search
		else
		{
			// on enter, start search from end
			// of current match to find next one
			int start;
			JEditTextArea textArea = view.getTextArea();
			Selection s = textArea.getSelectionAtOffset(
				textArea.getCaretPosition());
			if(s == null)
				start = textArea.getCaretPosition();
			else if(reverse)
				start = s.getStart();
			else
				start = s.getEnd();

			if(!incrementalSearch(start,reverse))
			{
				// not found. start from
				// beginning
				if(!incrementalSearch(reverse
					? view.getBuffer().getLength()
					: 0,reverse))
				{
					// not found at all.
					view.getStatus().setMessageAndClear(
						jEdit.getProperty(
						"view.status.search-not-found"));
				}
				else
				{
					// inform user search restarted
					view.getStatus().setMessageAndClear(
						jEdit.getProperty("view.status.auto-wrap"));
					// beep if beep property set
					if(jEdit.getBooleanProperty("search.beepOnSearchAutoWrap"))
					{
						javax.swing.UIManager.getLookAndFeel().provideErrorFeedback(null); 
					}
				}
			}
		} //}}}
	} //}}}

	//{{{ incrementalSearch() method
	private boolean incrementalSearch(int start, boolean reverse)
	{
		/* For example, if the current fileset is a directory,
		 * C+g will find the next match within that fileset.
		 * This can be annoying if you have just done an
		 * incremental search and want the next occurrence
		 * in the current buffer. */
		SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
		SearchAndReplace.setSearchString(find.getText());
		SearchAndReplace.setReverseSearch(reverse);

		boolean ret = false;
		try
		{
			if(SearchAndReplace.find(view,view.getBuffer(),start,false,reverse))
				ret = true;
		}
		catch(Exception e)
		{
			Log.log(Log.DEBUG,this,e);

			// invalid regexp, ignore
			// return true to avoid annoying beeping while
			// typing a re
			ret = true;
		}
		if (ret)
		{
			if (wasError)
			{
				find.setForeground(defaultForeground);
				find.setBackground(defaultBackground);
				wasError = false;
			}
		}
		else
		{
			if (!wasError)
			{
				find.setForeground(errorForeground);
				find.setBackground(errorBackground);
				wasError = true;
			}
		}


		return ret;
	} //}}}

	//{{{ timerIncrementalSearch() method
	private void timerIncrementalSearch(int start, boolean reverse)
	{
		searchStart = start;
		searchReverse = reverse;

		timer.stop();
		timer.setRepeats(false);
		timer.setInitialDelay(150);
		timer.start();
	} //}}}
	
	//{{{ setCloseButtonVisibility() method
	private void setCloseButtonVisibility()
	{
		if(isRemovable)
		{
			if(close == null)
			{
				close = new RolloverButton(GUIUtilities.loadIcon("closebox.gif"));
				close.addActionListener(e ->
				{
					view.removeToolBar(this);
					view.getEditPane().focusOnTextArea();
				});
				close.setToolTipText(jEdit.getProperty(
					"view.search.close-tooltip"));
			}
			add(close);
		}
		else if(close != null)
			remove(close);
	}
	//}}}

	//}}}

	//{{{ Inner classes

	//{{{ DocumentHandler class
	class DocumentHandler implements DocumentListener
	{
		//{{{ insertUpdate() method
		@Override
		public void insertUpdate(DocumentEvent evt)
		{
			// on insert, start search from beginning of
			// current match. This will continue to highlight
			// the current match until another match is found
			if(!hyperSearch.isSelected())
			{
				int start;
				JEditTextArea textArea = view.getTextArea();
				Selection s = textArea.getSelectionAtOffset(
					textArea.getCaretPosition());
				if(s == null)
					start = textArea.getCaretPosition();
				else
					start = s.getStart();

				timerIncrementalSearch(start,false);
			}
		} //}}}

		//{{{ removeUpdate() method
		@Override
		public void removeUpdate(DocumentEvent evt)
		{
			// on backspace, restart from beginning
			if(!hyperSearch.isSelected())
			{
				String text = find.getText();
				if(!text.isEmpty())
				{
					// don't beep if not found.
					// subsequent beeps are very
					// annoying when backspacing an
					// invalid search string.
					if(regexp.isSelected())
					{
						// reverse regexp search
						// not supported yet, so
						// 'simulate' with restart
						timerIncrementalSearch(0,false);
					}
					else
					{
						int start;
						JEditTextArea textArea = view.getTextArea();
						Selection s = textArea.getSelectionAtOffset(
							textArea.getCaretPosition());
						if(s == null)
							start = textArea.getCaretPosition();
						else
							start = s.getStart();
						timerIncrementalSearch(start,true);
					}
				}
			}
		} //}}}

		//{{{ changedUpdate() method
		@Override
		public void changedUpdate(DocumentEvent evt) {}
		//}}}
	} //}}}

	//{{{ KeyHandler class
	class KeyHandler extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent evt)
		{
			switch(evt.getKeyCode())
			{
			case KeyEvent.VK_ESCAPE:
				if(isRemovable)
				{
					view.removeToolBar(SearchBar.this);
				}
				evt.consume();
				view.getEditPane().focusOnTextArea();
				break;
			case KeyEvent.VK_ENTER:
				if(evt.isShiftDown())
				{
					evt.consume();
					find(true);
				}
				break;
			}
		}
	} //}}}
	//}}}
}
