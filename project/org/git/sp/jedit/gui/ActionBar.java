/*
 * ActionBar.java - For invoking actions directly
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2003 Slava Pestov
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

package org.gjt.sp.jedit.gui;

//{{{ Imports
import org.gjt.sp.jedit.bsh.NameSpace;
import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.event.*;
import javax.swing.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.GenericGUIUtilities;
import org.gjt.sp.util.StandardUtilities;
//}}}

/** Action invocation bar.
 */
public class ActionBar extends JToolBar
{
	//{{{ ActionBar constructor
	public ActionBar(View view, boolean temp)
	{
		this.view = view;
		this.temp = temp;

		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		setFloatable(false);
		add(Box.createHorizontalStrut(2));

		JLabel label = new JLabel(jEdit.getProperty("view.action.prompt"));
		add(label);
		add(Box.createHorizontalStrut(12));
		add(action = new ActionTextField());
		action.setEnterAddsToHistory(false);
		Dimension max = action.getPreferredSize();
		max.width = Integer.MAX_VALUE;
		action.setMaximumSize(max);
		action.addActionListener(e -> invoke());
		action.getDocument().addDocumentListener(new DocumentHandler());

		if(temp)
		{
			close = new RolloverButton(GUIUtilities.loadIcon("closebox.gif"));
			close.addActionListener(e -> view.removeToolBar(this));
			close.setToolTipText(jEdit.getProperty(
				"view.action.close-tooltip"));
			add(close);
		}

		// if 'temp' is true, hide search bar after user is done with it
		this.temp = temp;
	} //}}}

	//{{{ getField() method
	public HistoryTextField getField()
	{
		return action;
	} //}}}

	//{{{ goToActionBar() method
	public void goToActionBar()
	{
		repeatCount = view.getInputHandler().getRepeatCount();
		action.setText(null);
		action.requestFocus();
	} //}}}

	//{{{ Private members

	private static final NameSpace namespace = new NameSpace(
		BeanShell.getNameSpace(),"action bar namespace");

	//{{{ Instance variables
	private final View view;
	private boolean temp;
	private int repeatCount;
	private final HistoryTextField action;
	private CompletionPopup popup;
	private RolloverButton close;
	//}}}

	//{{{ invoke() method
	private void invoke()
	{
		String cmd;
		if(popup != null)
			cmd = popup.list.getSelectedValue();
		else
		{
			cmd = action.getText().trim();
			int index = cmd.indexOf('=');
			if(index != -1)
			{
				action.addCurrentToHistory();
				String propName = cmd.substring(0,index).trim();
				String propValue = cmd.substring(index + 1).trim();
				StringBuilder code = new StringBuilder(128);
				/* construct a BeanShell snippet instead of
				 * invoking directly so that user can record
				 * property changes in macros. */
				if(propName.startsWith("buffer."))
				{
					if(propName.equals("buffer.mode"))
					{
						code.append("buffer.setMode(\"")
							.append(StandardUtilities.charsToEscapes(propValue))
							.append("\");");
					}
					else
					{
						code.append("buffer.setStringProperty(\"")
							.append(StandardUtilities.charsToEscapes(propName.substring("buffer.".length())))
							.append("\",\"")
							.append(StandardUtilities.charsToEscapes(propValue))
							.append("\");");
					}

					code.append("\nbuffer.propertiesChanged();");
				}
				else if(propName.startsWith("!buffer."))
				{
					code.append("jEdit.setProperty(\"")
						.append(StandardUtilities.charsToEscapes(propName.substring(1))) 
						.append("\",\"")
						.append(StandardUtilities.charsToEscapes(propValue)) 
						.append("\");\njEdit.propertiesChanged();");
				}
				else
				{
					code.append("jEdit.setProperty(\"")
						.append(StandardUtilities.charsToEscapes(propName)) 
						.append("\",\"")
						.append(StandardUtilities.charsToEscapes(propValue)) 
						.append("\");\njEdit.propertiesChanged();");
				}

				Macros.Recorder recorder = view.getMacroRecorder();
				if(recorder != null)
					recorder.record(code.toString());
				BeanShell.eval(view, namespace, code.toString());
				cmd = null;
			}
			else if(!cmd.isEmpty())
			{
				String[] completions = getCompletions(cmd);
				if(completions.length != 0)
				{
					cmd = completions[0];
				}
			}
			else
				cmd = null;
		}

		if(popup != null)
		{
			popup.dispose();
			popup = null;
		}

		final String finalCmd = cmd;
		final EditAction act = (finalCmd == null ? null : jEdit.getAction(finalCmd));
		if(temp)
			view.removeToolBar(this);

		SwingUtilities.invokeLater(() ->
		{
			view.getTextArea().requestFocus();
			if(act == null)
			{
				if(finalCmd != null)
				{
					view.getStatus().setMessageAndClear(
						jEdit.getProperty(
						"view.action.no-completions"));
				}
			}
			else
			{
				view.getInputHandler().setRepeatCount(repeatCount);
				view.getInputHandler().invokeAction(act);
			}
		});
	} //}}}

