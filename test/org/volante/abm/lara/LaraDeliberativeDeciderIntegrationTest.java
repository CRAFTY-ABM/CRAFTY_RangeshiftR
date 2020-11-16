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
 * Created by Sascha Holzhauer on 4 Jun 2015
 */
package org.volante.abm.lara;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.StaticPerCellDemandModel;

/**
 * @author Sascha Holzhauer
 *
 */
public class LaraDeliberativeDeciderIntegrationTest extends BasicTestsUtils {

	LandUseAgent agent = null;
	LandUseAgent neighbour = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Make a perfect cell
		c12.setBaseCapitals(capitals(1, 1, 1, 1, 1, 1, 1));

		((StaticPerCellDemandModel) r1.getDemandModel()).setDemand(c11,
				services(0, 1, 0, 0));
		((StaticPerCellDemandModel) r1.getDemandModel()).setDemand(c12,
				services(0, 10, 0, 4.1));
		((StaticPerCellDemandModel) r1.getDemandModel()).setDemand(c13,
				services(0, 1, 0, 0));

		// Start with a forester
		agent = agentAssemblerR1.assembleAgent(c12, "Cognitor",
				forestryR1.getLabel());
		agent.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD,
				-Double.MAX_VALUE);
		agent.setProperty(
				AgentPropertyIds.FORBID_GIVING_UP_THRESHOLD_OVERWRITE, 2.0);
		r1.setOwnership(agent, c12);

		// Set up neighbours
		neighbour = agentAssemblerR1.assembleAgent(null, "Cognitor",
				forestryR1.getLabel());
		neighbour.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD,
				-Double.MAX_VALUE);
		neighbour.setProperty(
				AgentPropertyIds.FORBID_GIVING_UP_THRESHOLD_OVERWRITE, 2.0);
		r1.setOwnership(neighbour, c11);
		r1.setOwnership(neighbour, c13);

		// tick to initialise competitiveness etc.
		runInfo.getSchedule().tick();
	}

	@Test
	public void test() {
		// owner of cell c11 initialised as Forester
		assertEquals(forestryR1.getSerialID(), c12.getOwnersFrSerialID());

		// set demand towards Cereal
		((StaticPerCellDemandModel) r1.getDemandModel()).setDemand(c12,
				services(0, 0, 10, 0));

		// Since there is no demand for timber, FR check decision should be
		// triggered
		runInfo.getSchedule().tick();

		// Agent should remain on the cell:
		assertEquals(agent, c12.getOwner());
		// Agent should choose the FR producing demanded service (Commercial
		// Cereal)
		assertEquals("C_Cereal", c12.getOwnersFrLabel());


		// set FR back to forestry to trigger FR check:
		forestryR1.assignNewFunctionalComp(agent);
		// giving up was overwritten during assignment:
		agent.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD,
				-Double.MAX_VALUE);

		assertEquals(forestryR1.getLabel(), c12.getOwnersFrLabel());

		// Increase social appraisal towards NC_Cereal
		r1.getFunctionalRoleMapByLabel().get("NC_Cereal")
				.assignNewFunctionalComp(neighbour);

		runInfo.getSchedule().tick();

		// Agent should choose Non-Commercial Cereal
		assertEquals("NC_Cereal", c12.getOwnersFrLabel());
	}
}
