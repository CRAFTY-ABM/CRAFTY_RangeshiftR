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
package org.volante.abm.models;


import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Part of an Agent, and used to calculate the agent's production on a given cell
 */
public interface ProductionModel extends Initialisable {
	/**
	 * Calculates production on the given cell, puts it into the supplied NumberMap
	 * 
	 * @param c
	 * @param v
	 */
	public void production(Cell c, DoubleMap<Service> v);

}