	//{{{ getCompletions() method
	private static String[] getCompletions(String str)
	{
		str = str.toLowerCase();
		String[] actions = jEdit.getActionNames();
		ArrayList<String> returnValue = new ArrayList<>(actions.length);
		for (String act : actions)
		{
			if (act.toLowerCase().contains(str))
				returnValue.add(act);
		}

		return returnValue.toArray(StandardUtilities.EMPTY_STRING_ARRAY);
	} //}}}

	//{{{ complete() method
	private void complete(boolean insertLongestPrefix)
	{
		String text = action.getText().trim();
		String[] completions = getCompletions(text);
		if(completions.length == 1)
		{
			if(insertLongestPrefix)
				action.setText(completions[0]);
		}
		else if(completions.length != 0)
		{
			if(insertLongestPrefix)
			{
				String prefix = MiscUtilities.getLongestPrefix(
					completions,true);
				if(prefix.contains(text))
					action.setText(prefix);
			}

			if(popup != null)
				popup.setModel(completions);
			else
				popup = new CompletionPopup(completions);
			return;
		}

		if(popup != null)
		{
			popup.dispose();
			popup = null;
		}
	} //}}}

	//}}}

	//{{{ Inner classes
	//{{{ DocumentHandler class
	private class DocumentHandler implements DocumentListener
	{
		//{{{ insertUpdate() method
		@Override
		public void insertUpdate(DocumentEvent evt)
		{
			if(popup != null)
				complete(false);
		} //}}}

		//{{{ removeUpdate() method
		@Override
		public void removeUpdate(DocumentEvent evt)
		{
			if(popup != null)
				complete(false);
		} //}}}

		//{{{ changedUpdate() method
		@Override
		public void changedUpdate(DocumentEvent evt) {}
		//}}}
	} //}}}

	//{{{ ActionTextField class
	private class ActionTextField extends HistoryTextField
	{
		boolean repeat;
		boolean nonDigit;

		ActionTextField()
		{
			super("action");
			setSelectAllOnFocus(true);
		}

		@Override
		public boolean getFocusTraversalKeysEnabled()
		{
			return false;
		}

		@Override
		public void processKeyEvent(KeyEvent evt)
		{
			evt = KeyEventWorkaround.processKeyEvent(evt);
			if(evt == null)
				return;

			switch(evt.getID())
			{
			case KeyEvent.KEY_TYPED:
				char ch = evt.getKeyChar();
				if(!nonDigit && Character.isDigit(ch))
				{
					super.processKeyEvent(evt);
					repeat = true;
					repeatCount = Integer.parseInt(action.getText());
				}
				else
				{
					nonDigit = true;
					if(repeat)
					{
						passToView(evt);
					}
					else
						super.processKeyEvent(evt);
				}
				break;
			case KeyEvent.KEY_PRESSED:
				int keyCode = evt.getKeyCode();
				if(evt.isActionKey()
					|| evt.isControlDown()
					|| evt.isAltDown()
					|| evt.isMetaDown()
					|| keyCode == KeyEvent.VK_BACK_SPACE
					|| keyCode == KeyEvent.VK_DELETE
					|| keyCode == KeyEvent.VK_ENTER
					|| keyCode == KeyEvent.VK_TAB
					|| keyCode == KeyEvent.VK_ESCAPE)
				{
					nonDigit = true;
					if(repeat)
					{
						passToView(evt);
						break;
					}
					else if(keyCode == KeyEvent.VK_TAB)
					{
						complete(true);
						evt.consume();
					}
					else if(keyCode == KeyEvent.VK_ESCAPE)
					{
						evt.consume();
						if(popup != null)
						{
							popup.dispose();
							popup = null;
							action.requestFocus();
						}
						else
						{
							if(temp)
								view.removeToolBar(ActionBar.this);
							view.getEditPane().focusOnTextArea();
						}
						break;
					}
					else if((keyCode == KeyEvent.VK_UP
						|| keyCode == KeyEvent.VK_DOWN)
						&& popup != null)
					{
						popup.list.processKeyEvent(evt);
						break;
					}
				}
				super.processKeyEvent(evt);
				break;
			}
		}

