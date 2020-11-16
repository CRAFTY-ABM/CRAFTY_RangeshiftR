/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 5 Dec 2014
 */
package org.volante.abm.institutions.recruit;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.PopulationRegionHelper;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;


/**
 * Asks the initialTargetRecruitment for the first set of target recruitment agents and returns that
 * for every subsequent requests.
 * 
 * @author Sascha Holzhauer
 *
 */
public class RepetitiveRecruitment implements InstitutionTargetRecruitment,
		PopulationRegionHelper {

	/**
	 * Applied to determine the first set of target recruitment agents.
	 */
	@Element(name = "initialTargetRecruitment", required = true)
	InstitutionTargetRecruitment initialTargetRecruitment;

	/**
	 * Applied to retrieve additional agents in case agents have been removed
	 * from the initial set in meantime.
	 */
	@Element(name = "additionalRecruitment", required = false)
	NumberRandomRecruitment additionalRecruitment = new NumberRandomRecruitment();

	int missingAgents = 0;

	protected Region region;

	Collection<InnovativeBC> recruitedAgents = null;

	/**
	 * @see org.volante.abm.institutions.recruit.InstitutionTargetRecruitment#getRecruitedAgents(java.util.Collection)
	 */
	@Override
	public Collection<InnovativeBC> getRecruitedAgents(
			Collection<? extends InnovativeBC> allIBCs) {
		if (this.recruitedAgents == null)
			this.recruitedAgents = this.initialTargetRecruitment
					.getRecruitedAgents(allIBCs);
		
		if (this.missingAgents > 0) {
			this.additionalRecruitment.number = this.missingAgents;
			
			Collection<InnovativeBC> agentsPool = new LinkedHashSet<InnovativeBC>();
			for (Agent agent : this.region.getAllAllocatedAgents()) {
				if (!this.recruitedAgents.contains(agent.getBC())) {
					agentsPool.add((InnovativeBC) agent.getBC());
				}
			}
			this.recruitedAgents.addAll(this.additionalRecruitment
					.getRecruitedAgents(agentsPool));
			
			this.missingAgents = 0;
		}
		return this.recruitedAgents;
	}

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.initialTargetRecruitment.initialise(data, info, extent);
		this.additionalRecruitment.initialise(data, info, extent);
		this.region = extent;
		this.region.registerHelper(null, this);
	}

	/**
	 * @see org.volante.abm.data.PopulationRegionHelper#agentRemoved(org.volante.abm.agent.Agent)
	 */
	@Override
	public void agentRemoved(Agent agent) {
		if (this.recruitedAgents != null) {
			this.recruitedAgents.remove(agent);
			this.missingAgents++;
		}
	}

	/**
	 * @see org.volante.abm.data.PopulationRegionHelper#agentAdded(org.volante.abm.agent.Agent)
	 */
	@Override
	public void agentAdded(Agent agent) {
		// nothing to do
	}
}
