/*
 * FileCellRenderer.java - renders table cells for the VFS browser
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999 Jason Ginchereau
 * Portions copyright (C) 2001, 2003 Slava Pestov
 * Portions copyright (C) 2007 Matthieu Casanova
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.browser;

//{{{ Imports
import java.awt.*;
import java.awt.font.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import org.gjt.sp.jedit.io.FavoritesVFS;
import org.gjt.sp.jedit.io.VFSFile;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.manager.BufferManagerImpl;
//}}}

/**
 * Local filesystem VFS.
 * @version $Id: FileCellRenderer.java 25239 2020-04-14 20:00:17Z kpouer $
 */
public class FileCellRenderer extends DefaultTableCellRenderer
{
	public static Icon fileIcon = GUIUtilities.loadIcon(jEdit.getProperty("vfs.browser.file.icon"));
	public static Icon openFileIcon = GUIUtilities.loadIcon(jEdit.getProperty("vfs.browser.open-file.icon"));
	public static Icon dirIcon = GUIUtilities.loadIcon(jEdit.getProperty("vfs.browser.dir.icon"));
	public static Icon openDirIcon = GUIUtilities.loadIcon(jEdit.getProperty("vfs.browser.open-dir.icon"));
	public static Icon filesystemIcon = GUIUtilities.loadIcon(jEdit.getProperty("vfs.browser.filesystem.icon"));
	public static Icon loadingIcon = GUIUtilities.loadIcon(jEdit.getProperty("vfs.browser.loading.icon"));

	//{{{ FileCellRenderer constructor
	public FileCellRenderer()
	{
		plainFont = UIManager.getFont("Tree.font");
		if(plainFont == null)
			plainFont = jEdit.getFontProperty("metal.secondary.font");
		boldFont = plainFont.deriveFont(Font.BOLD);
	} //}}}

	//{{{ getTableCellRendererComponent() method
	@Override
	public Component getTableCellRendererComponent(JTable table,
		Object value, boolean isSelected, boolean hasFocus, 
		int row, int column)
	{
		super.getTableCellRendererComponent(table,value,isSelected,
			hasFocus,row,column);

		if(value instanceof VFSDirectoryEntryTableModel.Entry)
		{
			VFSDirectoryEntryTableModel.Entry entry =
				(VFSDirectoryEntryTableModel.Entry)value;
			VFSFile file = entry.dirEntry;

			setFont(file.getType() == VFSFile.FILE
				? plainFont : boldFont);

			this.isSelected = isSelected;
			this.file = file;

			if(column == 0)
			{
				// while its broken to have a null
				// symlinkPath, some older plugins
				// might...
				String path;
				if(file.getSymlinkPath() == null)
					path = file.getPath();
				else
					path = file.getSymlinkPath();

				// I don't know if we should expose _getBuffer in BufferManager interface
				BufferManagerImpl bufferManager = (BufferManagerImpl) jEdit.getBufferManager();
				openBuffer = bufferManager._getBuffer(path).isPresent();

				setIcon(showIcons
					? getIconForFile(file,entry.expanded,
					openBuffer) : null);
				if (file instanceof FavoritesVFS.Favorite)
				{
					FavoritesVFS.Favorite favorite = (FavoritesVFS.Favorite) file;
					setText(favorite.getLabel());
				}
				else
				{
					setText(file.getName());
				}

				int state;
				if(file.getType() == VFSFile.FILE)
					state = ExpansionToggleBorder.STATE_NONE;
				else if(entry.expanded)
					state = ExpansionToggleBorder.STATE_EXPANDED;
				else
					state = ExpansionToggleBorder.STATE_COLLAPSED;

				setBorder(new ExpansionToggleBorder(
					state,entry.level));
			}
			else
			{
				VFSDirectoryEntryTableModel model = (VFSDirectoryEntryTableModel)table.getModel();
				String extAttr = model.getExtendedAttribute(column);

				openBuffer = false;
				setIcon(null);
				setText(file.getExtendedAttribute(extAttr));
				setBorder(new EmptyBorder(1,1,1,1));
			}
		}

		return this;
	} //}}}

	//{{{ paintComponent() method
	@Override
	public void paintComponent(Graphics g)
	{
		if(!isSelected)
		{
			Color color = file.getColor();

			setForeground(color == null
				? UIManager.getColor("Tree.foreground")
				: color);
		}

		super.paintComponent(g);

		if(openBuffer)
		{
			Font font = getFont();

			FontMetrics fm = getFontMetrics(font);
			int x, y;
			if(getIcon() == null)
			{
				x = 0;
				y = fm.getAscent() + 2;
			}
			else
			{
				x = getIcon().getIconWidth() + getIconTextGap();
				y = Math.max(fm.getAscent() + 2,16);
			}

			Insets border = getBorder().getBorderInsets(this);
			x += border.left;

			g.setColor(getForeground());
			g.drawLine(x,y,x + fm.stringWidth(getText()),y);
		}
	} //}}}

