/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * CRAFTY is free software: You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * CRAFTY is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * School of Geoscience, University of Edinburgh, Edinburgh, UK
 */
package org.volante.abm.output;


import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;


public class ComponentImageCreator {
	int	height	= 500;
	int	width	= 500;

	public BufferedImage getImage(JComponent toPaint) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		toPaint.setSize(width, height);
		layoutComponent(toPaint);
		toPaint.paint(graphics);
		return image;
	}

	/**
	 * Needed to layout components before painting for the video
	 * 
	 * @param component
	 */
	public static void layoutComponent(Component component) {
		synchronized (component.getTreeLock()) {
			component.doLayout();

			if (component instanceof Container) {
				for (Component child : ((Container) component).getComponents()) {
					layoutComponent(child);
				}
			}
		}
	}

	/*
	 * Create a BufferedImage for Swing components. The entire component will be captured to an
	 * image.
	 * 
	 * @param component Swing component to create image from
	 * 
	 * @return image the image for the given region
	 */
	public static BufferedImage createImage(JComponent component) {
		Dimension d = component.getSize();

		if (d.width == 0 || d.height == 0) {
			d = component.getPreferredSize();
			component.setSize(d);
		}

		Rectangle region = new Rectangle(0, 0, d.width, d.height);
		return ComponentImageCreator.createImage(component, region);
	}

	/*
	 * Create a BufferedImage for Swing components. All or part of the component can be captured to
	 * an image.
	 * 
	 * @param component Swing component to create image from
	 * 
	 * @param region The region of the component to be captured to an image
	 * 
	 * @return image the image for the given region
	 */
	public static BufferedImage createImage(JComponent component, Rectangle region) {
		// Make sure the component has a size and has been layed out.
		// (necessary check for components not added to a realized frame)

		if (!component.isDisplayable()) {
			Dimension d = component.getSize();

			if (d.width == 0 || d.height == 0) {
				d = component.getPreferredSize();
				component.setSize(d);
			}

			layoutComponent(component);
		}

		BufferedImage image = new BufferedImage(region.width, region.height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();

		// Paint a background for non-opaque components,
		// otherwise the background will be black

		if (!component.isOpaque()) {
			g2d.setColor(component.getBackground());
			g2d.fillRect(region.x, region.y, region.width, region.height);
		}

		g2d.translate(-region.x, -region.y);
		component.paint(g2d);
		g2d.dispose();
		return image;
	}

	/*
	 * Create a BufferedImage for AWT components.
	 * 
	 * @param component AWT component to create image from
	 * 
	 * @return image the image for the given region
	 * 
	 * @exception AWTException see Robot class constructors
	 */
	public static BufferedImage createImage(Component component)
			throws AWTException {
		Point p = new Point(0, 0);
		SwingUtilities.convertPointToScreen(p, component);
		Rectangle region = component.getBounds();
		region.x = p.x;
		region.y = p.y;
		return ComponentImageCreator.createImage(region);
	}

	/**
	 * Create a BufferedImage from a rectangular region on the screen. This will include Swing
	 * components JFrame, JDialog and JWindow which all extend from Component, not JComponent.
	 * 
	 * @param region
	 *        region on the screen to create image from
	 * @return image the image for the given region
	 * @exception AWTException
	 *            see Robot class constructors
	 */
	public static BufferedImage createImage(Rectangle region)
			throws AWTException {
		BufferedImage image = new Robot().createScreenCapture(region);
		return image;
	}

	static JComponent getComp(String title, String pane, Color color) {
		JPanel p = new JPanel();
		p.add(new JLabel(pane));
		p.setBorder(new TitledBorder(title));
		p.setBackground(color);
		return p;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Image Test Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(getComp("A", "AAAAA", Color.cyan), BorderLayout.CENTER);
		p.add(getComp("B", "AAAAA", Color.cyan), BorderLayout.WEST);
		p.add(getComp("C", "AAAAA", Color.cyan), BorderLayout.EAST);
		p.add(getComp("D", "AAAAA", Color.cyan), BorderLayout.NORTH);
		p.add(getComp("E", "AAAAA", Color.cyan), BorderLayout.SOUTH);

		p.setPreferredSize(new Dimension(500, 500));

		// frame.add(new JLabel( new ImageIcon( createImage( p, new Rectangle( 0, 0, 500, 500 ) ) )
		// ) );
		frame.add(new JLabel(new ImageIcon(new ComponentImageCreator().getImage(p))));
		frame.setSize(500, 500);

		frame.setVisible(true);
	}
}
