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
 * Created by Sascha Holzhauer on 12 Feb 2016
 */
package org.volante.abm.output;


import org.volante.abm.agent.Agent;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.trigger.DecisionTrigger;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.decision.LaraDecisionData;


/**
 * Interface for reporting the performance of potential actions.
 * 
 * @author Sascha Holzhauer
 * 
 */
public interface ActionReporter {

	public void setActionInfos(Agent agent, DecisionTrigger trigger, LaraDecisionConfiguration dconfig, CraftyPa<?> pa,
			double score);
	
	public void setActionInfos(Agent agent, DecisionTrigger trigger, LaraDecisionConfiguration dconfig,
	        LaraDecisionData<?, CraftyPa<?>> dData, CraftyPa<?> pa, double score, boolean selected);

	public void setActionInfos(Agent agent, DecisionTrigger trigger, LaraDecisionConfiguration dconfig, CraftyPa<?> pa,
			double score, boolean selected);

	/**
	 * Intended for renewed actions that have been set before. These are set anew when an action's performed without a
	 * new decision process.
	 * 
	 * @param agent
	 * @param pa
	 */
	public void setActionInfos(Agent agent, CraftyPa<?> pa);

	public void registerAtAgent(Agent agent);
}
