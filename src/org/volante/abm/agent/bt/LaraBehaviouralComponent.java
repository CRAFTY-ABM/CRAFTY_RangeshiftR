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
 * Created by Sascha Holzhauer on 20 Mar 2015
 */
package org.volante.abm.agent.bt;

import org.volante.abm.agent.AgentAccessible;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.lara.CobraLaraAgentComponent;

import de.cesr.lara.components.agents.LaraAgent;
import de.cesr.lara.components.agents.LaraAgentComponent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;

/**
 * 
 * @author Sascha Holzhauer
 * 
 */
public interface LaraBehaviouralComponent extends
		LaraAgent<LaraBehaviouralComponent, CraftyPa<?>>, BehaviouralComponent,
		AgentAccessible {

	/**
	 * Returns the {@link LaraAgentComponent} of this agent.
	 * 
	 * @return component Lara agent component
	 */
	public CobraLaraAgentComponent getLaraComp();

	/**
	 * Subscribes once for the given {@link LaraDecisionConfiguration}. Syntactic sugar: forwards to
	 * {@link LaraAgentComponent}.
	 * 
	 * @param dc
	 */
	public void subscribeOnce(LaraDecisionConfiguration dc);

	/**
	 * Subscribes once for the given {@link LaraDecisionConfiguration}. Syntactic sugar: forwards to
	 * {@link LaraAgentComponent}.
	 * 
	 * @param dc
	 */
	public void subscribeOnce(LaraDecisionConfiguration dc, DecisionTrigger trigger);
}
