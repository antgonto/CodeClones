/*
 * SplashScreen.java - Splash screen
 * Copyright (C) 1998, 2004 Slava Pestov
 * Copyright (C) 2014 Eric Le Lay
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
import javax.swing.*;
import java.awt.*;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;
//}}}

/** The splash screen displayed on startup.
 * @version $Id: SplashScreen.java 24830 2018-02-22 01:21:35Z vampire0 $
 */
public class SplashScreen extends JComponent
{
	private static final long serialVersionUID = 1L;

	//{{{ SplashScreen constructor
	public SplashScreen()
	{
		realSplash = java.awt.SplashScreen.getSplashScreen();
		fm = getFontMetrics(labelFont);
		if(realSplash == null)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			setBackground(Color.white);

			image = getToolkit().getImage(
				getClass().getResource("/org/gjt/sp/jedit/icons/splash.png"));
			MediaTracker tracker = new MediaTracker(this);
			tracker.addImage(image,0);

			try
			{
				tracker.waitForAll();
			}
			catch(Exception e)
			{
				Log.log(Log.ERROR,this,e);
			}
			Dimension screen = getToolkit().getScreenSize(); // sane default
			win = new JWindow();
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			GraphicsDevice gd = gs[0];
			if (gd != null)
			{
				GraphicsConfiguration gconf = gd.getDefaultConfiguration();
				if (gconf != null)
				{
					Rectangle bounds = gconf.getBounds();
					screen = new Dimension(bounds.width, bounds.height);
				}
			}
			Dimension size = new Dimension(image.getWidth(this) + 2,
				image.getHeight(this)+2);
			win.setSize(size);
			win.getContentPane().add(this, BorderLayout.CENTER);
			win.setLocation((screen.width - size.width) / 2,
				(screen.height - size.height) / 2);
			win.validate();
			win.setVisible(true);
		}
		else
		{
			win = null;
			image=null;
		}
	} //}}}

	//{{{ dispose() method
	public void dispose()
	{
		if(realSplash==null)
		{
			win.dispose();
		}
		else
		{
			if(realSplash.isVisible())
			{
				realSplash.close();
			}
		}
	} //}}}

	//{{{ advance() methods
	public synchronized void advance()
	{
		logAdvanceTime(null);
		progress++;
		repaint();

		if(realSplash == null)
		{
			// wait for it to be painted to ensure progress is updated
			// continuously
			try
			{
				wait();
			}
			catch(InterruptedException ie)
			{
				Log.log(Log.ERROR,this,ie);
			}
		}
	}

	public synchronized void advance(String label)
	{
		logAdvanceTime(label);
		progress++;
		this.label = label;
		repaint();

		if(realSplash == null)
		{
			// wait for it to be painted to ensure progress is updated
			// continuously
			try
			{
				wait();
			}
			catch(InterruptedException ie)
			{
				Log.log(Log.ERROR,this,ie);
			}
		}
	} //}}}

	//{{{ logAdvanceTime() method
	private void logAdvanceTime(String label)
	{
		long currentTime = System.currentTimeMillis();
		if (lastLabel != null)
		{
			Log.log(Log.DEBUG, SplashScreen.class,
				lastLabel + ':' + (currentTime - lastAdvanceTime)
				+ "/" + (currentTime - firstAdvanceTime) + "ms");
		}
		if (label != null)
		{
			lastLabel = label;
			lastAdvanceTime = currentTime;

		}
	} //}}}

	//{{{ repaint() method
	@Override
	public void repaint()
	{
		if(realSplash == null)
		{
			// fallback to the default code
			super.repaint();
			return;
		}

		Graphics2D g = realSplash.createGraphics();
		if (g == null)
		{
			Log.log(Log.ERROR, SplashScreen.class,
					"using native splash screen, but can't obtain graphics from it");
			return;
		}

		Dimension size = realSplash.getSize();

		// tell the splash screen the zone to repaint (everything)
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0,0,size.width,size.height);
		g.setPaintMode();

		doPaintContents(g, size);

		realSplash.update();
	} //}}}

	//{{{ paintComponent() method
	@Override
	public synchronized void paintComponent(Graphics g)
	{
		Dimension size = getSize();

		g.drawImage(image,1,1,this);

		doPaintContents(g, size);

		// for the wait() inside advance(...)
		notify();
	} //}}}

	//{{{ doPaintContents() method
	/**
	 *  Code common to the native and swing splash screens
	 */
	private void doPaintContents(Graphics g, Dimension size)
	{
		g.setColor(Color.black);
		g.drawRect(0,0,size.width - 1,size.height - 1);

		g.setColor(Color.white);
		g.fillRect(1,size.height - 1 - PROGRESS_HEIGHT,
			((size.width - 2) * progress) / PROGRESS_COUNT, PROGRESS_HEIGHT);

		g.setColor(Color.black);

		if (label != null)
		{
			int drawOffsetX = (size.width - fm.stringWidth(label)) / 2;
			int drawOffsetY = size.height - 2 - PROGRESS_HEIGHT
					+ (PROGRESS_HEIGHT + fm.getAscent() + fm.getDescent()) / 2;

			paintString(g, label, drawOffsetX, drawOffsetY);
		}

		String version = "version " + jEdit.getVersion();

		int drawOffsetX = (size.width / 2) - (fm.stringWidth(version) / 2);
		int drawOffsetY = size.height - PROGRESS_HEIGHT - fm.getDescent() - 3;

		paintString(g, version, drawOffsetX, drawOffsetY);

	} //}}}

	//{{{ paintString() method
	private void paintString(Graphics g, String version, int drawOffsetX,
				 int drawOffsetY)
	{
		g.setFont( labelFont );

		g.setColor( versionColor1 );
		g.drawString( version, drawOffsetX, drawOffsetY );
		// Draw a highlight effect
		g.setColor( versionColor2 );
		g.drawString( version, drawOffsetX + 1, drawOffsetY + 1 );
	} //}}}

	//{{{ private members
	private final FontMetrics fm;
	private final JWindow win;
	private final Image image;
	private int progress;
	private static final int PROGRESS_HEIGHT = 20;
	private static final int PROGRESS_COUNT = 23;
	private String label;
	private String lastLabel;
	private long firstAdvanceTime = System.currentTimeMillis();
	private long lastAdvanceTime = System.currentTimeMillis();
	private Font labelFont = UIManager.getFont("Label.font").deriveFont(9.8f);
	private Color versionColor1 = new Color(55, 55, 55);
	private Color versionColor2 = new Color(255, 255, 255, 50);
	private java.awt.SplashScreen realSplash;
	//}}}
}
