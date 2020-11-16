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
 * 
 * Created by Sascha Holzhauer on 11.03.2014
 */
package org.volante.abm.output;

import org.volante.abm.data.Cell;

/**
 * @author Sascha Holzhauer
 *
 */
public class AgentTypeSerialIdRasterOutputter extends RasterOutputter {

	/**
	 * @see org.volante.abm.serialization.CellToDouble#apply(org.volante.abm.data.Cell)
	 */
	@Override
	public double apply(Cell c) {
		return c.getOwner().getFC().getFR().getSerialID();
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "Agent-SerialID";
	}

	@Override
	public boolean isInt() {
		return true;
	}
}
