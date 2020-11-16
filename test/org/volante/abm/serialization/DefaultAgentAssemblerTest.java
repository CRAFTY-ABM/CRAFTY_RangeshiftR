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
 * Created by Sascha Holzhauer on 23 Mar 2015
 */
package org.volante.abm.serialization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.example.BasicTestsUtils;

import de.cesr.lara.components.model.impl.LModel;

/**
 * @author Sascha Holzhauer
 *
 */
public class DefaultAgentAssemblerTest extends BasicTestsUtils {

	/**
	 * Test method for {@link org.volante.abm.agent.assembler.DefaultAgentAssembler#assembleAgent(org.volante.abm.data.Cell, int, int)}.
	 */
	@Test
	public void testAssembleAgentCellIntInt() {

		Agent agent = agentAssemblerR1.assembleAgent(c11, 1, 1);

		assertEquals(1, agent.getBC().getType().getSerialID());
		assertEquals("Cognitor", agent.getBC().getType().getLabel());

		assertEquals(
				2.0,
				((LaraBehaviouralComponent) agent.getBC()).getLaraComp()
						.getPreferenceWeight(
								LModel.getModel(r1).getPrefRegistry()
										.get("PreferenceB")), 0.001);

		assertEquals(1, agent.getFC().getFR().getSerialID());
		assertEquals("NC_Cereal", agent.getFC().getFR().getLabel());
	}

	/**
	 * Test method for {@link org.volante.abm.agent.assembler.DefaultAgentAssembler#assembleAgent(org.volante.abm.data.Cell, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAssembleAgentCellStringString() {
		Agent agent = agentAssemblerR1.assembleAgent(c11, "Cognitor",
				"NC_Cereal");

		assertEquals(1, agent.getBC().getType().getSerialID());
		assertEquals("Cognitor", agent.getBC().getType().getLabel());

		assertEquals(
				2.0,
				((LaraBehaviouralComponent) agent.getBC()).getLaraComp()
						.getPreferenceWeight(
								LModel.getModel(r1).getPrefRegistry()
										.get("PreferenceB")), 0.001);

		assertEquals(1, agent.getFC().getFR().getSerialID());
		assertEquals("NC_Cereal", agent.getFC().getFR().getLabel());
	}
}
