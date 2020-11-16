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
 * Created by Sascha Holzhauer on 18 Mar 2015
 */
package org.volante.abm.decision.pa;

import java.util.Map;

import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.Cell;

import de.cesr.lara.components.LaraPerformableBo;
import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;

/**
 * Assigns the FR identified by this PO's key to this PO's agent.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class FrSelectionPa extends CraftyPa<FrSelectionPa> implements LaraPerformableBo {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(FrSelectionPa.class);

	public enum FrSelectionPreferences {

		COMPETITIVENESS("Competitiveness"),

		NEIGHBOUR_APPROVAL("NeighbourApproval"),

		SOCIAL_APPROVAL("SocialApproval");

		FrSelectionPreferences(String name) {
			this.name = name;
		}

		protected String name;

		public String getName() {
			return this.name;
		}
	}
	/**
	 * @param key
	 * @param agent
	 * @param preferenceUtilities
	 */
	public FrSelectionPa(
			String key,
			LaraBehaviouralComponent agent,
			Map<LaraPreference, Double> preferenceUtilities) {
		super(key, agent, preferenceUtilities);
		checkFrKey(key, agent);
	}

	public FrSelectionPa(String key, LaraBehaviouralComponent agent) {
		super(key, agent);
		checkFrKey(key, agent);
	}

	/**
	 * @param key
	 * @param agent
	 */
	protected void checkFrKey(String key, LaraBehaviouralComponent agent) {
		if (!agent.getAgent().getRegion().getFunctionalRoleMapByLabel().containsKey(key)) {
			throw new IllegalStateException("There is no FR defined in region " + agent.getAgent().getRegion()
					+ " for label " + key + "!");
		}
	}

	/**
	 * @see de.cesr.lara.components.LaraPerformableBo#perform()
	 */
	@Override
	public void perform() {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Assign FR " + this.getKey() + " to agent "
					+ this.getAgent().getAgent());
		}
		// LOGGING ->

		this.getAgent().getAgent().getRegion()
						.getFunctionalRoleMapByLabel().get(this.getKey())
				.assignNewFunctionalComp(this.getAgent().getAgent());
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
	 *      java.util.Map)
	 */
	@Override
	public CraftyPa<FrSelectionPa> getModifiedBO(
			LaraBehaviouralComponent agent,
			Map<LaraPreference, Double> preferenceUtilities) {
		return new FrSelectionPa(this.getKey(), agent, preferenceUtilities);
	}



	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getSituationalUtilities(de.cesr.lara.components.decision.LaraDecisionConfiguration)
	 */
	@Override
	public Map<LaraPreference, Double> getSituationalUtilities(
			LaraDecisionConfiguration dConfig) {
		Map<LaraPreference, Double> utilities = super.getModifiableUtilities();
		// calculate competitiveness
		double competitiveness = 0;
		for (Cell c : ((LandUseAgent) getAgent().getAgent()).getCells()) {
			competitiveness += c.getRegion().getCompetitiveness(
					getAgent().getAgent().getRegion()
							.getFunctionalRoleMapByLabel().get(this.getKey()),
					c);
		}

		utilities.put(this.getAgent().getLaraComp().getLaraModel().
				getPrefRegistry().get(FrSelectionPreferences.COMPETITIVENESS.getName()),
				competitiveness);

		double socialApproval = 0;
		if (this.getAgent().getAgent() instanceof SocialAgent
				&& this.getAgent().getAgent().getRegion().getNetwork() != null) {
			for (Agent partner : this.getAgent().getAgent().getRegion()
					.getNetwork()
					.getPredecessors((SocialAgent) this.getAgent().getAgent())) {
				if (partner.getFC().getFR().getLabel().equals(this.getKey())) {
					socialApproval += 1.0;
				}
			}
			socialApproval = socialApproval
					/ this.getAgent()
							.getAgent()
							.getRegion()
							.getNetwork()
							.getInDegree(
									(SocialAgent) this.getAgent().getAgent());
		}
		utilities.put(this.getAgent().getLaraComp().getLaraModel().
				getPrefRegistry().get(FrSelectionPreferences.SOCIAL_APPROVAL.getName()),
				socialApproval);

		double neighbourApproval = 0;
		if (this.getAgent().getAgent().getHomeCell() != null) {
			for (Cell neighbour : this.getAgent().getAgent().getRegion()
					.getAdjacentCells(this.getAgent().getAgent().getHomeCell())) {
				if (neighbour.getOwnersFrLabel().equals(this.getKey())) {
					neighbourApproval += 1.0;
				}
			}
		}
		utilities.put(
				this.getAgent()
						.getLaraComp()
						.getLaraModel()
						.getPrefRegistry()
						.get(FrSelectionPreferences.NEIGHBOUR_APPROVAL
								.getName()), neighbourApproval);
		
		return utilities;
	}
}
