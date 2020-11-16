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
package org.volante.abm.serialization;


import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.serialization.RegionLoader.CellInitialiser;
import org.volante.abm.serialization.transform.IntTransformer;

import com.csvreader.CsvReader;
import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Reads information from a csv file into the given region. Does not consider agent column!
 * 
 * @author dmrust
 * 
 */
public class CellCSVReader implements CellInitialiser {
	@Attribute
	String	csvFile		= "";

	@Attribute(required = false)
	String			xColumn			= "X";
	@Attribute(required = false)
	String			yColumn			= "Y";

	@Element(required = false)
	IntTransformer	xTransformer	= null;

	@Element(required = false)
	IntTransformer	yTransformer	= null;

	Logger	log			= Logger.getLogger(getClass());

	@Override
	public void initialise(RegionLoader rl) throws Exception {
		ModelData data = rl.modelData;

		if (!rl.persister.csvFileOK("RegionLoader", csvFile, rl.getRegion()
				.getPersisterContextExtra(), xColumn, yColumn)) {
			return;
		}
		log.info("Loading cell CSV from " + csvFile);
		ModelRunner.clog("CapitalCSVFile", csvFile);

		CsvReader reader = rl.persister.getCSVReader(csvFile, rl.getRegion()
				.getPersisterContextExtra());

		while (reader.readRecord()) {
			// <- LOGGING
			if (log.isDebugEnabled()) {
				log.debug("Read row " + reader.getCurrentRecord());
			}
			// LOGGING ->

			int x = Integer.parseInt(reader.get(xColumn));
			if (xTransformer != null) {
				x = xTransformer.transform(x);
			}

			int y = Integer.parseInt(reader.get(yColumn));
			if (yTransformer != null) {
				y = yTransformer.transform(y);
			}

			Cell c = rl.getCell(x, y);

			DoubleMap<Capital> adjusted = data.capitalMap();
			c.getBaseCapitals().copyInto(adjusted);

			for (Capital cap : data.capitals) {
				String s = reader.get(cap.getName());
				if (s != null) {
					try {
						adjusted.putDouble(cap, Double.parseDouble(s));
					} catch (Exception exception) {
						log.error("Exception in row " + reader.getCurrentRecord() + " ("
								+ exception.getMessage() + ") for capital " + cap.getName());
					}
				}
			}
			c.setBaseCapitals(adjusted);

		}

		// <- LOGGING
		if (log.isDebugEnabled()) {
			log.debug("Finished reading CSV file "
					+ rl.persister.getFullPath(csvFile, rl.getRegion().getPersisterContextExtra()));
		}
		// LOGGING ->
	}

	@Override
	public String toString() {
		return "CellCSVReader for " + csvFile;
	}
}
