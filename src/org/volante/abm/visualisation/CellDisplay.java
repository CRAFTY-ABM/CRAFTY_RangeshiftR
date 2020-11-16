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
package org.volante.abm.visualisation;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Extent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.Indexed;
import com.moseph.modelutils.fastdata.Named;
import com.moseph.modelutils.fastdata.NamedIndexSet;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


public abstract class CellDisplay extends AbstractDisplay implements KeyListener, MouseListener {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7844559478600001796L;
	BufferedImage				image				= null;
	Extent						extent				= null;

	@Element(required = false)
	Color bgColorCells = new Color(0.0f, 0.0f, 0.0f, 1.0f);

	int							regionHeight		= 0;
	int							regionWidth			= 0;
	Cell[][]					cells				= null;
	Cell						selected			= null;
	CellInfoDisplay				cellInfo			= new CellInfoDisplay();
	int							selectedX			= 0;
	int							selectedY			= 0;
	
	@Attribute(required = false)
	int							legendSize			= 100;
	
	@Attribute(required = false)
	int							legendSquares		= 3;
	Logger						log					= Logger.getLogger(getClass());

	@Attribute(required = false)
	boolean						drawLegend			= true;

	public CellDisplay() {
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
		super.initialise(data, info, region);
		this.extent = region.getExtent();
		regionWidth = extent.getWidth();
		regionHeight = extent.getHeight();
		image = new BufferedImage(regionWidth, regionHeight, BufferedImage.TYPE_INT_RGB);
		cells = new Cell[regionWidth][regionHeight];
		
		//setBackground(Color.magenta);
		setBackground(bgColorCells); 

		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);
	}

	protected int getScreenWidth() {
		return (int) ((double) regionWidth / getWidth() >= (double) regionHeight / getHeight() ?
				getWidth() : regionWidth / ((double) regionHeight / getHeight()));
	}

	protected int getScreenHeight() {
		return (int) ((double) regionWidth / getWidth() < (double) regionHeight / getHeight() ?
				getHeight() : regionHeight / ((double) regionWidth / getWidth()));
	}


	@Override
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, getScreenWidth(), getScreenHeight(), 0, 0, regionWidth,
				regionHeight, this);

		// g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, width, height, this);

		int x = cXtoPX(selectedX);
		int y = cYtoPY(selectedY);
		int w = cellPX();
		int h = cellPY();
		if (selected != null) {
			g.setColor(Color.gray);
			g.drawRect(x - 1, y - 1, w + 2, h + 2);
			g.setColor(Color.black);
			g.drawRect(x - 2, y - 2, w + 4, h + 4);
			g.setColor(Color.red);
			g.drawRect(x - 3, y - 3, w + 6, h + 6);
		} else {
			g.setColor(Color.green);
			g.drawRect(x - 2, y - 2, w + 4, h + 4);
		}
		if (drawLegend) {
			int zx = cXtoIX(selectedX);
			int zy = cYtoIY(selectedY);
			g.setColor(Color.black);
			g.fillRect(0, 0, legendSize + 9, legendSize + 9);
			g.setColor(Color.lightGray);
			g.fillRect(0, 0, legendSize + 7, legendSize + 7);
			g.drawImage(image, 0, 0, legendSize, legendSize, zx - legendSquares,
					zy - legendSquares, zx + legendSquares + 1, zy + legendSquares + 1, null);
			int bo = (int) ((double) legendSquares / (legendSquares * 2 + 1) * legendSize) - 1;
			int bw = (int) (1.0 / (legendSquares * 2 + 1) * legendSize);
			g.setColor(Color.black);
			g.drawRect(bo, bo, bw + 1, bw + 1);
			g.setColor(Color.gray);
			g.drawRect(bo - 1, bo - 1, bw + 3, bw + 3);
		}
	}

	@Override
	public void update() {
		super.update();
		Graphics g = image.getGraphics(); 
		g.setColor(bgColorCells);
		g.fillRect(0, 0, regionWidth, regionHeight);
		for (Cell c : region.getAllCells()) {
			cells[extent.xInd(c.getX())][extent.yInd(c.getY())] = c;
			int x = cXtoIX(c.getX());
			int y = cYtoIY(c.getY());
			try {
				image.setRGB(x, y, getColourForCell(c));
			} catch (ArrayIndexOutOfBoundsException e) {
				log.fatal("Extent: " + extent);
				log.fatal("Couldn't set cell: " + x + ", " + y + ": " + c.getX() + ", " + c.getY());
				throw e;
			}
		}
	
		super.postUpdate();
	}

	public void setSelectedCell(Cell c) {
		selected = c;
		cellInfo.setCell(c);
		if (selected != null) {
			selectedX = c.getX();
			selectedY = c.getY();
		} else {
			cellInfo.setCellXY(selectedX, selectedY);
		}
		repaint();
	}

	// Takes image coordinates and gets the relevant cell
	public void setSelectedCell(int x, int y) {
		if (x >= 0 && x < regionWidth && y >= 0 && y < regionHeight) {
			setSelectedCell(cells[x][y]);
			if (selected == null) {
				log.error("No cell found for " + x + ", " + y);
			}
			selectedX = selected.getX();
			selectedY = selected.getY();
		} else {
			log.warn("Tried to set cell " + x + "," + y + ", with width=" + regionWidth
					+ ", height="
					+ regionHeight);
		}
	}

	public void moveSelection(int dx, int dy) {
		int x = extent.xInd(selectedX + dx);
		int y = extent.yInd(selectedY + dy);
		setSelectedCell(x, y);
		fireCellChanged(selected);
	}

	@Override
	public JComponent getEastSidePanel() {
		JScrollPane scroller = new JScrollPane(cellInfo);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		int height = scroller.getHeight();
		scroller.setPreferredSize(new Dimension(270, height));
		return scroller;
	}

	// Turn cell pixels into image pixels.
	public int cXtoIX(int x) {
		return x - extent.getMinX();
	}

	public int cYtoIY(int y) {
		return regionHeight - 1 - y + extent.getMinY();
	}

	// Turn cell address into screen pixels
	public int cXtoPX(int x) {
		return (int) ((double) cXtoIX(x) * getScreenWidth() / regionWidth) + 1;
	}

	public int cYtoPY(int y) {
		return (int) ((double) cYtoIY(y) * getScreenHeight() / regionHeight);
	}

	public int cellPX() {
		return (int) ((double) getScreenWidth() / regionWidth);
	}

	public int cellPY() {
		return (int) ((double) getScreenHeight() / regionHeight);
	}

	// Turn pixels into cells
	public int pxToC(int x) {
		return (int) ((double) x / getScreenWidth() * regionWidth);
	}

	public int pyToC(int y) {
		return (int) ((1.0 - (double) y / getScreenHeight()) * regionHeight);
	}

	// public int cYtoPY( int y ) { return 0; }

	/**
	 * Returns an int representing the colour for the given cell
	 * 
	 * @param c
	 * @return
	 */
	public abstract int getColourForCell(Cell c);

	public static int floatsToARGB(double a, double r, double g, double b) {
		return (((int) (a * 255) & 0xFF) << 24) | // alpha
				(((int) (r * 255) & 0xFF) << 16) | // red
				(((int) (g * 255) & 0xFF) << 8) | // green
				(((int) (b * 255) & 0xFF) << 0); // blue
	}

	public <T extends Named & Indexed> double getDoubleForString(String name,
			UnmodifiableNumberMap<T> map) {
		@SuppressWarnings("unchecked")
		NamedIndexSet<T> n = (NamedIndexSet<T>) map.getKeys();
		if (!n.contains(name)) {
			log.warn("Bad value asked for: " + name + " got: " + n.names());
			return 0;
		}
		return map.getDouble(n.forName(name));
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_KP_LEFT || key == KeyEvent.VK_LEFT) {
			moveSelection(-1, 0);
		} else if (key == KeyEvent.VK_KP_RIGHT || key == KeyEvent.VK_RIGHT) {
			moveSelection(1, 0);
		} else if (key == KeyEvent.VK_KP_UP || key == KeyEvent.VK_UP) {
			moveSelection(0, 1);
		} else if (key == KeyEvent.VK_KP_DOWN || key == KeyEvent.VK_DOWN) {
			moveSelection(0, -1);
		} else if (e.getKeyChar() == 'u') {
			log.debug("Update");
			update();
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
		int x = pxToC(e.getX());
		int y = pyToC(e.getY());
		setSelectedCell(x, y);
		fireCellChanged(selected);
	}

	@Override
	public void cellChanged(Cell c) {
		setSelectedCell(c);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	public static void main(String[] args) throws Exception {
		Region r = new Region();
		ModelData data = new ModelData();
		Cell sel = new Cell();
		for (int x = 0; x < 50; x++) {
			for (int y = 0; y < 50; y++) {
				Cell c = new Cell(x, y);
				c.initialise(data, null, r);
				if (x == 1 && y == 2) {
					sel = c;
				}
				r.addCell(c);
			}
		}
		Cell c = new Cell(55, 55);
		c.initialise(data, null, r);
		r.addCell(c);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setBackground(Color.cyan);

		CellDisplay ce = new CellDisplay()
		{
			private static final long	serialVersionUID	= 202623092558423513L;

			@Override
			public int getColourForCell(Cell c)
			{
				return floatsToARGB(1d, c.getX() % 10 / 10.0, c.getY() % 10 / 10.0, 0d);
			}
		};
		ce.initialise(null, null, r);
		ce.setSelectedCell(sel);

		JComponent panel = ce.getDisplay();

		
		frame.add(panel);
		frame.setSize(new Dimension(500, 600));
		frame.setVisible(true);
	}
	
	@Override
	public void postTick() {
		update();
		repaint();
		

	}
	
	

}
