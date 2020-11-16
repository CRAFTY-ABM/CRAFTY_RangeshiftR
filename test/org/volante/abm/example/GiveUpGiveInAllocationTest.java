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
package org.volante.abm.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.assembler.AgentAssembler;
import org.volante.abm.agent.assembler.DefaultAgentAssembler;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.fr.VariantProductionFR;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;

/**
 * TODO seemed to depend on execution order > make clean to guarantee determiend
 * start conditions
 * 
 * @author Sascha Holzhauer
 * 
 */
public class GiveUpGiveInAllocationTest extends BasicTestsUtils {
	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(GiveUpGiveInAllocationTest.class);

	@Test
	public void testSimpleAllocation() throws Exception {
		// Make an allocation model etc.
		GiveUpGiveInAllocationModel allocation = new GiveUpGiveInAllocationModel();
		allocation.numTakeovers = "1";
		allocation.numCells = "10";
		allocation.probabilityExponent = 1;
		allocation = persister.roundTripSerialise(allocation);
		RegionalDemandModel demand = new RegionalDemandModel();
		SimpleCompetitivenessModel competition = new SimpleCompetitivenessModel();
		
		// Makes the maths easier if we ignore oversupply
		competition.setRemoveNegative(true);

		// Create the region
		Region r = new Region(allocation, true, competition, demand,
				behaviouralTypes, functionalRolesR1, c11);
		r.initialise(modelData, runInfo, r);

		// Check the agents are in the region correctly
		assertTrue(CollectionUtils.isEqualCollection(functionalRolesR1,
				r.getFunctionalRoles()));

		FunctionalRole forestry = new VariantProductionFR("Forestry", 11,
				forestryProduction, -20, 20);
		forestry.initialise(modelData, runInfo, r);
		Set<FunctionalRole> roles = new HashSet<FunctionalRole>();
		roles.add(forestry);
		roles.add(farmingR1);
		r.clearFunctionalRoles();
		r.addfunctionalRoles(roles);

		logger.info(r.getFunctionalRoles());

		AgentAssembler agentAssemblerR = new DefaultAgentAssembler();
		agentAssemblerR.initialise(modelData, runInfo, r);

		// Start with a forester
		r.setOwnership(
				agentAssemblerR.assembleAgent(null, "Pseudo",
						forestry.getLabel()), c11);

		// Make a perfect cell
		c11.setBaseCapitals(capitals(1, 1, 1, 1, 1, 1, 1));

		// Set the demand to just be for food
		demand.setDemand(services(0, 0, 1, 0));

		// Check that both have full productivity
		assertEqualMaps(services(0, 10, 0, 4), forestry.getExpectedSupply(c11));

		assertEqualMaps(services(1, 0, 7, 4), farmingR1.getExpectedSupply(c11));

		// Farming should have competitiveness proportional to the demand for
		// food
		assertEquals(7, r.getCompetitiveness(farmingR1, c11), 0.0001);

		// And forestry should be 0 - no demand for timber
		assertEquals(0, r.getCompetitiveness(forestry, c11), 0.0001);

		// When we allocate the land initially, the forester should stay there
		r.getAllocationModel().allocateLand(r);
		assertEquals(forestry.getSerialID(), c11.getOwnersFrSerialID());

		// But when we up the demand, the farmer should force the forester out
		// Not at 14 competitiveness
		demand.setDemand(services(0, 0, 2, 0));
		assertEquals(14, r.getCompetitiveness(farmingR1, c11), 0.0001);
		r.getAllocationModel().allocateLand(r);
		assertEquals(forestry.getSerialID(),
				c11.getOwnersFrSerialID());

		// But at 21
		demand.setDemand(services(0, 0, 3, 0));
		assertEquals(21, r.getCompetitiveness(farmingR1, c11), 0.0001);
		r.getAllocationModel().allocateLand(r);

		// And the farmer's taken over:
		assertEquals(farmingR1.getSerialID(), c11.getOwnersFrSerialID());
	}

