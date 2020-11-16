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
 * Created by Sascha Holzhauer on 20 Oct 2015
 */
package org.volante.abm.example;

import static org.junit.Assert.assertEquals;
import static org.volante.abm.example.SimpleService.FOOD;
import static org.volante.abm.example.SimpleService.HOUSING;
import static org.volante.abm.example.SimpleService.RECREATION;
import static org.volante.abm.example.SimpleService.TIMBER;

import org.junit.Test;
import org.volante.abm.data.Service;

import com.moseph.modelutils.curve.LinearFunction;
import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * @author Sascha Holzhauer
 *
 */
public class NormalisedCurveCompetitivenessModelTest extends BasicTestsUtils {
	NormalisedCurveCompetitivenessModel	comp;
	StaticPerCellDemandModel			demand;

	public void setupModel() throws Exception {
		comp = new NormalisedCurveCompetitivenessModel();
		comp.curves.put(HOUSING, new LinearFunction(5, 1));
		comp.curves.put(TIMBER, new LinearFunction(4, 2));
		comp.curves.put(FOOD, new LinearFunction(3, 3));
		comp.curves.put(RECREATION, new LinearFunction(2, 4));
		comp = persister.roundTripSerialise(comp);
		r1.setCompetitivenessModel(comp);
		comp.initialise(modelData, runInfo, r1);

		demand = new StaticPerCellDemandModel();
		demand.initialise(modelData, runInfo, r1);
		r1.setDemandModel(demand);
	}

	@Test
	public void testCompetitiveness() throws Exception {
		setupModel();
		// set demand:
		demand.setDemand(c11, services(18, 5, 0, 0));

		// get average per-cell demand:
		DoubleMap<Service> avgDemands = r1.getDemandModel().getAveragedPerCellDemand();

		// Competitiveness should be the curves sampled at the
		// residual demand in a cell
		demand.setResidual(c11, services(18, 5, 0, 0));

		assertEqualMaps(services(18, 5, 0, 0), demand.getResidualDemand(c11));
		assertEqualMaps(services(18.0 / 9, 5.0 / 9, 0, 0), avgDemands);

		// Each bit is supply * marginal utility (which is offset + slope*residual)
		double expected = (1.1 / 2) * (5 + 18.0 / 2) +
 (1.2 / (5.0 / 9)) * (4 + 10.0 / (5.0 / 9));
		assertEquals("Checking that competitiveness is calculated based on residual",
				expected, comp.getCompetitiveness(demand, services(1.1, 1.2, 0, 0), c11),
				0.00001);
	}

}
