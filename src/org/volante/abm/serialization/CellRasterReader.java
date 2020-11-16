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
package org.volante.abm.serialization;


import static java.lang.Double.isNaN;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.serialization.RegionLoader.CellInitialiser;

import com.moseph.gis.raster.Raster;
import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Reads information from a raster file into the given region
 * 
 * @author dmrust
 * 
 */
public class CellRasterReader implements CellInitialiser {
	@Attribute(name = "file")
	String	rasterFile	= "";
	@Attribute(name = "capital")
	String	capitalName	= "HUMAN";

	Logger	log			= Logger.getLogger(getClass());

	@Override
	public void initialise(RegionLoader rl) throws Exception {
		ModelData data = rl.modelData;
		log.info("Loading raster for " + capitalName + " from " + rasterFile);
		Raster raster = rl.persister.readRaster(rasterFile, rl.getRegion()
				.getPersisterContextExtra());
		Capital capital = data.capitals.forName(capitalName);
		int cells = 0;
		for (int x = 0; x < raster.getCols(); x++) {
			for (int y = 0; y < raster.getRows(); y++) {
				double val = raster.getValue(y, x);
				int xPos = raster.colToX(x);
				int yPos = raster.rowToY(y);
				if (isNaN(val)) {
					continue;
				}
				cells++;
				if (cells % 10000 == 0) {
					log.debug("Cell: " + cells);
					Runtime r = Runtime.getRuntime();
					log.debug(String.format("Mem: Total: %d, Free: %d, Used: %d\n",
							r.totalMemory(), r.freeMemory(), r.totalMemory() - r.freeMemory()));
				}
				Cell cell = rl.getCell(xPos, yPos);
				DoubleMap<Capital> adjusted = data.capitalMap();
				cell.getBaseCapitals().copyInto(adjusted);
				adjusted.putDouble(capital, val);
				cell.setBaseCapitals(adjusted);
			}
		}
	}
}