	@Test
	public void testCreatingIndividualAgentsWithVariation() throws Exception {
		// Models to use
		GiveUpGiveInAllocationModel allocation = new GiveUpGiveInAllocationModel();
		allocation.numTakeovers = "1";
		allocation.numCells = "1";
		allocation.probabilityExponent = 1;
		allocation = persister.roundTripSerialise(allocation);
		RegionalDemandModel demand = new RegionalDemandModel();
		SimpleCompetitivenessModel competition = new SimpleCompetitivenessModel();
		competition.setRemoveNegative(true); // Makes the maths easier if we
												// ignore oversupply

		// Make it hard to give in and up
		FunctionalRole persistentForestry = new VariantProductionFR(
				"Forestry20", forestryProduction, 20, -20);

		// Cells
		Cell c1 = new Cell(0, 0);

		functionalRolesR1.add(persistentForestry);

		// Create the region
		Region r = new Region(allocation, true, competition, demand,
				behaviouralTypes, functionalRolesR1, c1);
		r.initialise(modelData, runInfo, r);

		c1.setBaseCapitals(capitals(1, 1, 1, 1, 1, 1, 1)); // Perfect cell

		demand.setDemand(services(0, 1, 1, 0)); // demands so that neither has
												// much comp advantage

		persistentForestry.initialise(modelData, runInfo, r);

		// Check the supply levels from the forester (10 timber, 4 recreation
		// max)
		assertEqualMaps(services(0, 10, 0, 4),
				persistentForestry.getExpectedSupply(c1));

		// Check supply levels from potential farmer
		assertEqualMaps(services(1, 0, 7, 4), farmingR1.getExpectedSupply(c1));

		assertEquals(10.0, r.getCompetitiveness(persistentForestry, c1), 0.0001);
		assertEquals(7.0, r.getCompetitiveness(farmingR1, c1), 0.0001);

		AgentAssembler agentAssemblerR = new DefaultAgentAssembler();
		agentAssemblerR.initialise(modelData, runInfo, r);

		// Give cell to ordinary forester
		r.setOwnership(
				agentAssemblerR.assembleAgent(c1, "Pseudo",
						forestryR1.getLabel()), c1);

		assertTrue(CollectionUtils.isEqualCollection(functionalRolesR1,
				r.getFunctionalRoles())); // Check the agents are in the region
											// correctly

		// Check competitivenesses (a forester has no value as one already in
		// place; a farmer still has 7.0
		assertEquals(0.0, r.getCompetitiveness(persistentForestry, c1), 0.0001);
		assertEquals(7.0, r.getCompetitiveness(farmingR1, c1), 0.0001);

		// Now set thresholds
		// A normal forester will not give in to a farmer...
		FunctionalRole forestry = new VariantProductionFR("Forestry", 11,
				forestryProduction, -20, 7.25);
		forestry.initialise(modelData, runInfo, r);

		// A normal farmer will not give in to a forester...
		FunctionalRole farmer = new VariantProductionFR("Farming", 12,
				farmingProduction, -20, 10.5);
		farmer.initialise(modelData, runInfo, r);

		Set<FunctionalRole> roles = new HashSet<FunctionalRole>();
		roles.add(persistentForestry);
		roles.add(forestry);
		roles.add(farmer);
		r.clearFunctionalRoles();
		r.addfunctionalRoles(roles);

		// Now if we allocate land, the forester should stay there
		r.getAllocationModel().allocateLand(r);
		assertEquals(forestryR1.getSerialID(), c1.getOwnersFrSerialID());

		// Now replace the forester with a variant forester
		FunctionalRole vForest = runInfo.getPersister().readXML(
				FunctionalRole.class, "xml/VariantForester1.xml",
				r.getPersisterContextExtra());
		vForest.initialise(modelData, runInfo, r);

		roles.clear();
		roles.add(vForest);
		r.addfunctionalRoles(roles);

		r.setOwnership(
				agentAssemblerR.assembleAgent(c1, "Pseudo", vForest.getLabel()),
				c1);

		// Check the base variant agent is as expected
		assertEquals(vForest.getLabel(), c1.getOwnersFrLabel());
		assertEquals(-20, vForest.getMeanGivingUpThreshold(), 0.0001);
		assertEquals(6.2, vForest.getMeanGivingInThreshold(), 0.0001);
		assertEquals("VariantForester1", vForest.getLabel());
		assertEquals(21, vForest.getSerialID());

		// The base agent has thresholds the same as the simple forester,
		// but the giving-in threshold is then drawn from a [6,6.4] Unif dist.
		// So, farmer should take over....
		r.getAllocationModel().allocateLand(r);
		assertEquals(farmingR1.getSerialID(), c1.getOwnersFrSerialID());

		// Now test a variable giving-in farmer:
		// Start with the current farmer, check comp and persistence:
		assertEquals(10.0, r.getCompetitiveness(forestryR1, c1), 0.0001);
		assertEquals(0.0, r.getCompetitiveness(farmingR1, c1), 0.0001);

		// This farmer should not give up or give in:
		r.getAllocationModel().allocateLand(r);
		assertEquals(farmingR1.getSerialID(), c1.getOwnersFrSerialID());

		// Now give the land to a variant farmer with a higher giving up
		// distribution [10,11]
		// This farmer should then give up, and the land will go to the ordinary
		// forester
		FunctionalRole vFarmer = runInfo.getPersister().readXML(
				FunctionalRole.class, "xml/VariantFarmer1.xml",
				r.getPersisterContextExtra());
		vFarmer.initialise(modelData, runInfo, r);

		roles.clear();
		roles.add(vFarmer);
		r.addfunctionalRoles(roles);

		LandUseAgent vfarmerAgent = agentAssemblerR.assembleAgent(c1, "Pseudo",
				vFarmer.getLabel());
		r.setOwnership(vfarmerAgent, c1);

		// Check the base variant agent is as expected
		assertEquals(vFarmer.getSerialID(), c1.getOwnersFrSerialID());
		assertEquals(-20, vFarmer.getMeanGivingUpThreshold(), 0.0001);
		assertEquals(10.5, vFarmer.getMeanGivingInThreshold(), 0.0001);
		assertEquals("VariantFarmer1", vfarmerAgent.getFC().getFR().getLabel());
		assertEquals(22, vFarmer.getSerialID());

		// Check the actual agent has the correct distribution of Giving Up
		// values
		assertTrue(vfarmerAgent
				.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD) >= 10);
		assertTrue(vfarmerAgent
				.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD) <= 11);

		// The farmer should now give up, and the normal forester should take
		// his place
		vfarmerAgent.considerGivingUp();
		r.getAllocationModel().allocateLand(r);
		assertEquals(forestryR1.getSerialID(), c1.getOwnersFrSerialID());
	}
}
