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
package org.volante.abm.output;


import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.serialization.CellToDouble;

public abstract class RasterOutputter extends AbstractOutputter implements CellToDouble
{
	@Attribute(required=false)
	boolean perRegion = false;

	@Attribute(required = false)
	String	doubleFormat	= "0.000";
	
	@Attribute(required = false)
	String nDataString = "-inf";

	DecimalFormat	doubleFmt		= null;

	@Override
	public void initialise() throws Exception {
		super.initialise();
		DecimalFormatSymbols decimalSymbols = new DecimalFormat()
				.getDecimalFormatSymbols();
		decimalSymbols.setDecimalSeparator('.');
		doubleFmt = new DecimalFormat(doubleFormat, decimalSymbols);
	}

	@Override
	public void doOutput( Regions regions )
	{
		if( perRegion) {
			for( Region r : regions.getAllRegions() ) {
				writeRaster( r );
			}
		} else {
			writeRaster( regions );
		}
	}
	
	public void writeRaster( Regions r ) 
	{
		String fn = tickFilename(r);
		try {
			outputs.runInfo.getPersister().regionsToRaster(fn, r, this, isInt(), doubleFmt, nDataString);
		} catch (Exception e) {
			log.error(
					"Couldn't write output raster '" + fn + "': "
							+ e.getMessage(), e);
			e.printStackTrace();
		}
	}

	public boolean isInt() { return false; }
	@Override

	public String getExtension() { return "asc"; }
}
