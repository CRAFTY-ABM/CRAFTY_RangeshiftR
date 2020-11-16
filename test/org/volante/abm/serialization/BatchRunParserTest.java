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
 * Created by Sascha Holzhauer on 06.05.2014
 */
package org.volante.abm.serialization;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class BatchRunParserTest {

	RunInfo	rInfo;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		rInfo = new RunInfo();
		rInfo.setNumRuns(10);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		rInfo = null;
	}

	@Test
	public void testSingleRexpr() {
		String param = "(5+4)";
		double exp = 9.0;
		assertEquals(exp, BatchRunParser.parseDouble(param, rInfo), 0.0001);
	}

	@Test
	public void testCombinedRexpr() {
		rInfo.getPersister().setBaseDir("test-data");
		String param = "(@(Runs.csv, ArbitraryParam)+4)";
		double exp = 46.0;
		assertEquals(exp, BatchRunParser.parseDouble(param, rInfo), 0.0001);
	}

	/**
	 * Test method for {@link org.volante.abm.serialization.BatchRunParser#parseDouble(java.lang.String, org.volante.abm.schedule.RunInfo)}.
	 */
	@Test
	public void testParseDouble() {
		String param = "rep(5,3)";
		double[] exp = { 5.0, 5.0, 5.0 };

		assertEquals(exp[0], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp[1], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp[2], BatchRunParser.parseDouble(param, rInfo), 0.0001);

		param = "seq(1.0,3.0,1.0)";
		double[] exp2 = { 1.0, 2.0, 3.0 };

		rInfo.setCurrentRun(0);
		assertEquals(exp2[0], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp2[1], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp2[2], BatchRunParser.parseDouble(param, rInfo), 0.0001);

		param = "rep(seq(1.0,3.0,1.0), 2)";
		double[] exp3 = { 1.0, 2.0, 3.0, 1.0, 2.0, 3.0 };

		rInfo.setCurrentRun(0);
		assertEquals(exp3[0], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp3[1], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp3[2], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp3[3], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp3[4], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(exp3[5], BatchRunParser.parseDouble(param, rInfo), 0.0001);

		param = "5|5|5";
		double[] expLine = { 5.0, 5.0, 5.0 };

		rInfo.setCurrentRun(0);
		assertEquals(expLine[0], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(expLine[1], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(expLine[2], BatchRunParser.parseDouble(param, rInfo), 0.0001);

		param = "1.0|2.2|3.0";
		double[] expLine2 = { 1.0, 2.2, 3.0 };

		rInfo.setCurrentRun(0);
		assertEquals(expLine2[0], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(expLine2[1], BatchRunParser.parseDouble(param, rInfo), 0.0001);
		rInfo.setCurrentRun(rInfo.getCurrentRun() + 1);
		assertEquals(expLine2[2], BatchRunParser.parseDouble(param, rInfo), 0.0001);

	}
}
