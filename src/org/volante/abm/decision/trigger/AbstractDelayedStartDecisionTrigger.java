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
 * Created by Sascha Holzhauer on 26 Sep 2016
 */
package org.volante.abm.decision.trigger;


import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.data.ModelData;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.schedule.WorldSyncSchedule;
import org.volante.abm.serialization.GloballyInitialisable;


/**
 * Checks whether the simulation has already arrived at the defined start tick.
 * 
 * @author Sascha Holzhauer
 * 
 */
public abstract class AbstractDelayedStartDecisionTrigger extends AbstractDecisionTrigger implements
        GloballyInitialisable {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(AbstractDelayedStartDecisionTrigger.class);

	@Element(required = false)
	protected int startTick = 0;

	protected RunInfo rInfo = null;
	protected ModelData mData = null;

	public void initialise(ModelData mData, RunInfo info) throws Exception {
		this.rInfo = info;
		this.mData = mData;
	}

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#check(org.volante.abm.agent.Agent)
	 */
	@Override
	public boolean check(Agent agent) {
		return this.checkFormal(agent) && checkHook(agent);
	}

	/**
	 * Implement the actual check here!
	 * 
	 * @param agent
	 * @return true if trigger evaluates to positive
	 */
	abstract protected boolean checkHook(Agent agent);

	/**
	 * @param agent
	 * @return true if the formal criteria for decision triggering are fulfilled.
	 */
	protected boolean checkFormal(Agent agent) {
		if (this.rInfo == null) {
			throw new IllegalStateException(this + " has not been initialised!");
		}
		if (this.rInfo.getSchedule() instanceof WorldSyncSchedule) {
			return (this.startTick <= this.rInfo.getSchedule().getCurrentTick());
		} else {
			logger.error("The schedule needs to be of type 'WorldSyncSchedule' to provide a 'WorldSyncModel'");
			throw new IllegalStateException(
			        "The schedule needs to be of type 'WorldSyncSchedule' to provide a 'WorldSyncModel'");
		}
	}
}