	//{{{ getIconForFile() method
	/**
	 * @since jEdit 4.3pre2
	 */
	public static Icon getIconForFile(VFSFile file,
		boolean expanded)
	{
		// I don't know if we should expose _getBuffer in BufferManager interface
		BufferManagerImpl bufferManager = (BufferManagerImpl) jEdit.getBufferManager();
		return getIconForFile(file,expanded,
			bufferManager._getBuffer(file.getSymlinkPath()).isPresent());
	} //}}}

	//{{{ getIconForFile() method
	public static Icon getIconForFile(VFSFile file,
		boolean expanded, boolean openBuffer)
	{
		if (defaultIcons)
			return file.getDefaultIcon(expanded, openBuffer);
		return file.getIcon(expanded, openBuffer);
	} //}}}

	//{{{ Package-private members
	Font plainFont;
	Font boldFont;
	boolean showIcons;
	private static boolean defaultIcons = true;

	//{{{ propertiesChanged() method
	void propertiesChanged()
	{
		showIcons = jEdit.getBooleanProperty("vfs.browser.showIcons");
		defaultIcons = jEdit.getBooleanProperty("vfs.browser.useDefaultIcons");
	} //}}}

	//{{{ getEntryWidth() method
	int getEntryWidth(VFSDirectoryEntryTableModel.Entry entry,
		Font font, FontRenderContext fontRenderContext)
	{
		String name = entry.dirEntry.getName();
		int width = (int)font.getStringBounds(name,fontRenderContext)
			.getWidth();
		width += ExpansionToggleBorder.ICON_WIDTH
			+ entry.level * ExpansionToggleBorder.LEVEL_WIDTH
			+ 3;
		if(showIcons)
		{
			width += fileIcon.getIconWidth();
			width += getIconTextGap();
		}
		return width;
	} //}}}

	//}}}

	//{{{ Private members
	private boolean openBuffer;
	private boolean isSelected;
	private VFSFile file;
	//}}}

	//{{{ ExpansionToggleBorder class
	static class ExpansionToggleBorder implements Border
	{
		static final Icon COLLAPSE_ICON;
		static final Icon EXPAND_ICON;
		static final int ICON_WIDTH;

		static final int LEVEL_WIDTH = 10;

		static final int STATE_NONE = 0;
		static final int STATE_COLLAPSED = 1;
		static final int STATE_EXPANDED = 2;

		//{{{ ExpansionToggleBorder constructor
		ExpansionToggleBorder(int state, int level)
		{
			this.state = state;
			this.level = level;
		} //}}}

		//{{{ paintBorder() method
		@Override
		public void paintBorder(Component c, Graphics g,
			int x, int y, int width, int height)
		{
			// paint the opposite icon of what the state is
			switch(state)
			{
			case STATE_COLLAPSED:
				EXPAND_ICON.paintIcon(c,g,
					x + level * LEVEL_WIDTH + 2,
					y + (height - EXPAND_ICON.getIconHeight()) / 2);
				break;
			case STATE_EXPANDED:
				COLLAPSE_ICON.paintIcon(c,g,
					x + level * LEVEL_WIDTH + 2,
					y + (height - COLLAPSE_ICON.getIconHeight()) / 2);
				break;
			}
		} //}}}

		//{{{ getBorderInsets() method
		@Override
		public Insets getBorderInsets(Component c)
		{
			return new Insets(1,level * LEVEL_WIDTH
				+ ICON_WIDTH + 4,1,1);
		} //}}}

		//{{{ isBorderOpaque() method
		@Override
		public boolean isBorderOpaque()
		{
			return false;
		} //}}}

		//{{{ isExpansionToggle() method
		public static boolean isExpansionToggle(int level, int x)
		{
			return (x >= level * LEVEL_WIDTH)
				&& (x <= level * LEVEL_WIDTH + ICON_WIDTH);
		} //}}}

		//{{{ Private members
		private final int state;
		private final int level;

		static
		{
			COLLAPSE_ICON = GUIUtilities.loadIcon(jEdit.getProperty("vfs.browser.collapse.icon"));
			EXPAND_ICON = GUIUtilities.loadIcon(jEdit.getProperty("vfs.browser.expand.icon"));
			ICON_WIDTH = Math.max(COLLAPSE_ICON.getIconWidth(), EXPAND_ICON.getIconWidth());
		} //}}}
	} //}}}
}
