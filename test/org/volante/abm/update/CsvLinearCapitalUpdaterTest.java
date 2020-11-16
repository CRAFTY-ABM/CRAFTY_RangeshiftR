/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 10 Nov 2015
 */
package org.volante.abm.update;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;

import com.csvreader.CsvReader;
import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * @author Sascha Holzhauer
 *
 */
public class CsvLinearCapitalUpdaterTest extends BasicTestsUtils {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(CsvLinearCapitalUpdaterTest.class);
	
	static final String CSV_LINEAR_UPDATER_XML_FILE = "xml/CsvLinearCapitalUpdater.xml";

	static final String CSV_LINEAR_UPDATER_RESULT1 = "csv/LinearCapitalOperandsRegion1_ResultTick1.csv";
	static final String CSV_LINEAR_UPDATER_RESULT2 = "csv/LinearCapitalOperandsRegion1_ResultTick2.csv";
	CsvLinearCapitalUpdater updater;
	Region r;
	
	@Before
	public void setUp() throws Exception {
		// required since cells may not have been initialised for the test
		c11 = new Cell(1, 1);
		c12 = new Cell(1, 2);
		c21 = new Cell(2, 1);
		c22 = new Cell(2, 2);
		
		try {
			updater = persister.readXML(CsvLinearCapitalUpdater.class, CSV_LINEAR_UPDATER_XML_FILE, null);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Test
	public void testGeneralOperation() throws Exception {

		DoubleMap<Capital> initialCapital = new DoubleMap<>(modelData.capitals, 1.0);
		r = setupWorldWithUpdater(false, 2000, updater, c11, c12, c21, c22);
		c11.setBaseCapitals(initialCapital);
		c12.setBaseCapitals(initialCapital);
		c21.setBaseCapitals(initialCapital);
		c22.setBaseCapitals(initialCapital);

		updater = persister.roundTripSerialise(updater);


		runInfo.getSchedule().tick();
		checkRegionCells(r, CSV_LINEAR_UPDATER_RESULT1);
		runInfo.getSchedule().tick();
		checkRegionCells(r, CSV_LINEAR_UPDATER_RESULT2);
	}

	/**
	 * Test interplay with other (effective) capital level affecting means.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testYearlyFilenameEffectiveCapitalsRequired() throws Exception {
		DoubleMap<Capital> initialCapital = new DoubleMap<>(modelData.capitals, 1.0);
		r = setupWorldWithUpdater(true, 2000, updater, c11, c12, c21, c22);
		c11.setBaseCapitals(initialCapital);
		c12.setBaseCapitals(initialCapital);
		c21.setBaseCapitals(initialCapital);
		c22.setBaseCapitals(initialCapital);

		runInfo.getSchedule().tick();
		checkRegionCells(r, CSV_LINEAR_UPDATER_RESULT1);
		runInfo.getSchedule().tick();
		checkRegionCells(r, CSV_LINEAR_UPDATER_RESULT2);
	}

	public Region setupWorldWithUpdater(boolean requiresEffectiveCapitalData, int year, AbstractUpdater updater,
			Cell... cells) throws Exception {
		Region r = new Region(cells);
		if (requiresEffectiveCapitalData) {
			r.setRequiresEffectiveCapitalData();
		}
		setupBasicWorld(r, cells);

		updater.initialise(modelData, runInfo, r);
		runInfo.getSchedule().register(updater);
		runInfo.getSchedule().setStartTick(year);
		return r;
	}

	public void checkRegionCells(Region r, String csvFile) throws IOException {
		CsvReader target = runInfo.getPersister().getCSVReader(csvFile, r.getPersisterContextExtra());
		while (target.readRecord()) {
			Cell cell = r.getCell(Integer.parseInt(target.get("X")), Integer.parseInt(target.get("Y")));
			for (Capital c : modelData.capitals) {
				if (target.get(c.getName()) != null) {
					double exp = Double.parseDouble(target.get(c.getName()));
					double got = cell.getEffectiveCapitals().getDouble(c);
					assertEquals("Capital " + c.getName(), exp, got, 0.00001);
					logger.info("Got: " + got + ", Exp: " + exp + " for " + c.getName() + " on " + cell);
				}
			}
		}
	}

}
