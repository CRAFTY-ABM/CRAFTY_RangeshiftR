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
 * Created by Sascha Holzhauer on 19 Jul 2016
 */
package org.volante.abm.decision.trigger;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.simpleframework.xml.ElementList;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.data.ModelData;
import org.volante.abm.example.measures.LandUseProportionMeasure;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;


/**
 * TODO implement
 * 
 * @author Sascha Holzhauer
 * 
 */
public class LandUseProportionDT extends AbstractDecisionTrigger implements GloballyInitialisable {

	public enum AgentProperty implements PropertyId {
		CONNECTIVITY_THRESHOLD;
	}

	enum ConnectivityTriggerIds implements PropertyId {
		LAST_RECORD_TICK;
	}

	@ElementList(inline = true, required = false, entry = "similarFrLabel", empty = false)
	public List<String> serialFrLabels = new ArrayList<>();

	protected Set<FunctionalRole> fRoles = null;

	protected ModelData mData;

	public void initialise(ModelData mData, RunInfo info) throws Exception {
		this.mData = mData;
	}

	/**
	 * @param agent
	 * @return connectivity
	 */
	protected double getCurrentLUproportions(Agent agent) {
		if (this.fRoles == null) {
			this.fRoles = new HashSet<>();
			for (String frLabel : this.serialFrLabels) {
				this.fRoles.add(agent.getRegion().getFunctionalRoleMapByLabel().get(frLabel));
			}
		}

		return LandUseProportionMeasure.getScore(this.mData.getRootRegionSet().getAllRegions(), this.fRoles, false);
	}

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#check(org.volante.abm.agent.Agent)
	 */
	@Override
	public boolean check(Agent agent) {

		return false;
	}

}
