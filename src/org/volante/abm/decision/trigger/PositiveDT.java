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
 * Created by Sascha Holzhauer on 3 Jun 2015
 */
package org.volante.abm.decision.trigger;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;

import de.cesr.lara.components.model.impl.LModel;


/**
 * The check is always positive and the decision configuration gets triggered.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class PositiveDT extends AbstractDelayedStartDecisionTrigger {

	/**
	 * 
	 */
	public PositiveDT() {
	}

	/**
	 * For testing purposes
	 * 
	 * @param dcId
	 */
	public PositiveDT(String dcId) {
		this.dcId = dcId;
	}

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#check(Agent)
	 */
	@Override
	protected boolean checkHook(Agent agent) {
		((LaraBehaviouralComponent) agent.getBC()).subscribeOnce(LModel.getModel(agent.getRegion())
				.getDecisionConfigRegistry()
				.get(dcId), this);
		return true;
	}
}
