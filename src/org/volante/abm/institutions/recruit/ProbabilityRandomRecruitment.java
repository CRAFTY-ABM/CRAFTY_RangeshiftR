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
 * Created by Sascha Holzhauer on 8 Dec 2014
 */
package org.volante.abm.institutions.recruit;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.simpleframework.xml.Element;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.BatchRunParser;

import cern.jet.random.engine.RandomEngine;

/**
 * Recruits each agent from the passed set with probability given by
 * <code>probability</code>.
 * 
 * @author Sascha Holzhauer
 *
 */
public class ProbabilityRandomRecruitment implements InstitutionTargetRecruitment {

	/**
	 * Probability of an agent to become initially aware.
	 */
	@Element(required = false)
	protected String probability = "0.05";

	protected Region region = null;
	protected RunInfo rInfo = null;

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.region = extent;
		this.rInfo = info;
	}


	/**
	 * @see org.volante.abm.institutions.recruit.InstitutionTargetRecruitment#getRecruitedAgents(java.util.Collection)
	 */
	@Override
	public Collection<InnovativeBC> getRecruitedAgents(
			Collection<? extends InnovativeBC> allAgents) {

		Collection<InnovativeBC> recruitedAgents = new LinkedHashSet<InnovativeBC>();
		RandomEngine rEngine = region.getRandom().getURService()
				.getGenerator(RandomPa.RANDOM_SEED_RUN.name());

		double awarenessProb = BatchRunParser.parseDouble(
				this.probability, rInfo);
		for (InnovativeBC ibc : allAgents) {
			if (awarenessProb == 1.0 || rEngine.nextDouble() <= awarenessProb) {
				recruitedAgents.add(ibc);
			}
		}
		return recruitedAgents;
	}
}
