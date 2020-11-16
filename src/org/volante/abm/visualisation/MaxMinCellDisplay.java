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


import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Cell;


public abstract class MaxMinCellDisplay extends CellDisplay {
	private static final long	serialVersionUID	= 1249078765593600128L;

	@Attribute(required = false)
	boolean						updateMaxMin		= true;
	Color						startCol			= Color.black;
	Color						endCol				= Color.cyan;
	Color						nanCol				= Color.yellow;
	float[]						start				= startCol.getColorComponents(null);
	float[]						end					= endCol.getColorComponents(null);
	double						min					= 0;
	double						max					= 1;
	Legend						legend				= new Legend();

	@Override
	public void update() {
		if (updateMaxMin) {
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			for (Cell c : region.getAllCells()) {
				double v = getVal(c);
				min = min(min, v);
				max = max(max, v);
			}
		}
		legend.updateInfo(min, max, startCol, endCol);
		super.update();
	}

	public abstract double getVal(Cell c);

	@Override
	public int getColourForCell(Cell c) {
		double v = getVal(c);
		if (Double.isNaN(v)) {
			return nanCol.getRGB();
		}
		double val = (getVal(c) - min) / (max - min);
		val = max(0, min(val, 1));
		return interpolate(start, end, (float) val);
	}

	public int interpolate(float[] start, float[] end, float amount) {
		float nAmount = 1 - amount;
		float[] newCol = new float[start.length];
		for (int i = 0; i < start.length; i++) {
			newCol[i] = start[i] * nAmount + end[i] * amount;
		}
		return floatsToARGB(1, newCol[0], newCol[1], newCol[2]);
	}

	public void setStartColour(Color s) {
		startCol = s;
		start = startCol.getColorComponents(null);
	}

	public void setEndColour(Color e) {
		endCol = e;
		end = endCol.getColorComponents(null);
	}

	public class Legend extends JPanel {
		private static final long	serialVersionUID	= 969929190002244801L;

		JPanel	startPan	= new JPanel();
		JPanel	endPan		= new JPanel();
		JLabel	startNum	= new JLabel("0.0");
		JLabel	endNum		= new JLabel("0.0");

		public Legend() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			startPan.setPreferredSize(new Dimension(50, 20));
			startPan.setMaximumSize(new Dimension(50, 20));
			endPan.setPreferredSize(new Dimension(50, 20));
			endPan.setMaximumSize(new Dimension(50, 20));
			startPan.setAlignmentX(1);
			startNum.setAlignmentX(1);
			endPan.setAlignmentX(1);
			endNum.setAlignmentX(1);
			add(Box.createHorizontalGlue());
			Box start = new Box(BoxLayout.X_AXIS);
			start.add(startNum);
			start.add(Box.createHorizontalStrut(5));
			start.add(startPan);
			add(start);
			add(Box.createHorizontalStrut(20));
			add(new JLabel(" to "));
			add(Box.createHorizontalStrut(20));
			Box end = new Box(BoxLayout.X_AXIS);
			end.add(endPan);
			end.add(Box.createHorizontalStrut(5));
			end.add(endNum);
			add(end);
			add(Box.createHorizontalGlue());
			setBorder(new TitledBorder(new EtchedBorder(), "Key"));
		}

		public void updateInfo(double min, double max, Color start, Color end) {
			startPan.setBackground(start);
			endPan.setBackground(end);
			startNum.setText(min + "");
			endNum.setText(max + "");
		}
	}

	@Override
	public JComponent getControls() {
		return null;
	}

	@Override
	public JComponent getLegend() {
		return legend;
	}

}
