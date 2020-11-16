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


import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


public class DoubleMapTextDisplay extends JPanel implements DoubleMapDisplay {
	private static final long	serialVersionUID	= 3424487388182309007L;

	Map<Object, JLabel>			displays			= new HashMap<Object, JLabel>();

	public DoubleMapTextDisplay() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public DoubleMapTextDisplay(String title) {
		this();
		setBorder(new TitledBorder(title));
	}

	@Override
	public JComponent getDisplay() {
		return this;
	}

	@Override
	public void setMap(Map<?, ? extends Number> map) {
		for (Object t : map.keySet()) {
			if (!displays.containsKey(t)) {
				addItem(t, map.get(t).doubleValue());
			}
			displays.get(t).setText(format(map.get(t).doubleValue()));
		}
	}

	public void addItem(Object item, double val) {
		Box b = new Box(BoxLayout.X_AXIS);
		JLabel lab = new JLabel(item.toString() + ": ");
		lab.setPreferredSize(new Dimension(170, 15));
		b.add(lab);
		JLabel disp = new JLabel(format(val));
		disp.setPreferredSize(new Dimension(80, 15));
		disp.setMinimumSize(new Dimension(80, 15));
		b.add(disp);
		displays.put(item, disp);
		b.setAlignmentX(1);
		add(b);
		invalidate();
	}

	@Override
	public void clear() {
		for (JLabel l : displays.values()) {
			l.setText("?");
		}
	}

	public String format(double d) {
		return String.format("%7.4f", d);
	}
}
