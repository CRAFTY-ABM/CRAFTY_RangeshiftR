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
 * Created by Sascha Holzhauer on 13 Mar 2015
 */
package org.volante.abm.lara;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.assembler.AgentAssembler;
import org.volante.abm.agent.assembler.DefaultAgentAssembler;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.SimpleCapital;
import org.volante.abm.lara.decider.CapitalBasedImitatingFrDeciderFactory;
import org.volante.abm.testutils.CraftyTestUtils;
import org.volante.abm.testutils.CraftyTestUtils.PseudoFR;

import com.moseph.modelutils.fastdata.DoubleMap;

import de.cesr.lara.components.decision.LaraDecider;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.decision.LaraDecisionData;
import de.cesr.lara.components.decision.LaraDecisionModes;
import de.cesr.lara.components.decision.impl.LDecisionConfiguration;

/**
 * @author Sascha Holzhauer
 *
 */
public class ImitatingFrDeciderTest extends BasicTestsUtils {

	static final String XML_DECIDER_POWER_FILENAME = "./xml/CapitalBasedPowerImitatingFrDecider.xml";

	static final String XML_DECIDER_IDENTITY_FILENAME = "./xml/CapitalBasedIdentityImitatingFrDecider.xml";
	
	protected LaraDecider<CraftyPa<?>> decider;

	protected LaraDecisionData<LaraBehaviouralComponent, CraftyPa<?>> ddata;

	protected Region r;
	protected LandUseAgent focal;

	protected LaraDecisionConfiguration dConfig = new LDecisionConfiguration(
			"TestDConfig");

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// setup neighbourhood with different degree of similar capital levels
		c11 = new Cell(1, 1);
		c12 = new Cell(1, 2);
		c21 = new Cell(2, 1); // focal agent's cell
		c22 = new Cell(2, 2);

		r = setupBasicWorld(c11, c12, c21, c22);
		c11.initialise(modelData, runInfo, r);
		c12.initialise(modelData, runInfo, r);
		c21.initialise(modelData, runInfo, r);
		c22.initialise(modelData, runInfo, r);
		
		fillCellWithCapitalValues(c21, 0.5, 0.5, 0.5);
		fillCellWithCapitalValues(c11, 0.0, 0.5, 0.5);
		fillCellWithCapitalValues(c12, 0.2, 0.2, 0.3);

		// assign FR
		PseudoFR fr1 = new CraftyTestUtils.PseudoFR("FR1", 1);
		fr1.assignNewFunctionalComp(this.a1);

		PseudoFR fr2 = new CraftyTestUtils.PseudoFR("FR2", 2);
		fr2.assignNewFunctionalComp(this.a2);

		AgentAssembler agentAssemblerR = new DefaultAgentAssembler();
		agentAssemblerR.initialise(modelData, runInfo, r);

		// Start with a forester
		this.focal = agentAssemblerR.assembleAgent(c21, "Cognitive",
				"NC_Cereal");

		r.setOwnership(this.a1, c11);
		r.setOwnership(this.a1, c12);
		r.setOwnership(this.focal, c21);
		
		// setup imitating FR decider
		this.ddata = ((LaraBehaviouralComponent) this.focal.getBC())
				.getLaraComp().getDecisionData(
				dConfig);

		// load Prototype FRs
		List<FunctionalRole> frPrototypes = new ArrayList<FunctionalRole>();
		frPrototypes.add(fr1);
		frPrototypes.add(fr2);
		r.addfunctionalRoles(frPrototypes);

		this.decider = runInfo
				.getPersister()
				.readXML(CapitalBasedImitatingFrDeciderFactory.class,
						XML_DECIDER_IDENTITY_FILENAME,
						r.getPersisterContextExtra())
				.getDecider((LaraBehaviouralComponent) this.focal.getBC(),
						dConfig);
	}

	/**
	 * 
	 */
	protected void fillCellWithCapitalValues(Cell c, double nature,
			double economic,
			double infrastructure) {
		DoubleMap<Capital> capMap = modelData.capitalMap();
		capMap.add(SimpleCapital.NATURE_VALUE, nature);
		capMap.add(SimpleCapital.ECONOMIC, economic);
		capMap.add(SimpleCapital.INFRASTRUCTURE, infrastructure);
		c.setBaseCapitals(capMap);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		this.decider = null;
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.lara.decider.CapitalBasedImitatingFrDecider#decide()}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDecideIdentity() throws Exception {
		this.decider.decide();
		assertEquals(this.decider.getSelectedBo().getKey(), this.a1.getFC()
				.getFR()
				.getLabel());
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.lara.decider.CapitalBasedImitatingFrDecider#decide()}
	 * .
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDecidePower() throws Exception {
		this.decider = runInfo
				.getPersister()
				.readXML(CapitalBasedImitatingFrDeciderFactory.class,
						XML_DECIDER_POWER_FILENAME, r.getPersisterContextExtra())
				.getDecider((LaraBehaviouralComponent) this.focal.getBC(),
						dConfig);
		this.decider.decide();
		assertEquals(this.decider.getSelectedBo().getKey(), this.a1.getFC()
				.getFR()
				.getLabel());
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.lara.decider.CapitalBasedImitatingFrDecider#getNumSelectableBOs()}
	 * .
	 */
	@Test
	public void testGetNumSelectableBOs() {
		this.decider.decide();
		assertEquals(1, this.decider.getNumSelectableBOs());
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.lara.decider.CapitalBasedImitatingFrDecider#getDecisionMode()}
	 * .
	 */
	@Test
	public void testGetDecisionMode() {
		assertEquals(LaraDecisionModes.IMITATION,
				this.decider.getDecisionMode());
	}
}
