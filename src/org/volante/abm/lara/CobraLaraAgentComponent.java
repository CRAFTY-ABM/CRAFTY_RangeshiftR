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
 * Created by Sascha Holzhauer on 25 Mar 2016
 */
package org.volante.abm.lara;


import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.output.ActionReporter;

import de.cesr.lara.components.agents.LaraAgentComponent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;

/**
 * @author Sascha Holzhauer
 *
 */
public interface CobraLaraAgentComponent extends LaraAgentComponent<LaraBehaviouralComponent, CraftyPa<?>> {

	/**
	 * @param dc
	 */
	public void subscribeOnce(LaraDecisionConfiguration dc);

	public void subscribeOnce(LaraDecisionConfiguration dc, DecisionTrigger trigger);

	/**
	 * @param paReporter
	 *        {@link ActionReporter} to subscribe
	 */
	public void addPaReporter(ActionReporter paReporter);

	/**
	 * @param paReporter
	 *        {@link ActionReporter} to be removed
	 * @return true if given {@link ActionReporter} could be removed
	 */
	public boolean removePaReporter(ActionReporter paReporter);

	public void reportActionPerformance(CraftyPa<?> pa);
}
