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
 * Created by Sascha Holzhauer on 19 Mar 2015
 */
package org.volante.abm.decision;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultSocialLandUseAgent;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.example.BasicTestsUtils;

import de.cesr.lara.components.LaraPerformableBo;
import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.model.impl.LModel;
import de.cesr.lara.toolbox.config.xml.LPersister;

/**
 * 
 * @author Sascha Holzhauer
 *
 */
public class FrCheckThresholdDecisionTriggerTest extends BasicTestsUtils {

	public static boolean indicator = false;

	static public class FRCheckThresholdCompIndicatorPa extends CraftyPa<FRCheckThresholdCompIndicatorPa> implements
			LaraPerformableBo {

		/**
		 * @param key
		 * @param agent
		 * @param preferenceUtilities
		 */
		public FRCheckThresholdCompIndicatorPa(String key,
				LaraBehaviouralComponent agent,
				Map<LaraPreference, Double> preferenceUtilities) {
			super(key, agent, preferenceUtilities);
		}

		/**
		 * @see de.cesr.lara.components.LaraPerformableBo#perform()
		 */
		@Override
		public void perform() {
			indicator = true;
		}

		/**
		 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
		 *      java.util.Map)
		 */
		@Override
		public CraftyPa<FRCheckThresholdCompIndicatorPa> getModifiedBO(
				LaraBehaviouralComponent agent,
				Map<LaraPreference, Double> preferenceUtilities) {
			return new FRCheckThresholdCompIndicatorPa(getKey(), agent,
					preferenceUtilities);
		}

		/**
		 * @see de.cesr.lara.components.LaraBehaviouralOption#getSituationalUtilities(de.cesr.lara.components.decision.LaraDecisionConfiguration)
		 */
		@Override
		public Map<LaraPreference, Double> getSituationalUtilities(
				LaraDecisionConfiguration dConfig) {
			Map<LaraPreference, Double> utilities = this
					.getModifiableUtilities();
			utilities.put(
					LModel.getModel(this.getAgent().getAgent().getRegion())
							.getPrefRegistry().get("Competitiveness"), 1.0);
			utilities.put(
					LModel.getModel(this.getAgent().getAgent().getRegion())
							.getPrefRegistry().get("SocialApproval"), 1.0);
			return utilities;
		}
	}

	Agent agent;
	protected final String FR_CHECK_THRESHOLD_BT_XML = "xml/FrCheckThresholdCompTriggerBehaviouralType.xml";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		indicator = false;

		BehaviouralType laraBT = LPersister.getPersister(r1).readXML(
				BehaviouralType.class, FR_CHECK_THRESHOLD_BT_XML);

		laraBT.initialise(modelData, runInfo, r1);
		agent = new DefaultSocialLandUseAgent(farmingR1, "ID", modelData,
				r1, farmingProduction.copyWithNoise(modelData, null, null),
				0.5, 0.5) {
		};

		laraBT.assignNewBehaviouralComp(agent);
	}

	@Test
	public void testFallBelowThreshold() {
		agent.setProperty(AgentPropertyIds.COMPETITIVENESS, 0.1);
		agent.setProperty(AgentPropertyIds.EXPERIENCE, 0.1);
		agent.getBC().triggerDecisions(agent);

		runInfo.getSchedule().tick();

		assertTrue(indicator);
		// ((LaraBehaviouralComponent)agent.getBC()).

	}

	@Test
	public void testNoChange() {
		agent.setProperty(AgentPropertyIds.COMPETITIVENESS, 0.3);
		agent.setProperty(AgentPropertyIds.EXPERIENCE, 0.1);
		agent.getBC().triggerDecisions(agent);

		runInfo.getSchedule().tick();

		assertFalse(indicator);
	}
}
