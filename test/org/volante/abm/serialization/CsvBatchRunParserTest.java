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
 * Created by Sascha Holzhauer on 15 Sep 2014
 */
package org.volante.abm.serialization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class CsvBatchRunParserTest {

	final String	CSV_FILENAME		= "csv/CsvBatchRunParserTest.csv";
	final String	CSV_FILENAME_SEC	= "csv/CsvBatchRunParserSecondaryTest.csv";

	final String	CSV_FILENAME_SEC_LINKS	= "@@(csv)/CsvBatchRunParserSecondaryTest.csv";

	@Test
	public void testParseDouble() {
		RunInfo rInfo = new RunInfo();

		rInfo.setCurrentRun(42);
		assertEquals(42.0, CsvBatchRunParser.parseDouble("@(" + CSV_FILENAME + ", ColC)", rInfo),
				0.01);
		rInfo.setCurrentRun(10);
		assertEquals(10.5, CsvBatchRunParser.parseDouble("@(" + CSV_FILENAME + ", ColC)", rInfo),
				0.01);
	}

	@Test
	public void testParseInt() {
		RunInfo rInfo = new RunInfo();

		rInfo.setCurrentRun(42);
		assertEquals(42, CsvBatchRunParser.parseInt("@(" + CSV_FILENAME + ", ColC)", rInfo),
				0.01);
		rInfo.setCurrentRun(3);
		assertEquals(3, CsvBatchRunParser.parseInt("@(" + CSV_FILENAME + ", ColC)", rInfo),
				0.01);
	}

	@Test
	public void testParseString() {
		RunInfo rInfo = new RunInfo();

		rInfo.setCurrentRun(42);
		assertEquals("Test42B",
				CsvBatchRunParser.parseString("@(" + CSV_FILENAME + ", ColB)", rInfo));
		rInfo.setCurrentRun(10);
		assertEquals("Test10B",
				CsvBatchRunParser.parseString("@(" + CSV_FILENAME + ", ColB)", rInfo));
	}

	@Test
	public void testParseStringWithin() {
		RunInfo rInfo = new RunInfo();

		rInfo.setCurrentRun(42);
		assertEquals(
				"ParamTextBeforeTest42BParamTextAfter",
				CsvBatchRunParser.parseString("ParamTextBefore@(" + CSV_FILENAME
						+ ", ColB)ParamTextAfter", rInfo));
		rInfo.setCurrentRun(10);
		assertEquals("ParamTextBeforeTest10BParamTextAfter",
				CsvBatchRunParser.parseString("ParamTextBefore@(" + CSV_FILENAME
						+ ", ColB)ParamTextAfter", rInfo));
	}

	@Test
	public void testParseDoubleSecodoaryTable() {
		RunInfo rInfo = new RunInfo();

		rInfo.setCurrentRun(42);
		assertEquals(
				0.1,
				CsvBatchRunParser.parseDouble("@(" + CSV_FILENAME + " ~ " + CSV_FILENAME_SEC
						+ ", ColC)", rInfo),
				0.01);
		rInfo.setCurrentRun(10);
		assertEquals(
				0.2,
				CsvBatchRunParser.parseDouble("@(" + CSV_FILENAME + " ~ " + CSV_FILENAME_SEC
						+ ", ColC)", rInfo),
				0.01);
		rInfo.setCurrentRun(3);
		assertEquals(
				0.3,
				CsvBatchRunParser.parseDouble("@(" + CSV_FILENAME + " ~ " + CSV_FILENAME_SEC
						+ ", ColC)", rInfo),
				0.01);
	}
	
	@Test
	public void testLinks() {
		RunInfo rInfo = new RunInfo();

		rInfo.setCurrentRun(42);

		assertEquals(
				"cellInitialiser/cellInitialiser.xml",
				CsvBatchRunParser.parseString("@@(cellInitialiser/cellInitialiser.xml)", rInfo));

		assertEquals(
				"something/cellInitialiser/cellInitialiser.xml",
				CsvBatchRunParser.parseString("something/@@(cellInitialiser/cellInitialiser.xml)",
						rInfo));

		assertEquals(
				"something/cellInitialiser/cellInitialiser.xml/else",
				CsvBatchRunParser.parseString(
						"something/@@(cellInitialiser/cellInitialiser.xml)/else", rInfo));

		String recentBaseDir = rInfo.getPersister().getBaseDir();
		rInfo.getPersister().setBaseDir(recentBaseDir + "/alternative");
		assertEquals(
				"alternative/cellInitialiser/cellInitialiser.xml",
				CsvBatchRunParser.parseString("@@(cellInitialiser/cellInitialiser.xml)", rInfo));

		assertEquals(
				"id1/cellInitialiser/cellInitialiser.xml",
				CsvBatchRunParser
						.parseString("@@(cellInitialiser/cellInitialiser.xml; ID1)", rInfo));

		rInfo.getPersister().setBaseDir(recentBaseDir);
	}

	@Test
	public void testLinksCsvCombination() {
		RunInfo rInfo = new RunInfo();

		rInfo.setCurrentRun(1);

		assertEquals(
				"RunCellInitialiser",
				CsvBatchRunParser.parseString("@@(cellInitialiser/cellInitialiser.xml; RunCSV)",
						rInfo));
	}

	@Test
	public void testParseLinkDoubleSecodoaryTable() {
		RunInfo rInfo = new RunInfo();

		rInfo.setCurrentRun(42);
		assertEquals(
				1.1,
				CsvBatchRunParser.parseDouble("@(" + CSV_FILENAME + " ~ " + CSV_FILENAME_SEC_LINKS
						+ ", ColC)", rInfo),
				0.01);
		rInfo.setCurrentRun(10);
		assertEquals(
				1.2,
				CsvBatchRunParser.parseDouble("@(" + CSV_FILENAME + " ~ " + CSV_FILENAME_SEC_LINKS
						+ ", ColC)", rInfo),
				0.01);
		rInfo.setCurrentRun(3);
		assertEquals(
				1.3,
				CsvBatchRunParser.parseDouble("@(" + CSV_FILENAME + " ~ " + CSV_FILENAME_SEC_LINKS
						+ ", ColC)", rInfo),
				0.01);
	}
}
