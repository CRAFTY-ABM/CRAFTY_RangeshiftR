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

import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultLandUseAgent;
import org.volante.abm.agent.fr.DefaultFR;


public class DefaultFrTest extends BasicTestsUtils {

	protected static final String XML_LOW_INTENSITY_FR = "xml/LowIntensityArableFR.xml";

	SimpleProductionModel	p1	= new SimpleProductionModel();

	@Test
	public void test() {
		DefaultFR p = new DefaultFR("TestAgent", p1, 5, 3);
		DefaultLandUseAgent ag = new DefaultLandUseAgent(p, "TestAgent", modelData, r1);
		r1.setOwnership(ag, c11, c12);
		
		assertEquals(p1, ag.getProductionFunction());
		assertEquals("TestAgent", ag.getID());
		assertEquals(5, ag.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD),
				0.0000001);
		assertEquals(3, ag.getProperty(AgentPropertyIds.GIVING_IN_THRESHOLD),
				0.0000001);
		assertEquals(ag, c11.getOwner());
		assertEquals(ag, c12.getOwner());
		assertEquals(Agent.NOT_MANAGED, c13.getOwner());
		checkSet("Ownership of new agent", ag.getCells(), c11, c12);
	}

	@Test
	public void testDeserealisation() throws Exception {
		DefaultFR p = runInfo.getPersister().readXML(DefaultFR.class,
				XML_LOW_INTENSITY_FR, null);
		p.initialise(modelData, runInfo, null);
		testLowIntensityArableAgent(p);
	}

	public static void testLowIntensityArableAgent(DefaultFR p) {
		SimpleProductionTest
				.testLowIntensityArableProduction((SimpleProductionModel) p
						.getProduction());
		assertEquals(0.5, p.getMeanGivingUpThreshold(), 0.0001);
		assertEquals(1, p.getMeanGivingInThreshold(), 0.0001);
		assertEquals("LowIntensityArable", p.getLabel());
		assertEquals(1, p.getSerialID());
	}

	public static void testHighIntensityArableAgent(DefaultFR p) {
		SimpleProductionTest
				.testHighIntensityArableProduction((SimpleProductionModel) p
						.getProduction());
		assertEquals(0.5, p.getMeanGivingUpThreshold(), 0.0001);
		assertEquals(1, p.getMeanGivingInThreshold(), 0.0001);
		assertEquals("HighIntensityArable", p.getLabel());
		assertEquals(2, p.getSerialID());
	}

	public static void testCommercialForestryAgent(DefaultFR p) {
		SimpleProductionTest
				.testCommercialForestryProduction((SimpleProductionModel) p
						.getProduction());
		assertEquals(0.1, p.getMeanGivingUpThreshold(), 0.0001);
		assertEquals(3, p.getMeanGivingInThreshold(), 0.0001);
		assertEquals("CommercialForestry", p.getLabel());
		assertEquals(3, p.getSerialID());
	}

}
