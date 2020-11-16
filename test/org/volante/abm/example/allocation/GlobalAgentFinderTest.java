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
 * Created by Sascha Holzhauer on 7 Jul 2015
 */
package org.volante.abm.example.allocation;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultLandUseAgent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.SimpleAllocationModel;
import org.volante.abm.example.SimpleCompetitivenessModel;
import org.volante.abm.example.StaticPerCellDemandModel;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class GlobalAgentFinderTest extends BasicTestsUtils {

	@Before
	public void setupBasicTestEnvironment() {
		// prevent initialisation of regions etc.
	}

	/**
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings("deprecation")
	@Before
	public void setUp() throws Exception {
		// setup environment with empty region of two cells
		runInfo = new RunInfo();
		persister.setBaseDir("test-data");

		competition = new SimpleCompetitivenessModel();
		allocation = new SimpleAllocationModel();
		((SimpleAllocationModel) allocation).setAgentFinder(new GlobalAgentFinder());

		demandR1 = new StaticPerCellDemandModel();

		c11 = new Cell(1, 1);
		c12 = new Cell(1, 2);
		r1 = new Region(allocation, true, competition, demandR1, behaviouralTypes, functionalRolesR1, c11, c12);
		r1.setID("Region01");
		r1.initialise(modelData, runInfo, r1);
		this.agentAssemblerR1.initialise(modelData, runInfo, r1);
		// allocation.initialise(modelData, runInfo, r1);

		c11.setBaseCapitals(cellCapitalsA);
		c12.setBaseCapitals(cellCapitalsA);

		a1 = new DefaultLandUseAgent("A1", modelData);
		a2 = new DefaultLandUseAgent("A2", modelData);
		
		// cell 1 is suitable for timber
		demandR1.setResidual(c11, services(0, 1, 0, 0));
		// cell 2 is suitable for crops
		demandR1.setResidual(c12, services(0, 0, 1, 0));
		r1.setAvailable(c11);
		r1.setAvailable(c12);
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.example.allocation.GlobalAgentFinder#findAgent(org.volante.abm.data.Cell, int, int)}.
	 */
	@Test
	public void testFindAgentNoAmbulantAgents() {
		// init
		r1.getAllocationModel().allocateLand(r1);
		assertFalse(c11.getOwner().equals(Agent.NOT_MANAGED));
		assertFalse(c11.getOwner().equals(Agent.NOT_MANAGED));
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.example.allocation.GlobalAgentFinder#findAgent(org.volante.abm.data.Cell, int, int)}.
	 */
	@Test
	public void testFindAgentUnsuitableAmbulantAgents() {
		// init
		demandR1.setResidual(c12, services(0, 1, 0, 0));

		LandUseAgent a1 = this.agentAssemblerR1.assembleAgent(null, Integer.MIN_VALUE, 12);
		LandUseAgent a2 = this.agentAssemblerR1.assembleAgent(null, Integer.MIN_VALUE, 12);
		r1.setAmbulant(a1);
		r1.setAmbulant(a2);

		r1.getAllocationModel().allocateLand(r1);

		assertFalse(c11.getOwner().equals(Agent.NOT_MANAGED));
		assertFalse(c11.getOwner() == a1);
		assertFalse(c11.getOwner() == a2);
		assertFalse(c12.getOwner().equals(Agent.NOT_MANAGED));
		assertFalse(c12.getOwner() == a1);
		assertFalse(c12.getOwner() == a2);
	}

	/**
	 * Test method for {@link org.volante.abm.example.allocation.GlobalAgentFinder#findAgent(org.volante.abm.data.Cell, int, int)}.
	 */
	@Test
	public void testFindAgentSuitableAmbulantAgents() {
		// init
		LandUseAgent a1 = this.agentAssemblerR1.assembleAgent(null, Integer.MIN_VALUE, 11);
		LandUseAgent a2 = this.agentAssemblerR1.assembleAgent(null, Integer.MIN_VALUE, 12);
		r1.setAmbulant(a1);
		r1.setAmbulant(a2);

		r1.getAllocationModel().allocateLand(r1);

		assertTrue(c11.getOwner() == a1);
		assertTrue(c12.getOwner() == a2);
	}
}
