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
package org.volante.abm.decision.trigger;

import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.example.AgentPropertyIds;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.model.impl.LModel;


/**
 * Compares the agent's competitiveness to the last tick's and subscribes the agent for defined
 * {@link LaraDecisionConfiguration} in case it decreased. Requires agents to provide
 * {@link DecreasedCompetitivenessDT.FrCheckPropertyIds#LAST_RECORD_TICK} and
 * {@link DecreasedCompetitivenessDT.FrCheckPropertyIds#LAST_COMPETITIVENESS}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class DecreasedCompetitivenessDT extends AbstractDecisionTrigger {

	protected enum FrCheckPropertyIds implements PropertyId {
		LAST_RECORD_TICK,

		LAST_COMPETITIVENESS;
	}

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(DecreasedCompetitivenessDT.class);

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#check(Agent)
	 */
	@Override
	public boolean check(Agent agent) {
		boolean triggered = false;
		double currentCompetitiveness = agent
				.getProperty(AgentPropertyIds.COMPETITIVENESS);

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(agent
					+ "> Checking Functional Role "
					+ (agent.isProvided(FrCheckPropertyIds.LAST_COMPETITIVENESS) ? "(competition: "
					+ currentCompetitiveness + " - was: "
					+ agent.getProperty(FrCheckPropertyIds.LAST_COMPETITIVENESS)
					+ ") "
							: "") + "...");
		}
		// LOGGING ->

		if (agent.isProvided(FrCheckPropertyIds.LAST_COMPETITIVENESS)
				&& agent.isProvided(FrCheckPropertyIds.LAST_RECORD_TICK)
				&& agent.getRegion().getRinfo().getSchedule().getCurrentTick() > agent
						.getProperty(FrCheckPropertyIds.LAST_RECORD_TICK)) {
			if (agent.getProperty(FrCheckPropertyIds.LAST_COMPETITIVENESS) > currentCompetitiveness) {
				LaraDecisionConfiguration dConfig = LModel
						.getModel(agent.getRegion())
						.getDecisionConfigRegistry().get(this.dcId);

				((LaraBehaviouralComponent) agent.getBC()).subscribeOnce(dConfig, this);
				triggered = true;
				
				// <- LOGGING
				logger.info(agent
						+ "> Triggered " + this.dcId + " (competition: "
						+ currentCompetitiveness + " - was: "
						+ agent.getProperty(FrCheckPropertyIds.LAST_COMPETITIVENESS)
						+ ")");
				// LOGGING ->
			}
		}
		agent.setProperty(FrCheckPropertyIds.LAST_COMPETITIVENESS,
				currentCompetitiveness);

		agent.setProperty(FrCheckPropertyIds.LAST_RECORD_TICK, new Double(agent.getRegion().getRinfo().getSchedule()
				.getCurrentTick()));

		return triggered;
	}
}
