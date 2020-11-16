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
package org.volante.abm.update;


import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.update.AgentTypeUpdater.CapitalUpdateFunction;


/**
 * Expresses change of value as a proportion of difference with top value (for +ve numbers) or
 * bottom value (for -ve numbers)
 * 
 * @author dmrust
 * 
 */
public class ProportionalChangeFunction implements CapitalUpdateFunction {
	Capital	capital		= null;
	@Attribute(required = false)
	double	top			= 1;
	@Attribute(required = false)
	double	bottom		= 0;
	@Attribute()
	double	change		= 0;
	@Attribute
	String	capitalName	= "";

	public ProportionalChangeFunction() {
	};

	public ProportionalChangeFunction(Capital c, double change) {
		this.capital = c;
		this.change = change;
	}

	public ProportionalChangeFunction(Capital c, double change, double top, double bottom) {
		this(c, change);
		this.top = top;
		this.bottom = bottom;
	}

	/**
	 * TODO This function did nothing. What should it do?
	 * 
	 * Applies the function to the cell. If you want to do something more complex (i.e. involving
	 * cell values) override this.
	 */
	@Override
	public void apply(Cell c) {
		// nothing
	}

	/**
	 * The actual update function. If you want to change the calculation, override this.
	 * 
	 * @param value
	 * @return
	 */
	public double function(double value) {
		// e.g. top = 0.8, value = 0.4, change = 0.5 -> 0.4 + (0.8-0.4)*0.5 -> 0.6
		if (change > 0) {
			return value + (top - value) * change;
		}
		// e.g. bottom = 0.2, value = 0.6, change = -0.5 -> 0.6 - (0.2-0.6) * (-0.5) -> 0.6 -
		// (-0.4*-0.5) -> 0.4
		if (change < 0) {
			return value - (bottom - value) * change;
		}
		return value;
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		if (capital == null) {
			capital = data.capitals.forName(capitalName);
		}
	}
}