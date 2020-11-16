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

import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.BehaviouralComponent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.example.AgentPropertyIds;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.eventbus.events.LAgentPreprocessEvent;
import de.cesr.lara.components.eventbus.impl.LEventbus;
import de.cesr.lara.components.model.impl.LModel;

/**
 * Subscribes the {@link BehaviouralComponent} at the {@link LEventbus} for
 * {@link LAgentPreprocessEvent} when its
 * {@link AgentPropertyIds#COMPETITIVENESS} falls below the defined threshold.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class ThresholdCompetitivenessDT extends AbstractDecisionTrigger {

	@Attribute(name = "competitivenessThreshold", required = true)
	protected double competitivenessThreshold = Double.NaN;

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#check(Agent)
	 */
	@Override
	public boolean check(Agent agent) {
		if (agent.getProperty(AgentPropertyIds.COMPETITIVENESS) < this.competitivenessThreshold) {

			LaraDecisionConfiguration dConfig = LModel
					.getModel(agent.getRegion()).getDecisionConfigRegistry()
					.get(this.dcId);

			((LaraBehaviouralComponent) agent.getBC()).subscribeOnce(dConfig, this);
			return true;
		}
		return false;
	}
}
