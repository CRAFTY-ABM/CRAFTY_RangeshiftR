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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;


public abstract class AbstractDisplay extends JPanel implements Display {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7141999176982745738L;

	@Attribute
	String						title				= "Unknown";

	@Element(required = false)
	Color bgColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);

	@Override
	public void postTick() {
		update();
	}

	@Override
	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}

	protected Logger	log	= Logger.getLogger(getClass());

	public JComponent getControls() {
		return null;
	}

	public JComponent getLegend() {
		return null;
	}

	public JComponent getEastSidePanel() {
		return null;
	}

	public JComponent getMainPanel() {
		return this;
	}

	List<Display>			cellListeners	= new ArrayList<Display>();

	protected ModelData		data;
	protected RunInfo		info;
	protected Regions		region;

	protected ModelDisplays	modelDisplays;

	@Override
	public JComponent getDisplay() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(getMainPanel(), BorderLayout.CENTER);
		JComponent controls = getControls();
		if (controls != null) {
			panel.add(controls, BorderLayout.NORTH);
		}

		JComponent legend = getLegend();
		if (legend != null) {
			panel.add(legend, BorderLayout.SOUTH);
		}

		JComponent p = getEastSidePanel();
		if (p != null) {
			panel.add(p, BorderLayout.EAST);
		}

		panel.setBackground(bgColor);

		return panel;
	}

	@Override
	public void paint(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);
	}

	@Override
	public void addCellListener(Display d) {
		if (d != this) {
			cellListeners.add(d);
		}
	}

	@Override
	public void cellChanged(Cell c) {
	}

	@Override
	public void fireCellChanged(Cell c) {
		for (Display d : cellListeners) {
			d.cellChanged(c);
		}
	}

	@Override
	public void setModelDisplays(ModelDisplays d) {
		this.modelDisplays = d;
	}

	@Override
	public void update() {
		// Nothing to do here

 	}

	public void postUpdate() {
		this.invalidate();
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
		this.data = data;
		this.info = info;
		this.region = region;
	}
}
