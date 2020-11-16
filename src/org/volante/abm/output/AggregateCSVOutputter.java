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
package org.volante.abm.output;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public abstract class AggregateCSVOutputter extends TableOutputter<Region> {

	@Attribute(required = false)
	String			doubleFormat	= "0.000";

	protected DecimalFormat doubleFmt = null;

	@Attribute(required = false)
	boolean			addTick			= true;

	@Attribute(required = false)
	boolean			addRegion		= true;

	@Override
	public void initialise() throws Exception {
		super.initialise();

		DecimalFormatSymbols decimalSymbols = new DecimalFormat()
				.getDecimalFormatSymbols();
		decimalSymbols.setDecimalSeparator('.');
		doubleFmt = new DecimalFormat(doubleFormat, decimalSymbols);

		if (addRegion) {
			addColumn(new RegionColumn());
		}

		if (addTick) {
			addColumn(new TickColumn<Region>());
		}
	}

	/**
	 * @see org.volante.abm.output.TableOutputter#getData(org.volante.abm.data.Regions)
	 */
	@Override
	public Iterable<Region> getData(Regions r) {
		return r.getAllRegions();
	}

	@Override
	public boolean filePerTick() {
		return false;
	}

	public static class RegionColumn implements TableColumn<Region> {
		@Override
		public String getHeader() {
			return "Region";
		}

		@Override
		public String getValue(Region r, ModelData data, RunInfo info, Regions rs) {
			return r.getID();
		}
	}
}
