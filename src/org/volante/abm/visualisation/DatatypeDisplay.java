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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;


public abstract class DatatypeDisplay<T> extends MaxMinCellDisplay implements Display,
		ActionListener {
	private static final long	serialVersionUID	= -6866841652221484687L;

	@Attribute(name = "initial", required = false)
	String						initialType			= null;
	private JComboBox<String>	controls;

	@Override
	public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
		super.initialise(data, info, region);
		setupControls();
		setTypeName(initialType);
	}

	public void setTypeName(String typeName) {
		if (typeName == null) {
			return;
		}
		setupType(typeName);
		update();
		repaint();
	}

	public abstract void setupType(String type);

	public abstract Collection<String> getNames();

	public void setupControls() {
		controls = new JComboBox<String>();
		for (String s : getNames()) {
			controls.addItem(s);
		}
		if (initialType != null) {
			controls.setSelectedItem(initialType);
		}
		controls.addActionListener(this);
	}

	@Override
	public JComponent getControls() {
		return controls;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setTypeName(controls.getSelectedItem() + "");
	}
}
