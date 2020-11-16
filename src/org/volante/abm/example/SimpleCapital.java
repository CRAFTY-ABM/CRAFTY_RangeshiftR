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
 */
package org.volante.abm.example;


import org.volante.abm.data.Capital;

import com.moseph.modelutils.fastdata.NamedArrayIndexSet;
import com.moseph.modelutils.fastdata.NamedIndexSet;


public enum SimpleCapital implements Capital {
	HUMAN(0),
	INFRASTRUCTURE(1),
	ECONOMIC(2),
	NATURAL_GRASSLAND(3),
	NATURAL_FOREST(4),
	NATURAL_CROPS(5),
	NATURE_VALUE(6);

	int											index			= 0;
	public static final NamedIndexSet<Capital>	simpleCapitals	= new NamedArrayIndexSet<Capital>(
																		SimpleCapital.values());

	private SimpleCapital(int index) {
		this.index = index;
	}

	@Override
	public String getName() {
		return toString();
	}

	@Override
	public int getIndex() {
		return index;
	}
}
