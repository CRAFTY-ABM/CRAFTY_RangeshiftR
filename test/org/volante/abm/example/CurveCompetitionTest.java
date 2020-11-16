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
package org.volante.abm.example;


import static org.junit.Assert.assertEquals;
import static org.volante.abm.example.SimpleService.FOOD;
import static org.volante.abm.example.SimpleService.HOUSING;
import static org.volante.abm.example.SimpleService.RECREATION;
import static org.volante.abm.example.SimpleService.TIMBER;

import org.junit.Test;

import com.moseph.modelutils.curve.LinearFunction;


public class CurveCompetitionTest extends BasicTestsUtils {
	CurveCompetitivenessModel	comp;

	public void setupModel() throws Exception {
		comp = new CurveCompetitivenessModel();
		comp.curves.put(HOUSING, new LinearFunction(5, 1));
		comp.curves.put(TIMBER, new LinearFunction(4, 2));
		comp.curves.put(FOOD, new LinearFunction(3, 3));
		comp.curves.put(RECREATION, new LinearFunction(2, 4));
		comp = persister.roundTripSerialise(comp);
		r1.setCompetitivenessModel(comp);
		comp.initialise(modelData, runInfo, r1);
	}

	@Test
	public void testCompetitiveness() throws Exception {
		setupModel();
		// Competitiveness should be the curves sampled at the
		// residual demand in a cell
		demandR1.setResidual(c11, services(-1, 0, 1, 2));
		assertEqualMaps(services(-1, 0, 1, 2), demandR1.getResidualDemand(c11));
		// Each bit is supply * marginal utility (which is offset + slope*residual)
		double expected = 1.1 * (5 - 1) +
				1.2 * (4 + 0) + 1.3 * (3 + 3) + 1.4 * (2 + 2 * 4);
		assertEquals("Checking that competitiveness is calculated based on residual",
				expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);
	}

	@Test
	public void testRemovingCurrentSupply() throws Exception {
		setupModel();
		c11.setSupply(services(1, 1, 1, 1));
		assertEqualMaps(services(1, 1, 1, 1), c11.getSupply());
		demandR1.setResidual(c11, services(0, 1, 2, 0));
		assertEqualMaps(services(0, 1, 2, 0), demandR1.getResidualDemand(c11));
		double expected = 1.1 * (5 + 0 * 1) + 1.2 * (4 + 1 * 2) + 1.3 * (3 + 2 * 3) + 1.4
				* (2 + 0 * 4);
		assertEquals(expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);

		comp.removeCurrentLevel = true; // Now residual should be (1,2,3,1)
		expected = 1.1 * (5 + 1 * 1) + 1.2 * (4 + 2 * 2) + 1.3 * (3 + 3 * 3) + 1.4 * (2 + 1 * 4);
		assertEquals(expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);

		c11.setSupply(services(0, 0, 0, 0)); // Now residual is back to (0,1,2,0)
		expected = 1.1 * (5 + 0 * 1) + 1.2 * (4 + 1 * 2) + 1.3 * (3 + 2 * 3) + 1.4 * (2 + 0 * 4);
		assertEquals(expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);
	}

	@Test
	public void testRemovingNegative() throws Exception {
		setupModel();
		demandR1.setResidual(c11, services(1, -1, 2, -3));
		double expected = 1.1 * (5 + 1 * 1) + 1.2 * (4 + -1 * 2) + 1.3 * (3 + 2 * 3) + 1.4
				* (2 + -3 * 4);
		assertEquals(expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);

		comp.removeNegative = true;
		expected = 1.1 * (5 + 1 * 1) + 1.2 * (4 + -1 * 2) + 1.3 * (3 + 2 * 3); // Just loose the
																				// last term as it's
																				// the only negative
																				// one
		assertEquals(expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);
	}

	@Test
	public void testBothAtOnce() throws Exception {
		setupModel();
		c11.setSupply(services(1, 2, 1, 1));
		demandR1.setResidual(c11, services(1, -1, 2, -3));
		double expected = 1.1 * (5 + 1 * 1) + 1.2 * (4 + -1 * 2) + 1.3 * (3 + 2 * 3) + 1.4
				* (2 + -3 * 4);
		assertEquals(expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);

		comp.removeCurrentLevel = true; // Now residual = 2, 1, 3, -2
		expected = 1.1 * (5 + 2 * 1) + 1.2 * (4 + 1 * 2) + 1.3 * (3 + 3 * 3) + 1.4 * (2 + -2 * 4);
		assertEquals(expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);

		comp.removeNegative = true; // Now residual = 1, 1, 3, -2, and ignore last term as that's
									// the negative
		expected = 1.1 * (5 + 2 * 1) + 1.2 * (4 + 1 * 2) + 1.3 * (3 + 3 * 3)
				+ (0 * 1.4 * (2 + -2 * 4));
		assertEquals(expected, comp.getCompetitiveness(demandR1, services(1.1, 1.2, 1.3, 1.4), c11),
				0.00001);
	}
}
