/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2016 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 15 Jun 2016
 */
package org.volante.abm.agent;


import java.util.Set;

import org.volante.abm.decision.trigger.DecisionTrigger;


/**
 * @author Sascha Holzhauer
 *
 */
public interface DecisionTriggerPrecheckingAgent {

	/**
	 * Enables the agent to pre-select the set of {@link DecisionTrigger}s. Also enables to agent to suppress decisions
	 * by returning an empty set
	 * 
	 * @param decisionTriggers
	 * @return set of checked decision triggerss
	 */
	public Set<DecisionTrigger> preCheckDecisionTriggers(Set<DecisionTrigger> decisionTriggers);
}
