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
 * Created by Sascha Holzhauer on 12 Mar 2015
 */
package org.volante.abm.lara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultLandUseAgent;
import org.volante.abm.agent.bt.CognitiveBC;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.example.BasicTestsUtils;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.decision.LaraDecisionModes;
import de.cesr.lara.components.decision.impl.LDecisionConfiguration;
import de.cesr.lara.components.eventbus.events.LAgentPreprocessEvent;
import de.cesr.lara.components.eventbus.impl.LEventbus;
import de.cesr.lara.components.model.impl.LModel;
import de.cesr.lara.components.preprocessor.LaraBOCollector;
import de.cesr.lara.components.preprocessor.LaraBOPreselector;
import de.cesr.lara.components.preprocessor.LaraBOUtilityUpdater;
import de.cesr.lara.components.preprocessor.LaraPreferenceUpdater;
import de.cesr.lara.components.preprocessor.LaraPreprocessorConfigurator;
import de.cesr.lara.testing.components.preprocessor.LPreprocessorTestUtils;

/**
 * @author Sascha Holzhauer
 *
 */
public class FrCheckerDecisionModeSelectorTest extends BasicTestsUtils {

	static final double THRESHOLD_COMPETITIVENESS = 0.6;

	static final double THRESHOLD_EXPERIENCE = 0.6;

	protected class TestAgent extends DefaultLandUseAgent {
		public TestAgent() {
			super("TestAgent", BasicTestsUtils.modelData);
		}
	}

	Agent agent = null;
	FrCheckDecisionModeSelector modeSelector;
	LEventbus eventbus;
	LaraPreprocessorConfigurator<LaraBehaviouralComponent, CraftyPa<?>> ppConfig;
	LaraDecisionConfiguration dConfig;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.eventbus = LModel.getModel(r1).getLEventbus();
		this.dConfig = new LDecisionConfiguration();
		
		this.agent = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				farmingR1.getLabel());

		// emulate trigger:
		((LaraBehaviouralComponent) this.agent.getBC()).subscribeOnce(dConfig);

		this.ppConfig = LPreprocessorTestUtils
				.getTestPreprocessorConfig();
		
		this.modeSelector = new FrCheckDecisionModeSelector();
		this.modeSelector.thresholdCompetitiveness = THRESHOLD_COMPETITIVENESS;
		this.modeSelector.thresholdExperience = THRESHOLD_EXPERIENCE;
		
		this.ppConfig.setDecisionModeSelector(this.modeSelector);

		((LaraBehaviouralComponent) this.agent.getBC()).getLaraComp()
				.setPreprocessor(ppConfig.getPreprocessor());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.volante.abm.lara.FrCheckDecisionModeSelector#onInternalEvent(de.cesr.lara.components.eventbus.events.LaraEvent)}.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testOnInternalEventImitation() {
		this.agent.setProperty(AgentPropertyIds.COMPETITIVENESS, 0.8);
		this.agent.setProperty(AgentPropertyIds.EXPERIENCE, 0.3);

		this.eventbus.publish(new LAgentPreprocessEvent(this.dConfig));

		assertEquals(LaraDecisionModes.IMITATION, ((CognitiveBC) agent.getBC())
				.getLaraComp()
				.getDecisionData(this.dConfig).getDecider()
				.getDecisionMode());

		assertFalse(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOCollector.class)).isCalled());
		assertFalse(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOPreselector.class)).isCalled());
		assertFalse(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOUtilityUpdater.class)).isCalled());
		assertFalse(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraPreferenceUpdater.class)).isCalled());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnInternalEventExploration() {
		this.agent.setProperty(AgentPropertyIds.COMPETITIVENESS, 0.3);
		this.agent.setProperty(AgentPropertyIds.EXPERIENCE, 0.3);

		this.eventbus.publish(new LAgentPreprocessEvent(this.dConfig));

		assertEquals(LaraDecisionModes.HEURISTICS_EXPLORATION,
				((LaraBehaviouralComponent) agent.getBC()).getLaraComp()
				.getDecisionData(this.dConfig).getDecider()
				.getDecisionMode());

		assertTrue(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOCollector.class)).isCalled());
		assertTrue(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOPreselector.class)).isCalled());
		assertFalse(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOUtilityUpdater.class)).isCalled());
		assertFalse(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraPreferenceUpdater.class)).isCalled());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOnInternalEventDeliberation() {
		this.agent.setProperty(AgentPropertyIds.COMPETITIVENESS, 0.3);
		this.agent.setProperty(AgentPropertyIds.EXPERIENCE, 0.8);

		this.eventbus.publish(new LAgentPreprocessEvent(this.dConfig));

		assertEquals(LaraDecisionModes.DELIBERATIVE,
				((LaraBehaviouralComponent) agent.getBC())
				.getLaraComp().getDecisionData(this.dConfig).getDecider()
				.getDecisionMode());

		assertTrue(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOCollector.class)).isCalled());
		assertTrue(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOPreselector.class)).isCalled());
		assertTrue(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraBOUtilityUpdater.class)).isCalled());
		assertTrue(((LPreprocessorTestUtils.LAbstractTestPpComp<LaraBehaviouralComponent, CraftyPa<?>>) this.ppConfig
				.get(null, LaraPreferenceUpdater.class)).isCalled());
	}
}
