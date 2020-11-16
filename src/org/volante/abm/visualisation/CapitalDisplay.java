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
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;

import com.moseph.modelutils.fastdata.DoubleMap;


public class CapitalDisplay extends DatatypeDisplay<Capital> implements Display, ActionListener {

	/**
	 * Logger
	 */
	static private Logger		logger				= Logger.getLogger(CapitalDisplay.class);

	private static final long	serialVersionUID	= -5571528784889057798L;

	Capital	capital	= null;

	@Override
	public double getVal(Cell c) {
		if (capital == null) {
			return Double.NaN;
		}
		return c.getEffectiveCapitals().getDouble(capital);
	}

	@Override
	public Collection<String> getNames() {
		Set<String> names = new HashSet<String>();
		for (Capital c : data.capitals) {
			names.add(c.getName());
		}
		return names;
	}

	@Override
	public void setupType(String type) {
		capital = data.capitals.forName(type);
		if (capital == null) {
			logger.warn("Specified capital (" + type
					+ ") for CapitalDisplay is not valid. Using first capital.");
			capital = data.capitals.get(0);
		}
	}

	public static void main(String[] args) throws Exception {

		Region r = new Region();
		ModelData data = new ModelData();
		Capital capital = data.capitals.get(0);
		for (int x = 0; x < 255; x++) {
			for (int y = 0; y < 255; y++) {
				Cell c = new Cell(x, y);
				c.initialise(data, null, r);

				DoubleMap<Capital> adjusted = data.capitalMap();
				c.getBaseCapitals().copyInto(adjusted);
				adjusted.putDouble(capital, x + y);
				c.setBaseCapitals(adjusted);
				r.addCell(c);
			}
		}

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		CapitalDisplay ce = new CapitalDisplay();
		ce.initialType = capital.getName();
		ce.initialise(data, null, r);
		ce.update();

		frame.add(ce.getDisplay());
		frame.setSize(new Dimension(500, 500));
		frame.setVisible(true);
	}
}