		private void passToView(final KeyEvent evt)
		{
			if(temp)
				view.removeToolBar(ActionBar.this);
			view.getTextArea().requestFocus();
			SwingUtilities.invokeLater(() ->
			{
				view.getTextArea().requestFocus();
				view.getInputHandler().setRepeatCount(repeatCount);
				view.getInputHandler().processKeyEvent(evt,
					View.ACTION_BAR, false);
			});
		}

		@Override
		public void addNotify()
		{
			super.addNotify();
			repeat = nonDigit = false;
		}
	} //}}}

	//{{{ CompletionPopup class
	private class CompletionPopup extends JWindow
	{
		CompletionList<String> list;

		//{{{ CompletionPopup constructor
		CompletionPopup(String[] actions)
		{
			super(view);

			setContentPane(new JPanel(new BorderLayout())
			{
				/**
				 * Makes the tab key work in Java 1.4.
				 */
				@Override
				public boolean getFocusTraversalKeysEnabled()
				{
					return false;
				}
			});

			list = new CompletionList<String>(actions);
			list.setVisibleRowCount(8);
			list.addMouseListener(new MouseHandler());
			list.setSelectedIndex(0);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			// stupid scrollbar policy is an attempt to work around
			// bugs people have been seeing with IBM's JDK -- 7 Sep 2000
			JScrollPane scroller = new JScrollPane(list,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			getContentPane().add(scroller, BorderLayout.CENTER);

			GenericGUIUtilities.requestFocus(this,list);

			pack();
			Point p = new Point(0,-getHeight());
			SwingUtilities.convertPointToScreen(p,action);
			setLocation(p);
			setVisible(true);

			KeyListener keyHandler = new KeyHandler();
			addKeyListener(keyHandler);
			list.addKeyListener(keyHandler);
		} //}}}

		//{{{ setModel() method
		void setModel(String[] actions)
		{
			list.setListData(actions);
			list.setSelectedIndex(0);
		} //}}}

		//{{{ MouseHandler class
		private class MouseHandler extends MouseAdapter
		{
			@Override
			public void mouseClicked(MouseEvent evt)
			{
				invoke();
			}
		} //}}}

		//{{{ CompletionList class
		class CompletionList<E> extends JList<E>
		{
			CompletionList(E[] data)
			{
				super(data);
			}

			// we need this public not protected
			@Override
			public void processKeyEvent(KeyEvent evt)	// NOPMD
			{
				super.processKeyEvent(evt);
			}
		} //}}}

		//{{{ KeyHandler class
		private class KeyHandler extends KeyAdapter
		{
			@Override
			public void keyTyped(KeyEvent evt)
			{
				action.processKeyEvent(evt);
			}

			@Override
			public void keyPressed(KeyEvent evt)
			{
				int keyCode = evt.getKeyCode();
				if(keyCode == KeyEvent.VK_ESCAPE)
					action.processKeyEvent(evt);
				else if(keyCode == KeyEvent.VK_ENTER)
					invoke();
				else if(keyCode == KeyEvent.VK_UP)
				{
					int selected = list.getSelectedIndex();
					if(selected == 0)
					{
						list.setSelectedIndex(
							list.getModel().getSize()
							- 1);
						evt.consume();
					}
				}
				else if(keyCode == KeyEvent.VK_DOWN)
				{
					int selected = list.getSelectedIndex();
					if(selected == list.getModel().getSize() - 1)
					{
						list.setSelectedIndex(0);
						evt.consume();
					}
				}
			}
		} //}}}
	} //}}}

	//}}}
}
