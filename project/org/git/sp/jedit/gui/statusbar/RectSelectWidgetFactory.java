/*
 * RectSelectWidgetFactory.java - The rectangular selection widget service
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2008 Matthieu Casanova
 * Portions Copyright (C) 2001, 2004 Slava Pestov
 * Portions copyright (C) 2001 Mike Dillon
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

package org.gjt.sp.jedit.gui.statusbar;

//{{{ Imports
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;
//}}}

/**
 * @author Matthieu Casanova
 * @since jEdit 4.3pre14 
 */
public class RectSelectWidgetFactory implements StatusWidgetFactory
{
    //{{{ getWidget() class
    @Override
    public Widget getWidget(View view)
    {
	    Widget rect = new RectSelectWidget(view);
	    return rect;
    } //}}}

    //{{{ RectSelectWidget class
    private static class RectSelectWidget implements Widget
    {
	    private final JLabel rectSelect;
	    private final View view;
	    RectSelectWidget(final View view)
	    {
		    rectSelect = new ToolTipLabel();
		    rectSelect.setHorizontalAlignment(SwingConstants.CENTER);
		    rectSelect.setToolTipText(jEdit.getProperty("view.status.rect-tooltip"));
		    this.view = view;
		    rectSelect.addMouseListener(new MouseAdapter()
		    {
			    @Override
			    public void mouseClicked(MouseEvent evt)
			    {
				    JEditTextArea textArea = view.getTextArea();
				    if (textArea != null)
				    	textArea.toggleRectangularSelectionEnabled();
			    }
		    });
	    }
	    
	    @Override
	    public JComponent getComponent()
	    {
		    return rectSelect;
	    }
	    
	    @Override
	    public void update()
	    {
		    JEditTextArea textArea = view.getTextArea();
		    if (textArea != null)
		    {
			    if (textArea.isRectangularSelectionEnabled())
			    {
				    rectSelect.setText("R");
				    rectSelect.setEnabled(true);
			    }
			    else
			    {
				    rectSelect.setText("r");
				    rectSelect.setEnabled(false);
			    }
		    }
	    }
	    
            @Override
	    public void propertiesChanged()
	    {
		    // retarded GTK look and feel!
		    Font font = new JLabel().getFont();
		    //UIManager.getFont("Label.font");
		    FontMetrics fm = rectSelect.getFontMetrics(font);
		    Dimension dim = new Dimension(
						  Math.max(fm.charWidth('r'),fm.charWidth('R')) + 1,
						  fm.getHeight());
		    rectSelect.setPreferredSize(dim);
		    rectSelect.setMaximumSize(dim);
		    
	    }
    } //}}}
}
