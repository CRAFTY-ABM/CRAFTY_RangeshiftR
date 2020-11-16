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


import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.Utilities;


/**
 * Randomly recruits the number given as <code>number</code> from the passed set
 * of agents.
 * 
 * @author Sascha Holzhauer
 *
 */
public class NumberRandomRecruitment implements InstitutionTargetRecruitment {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(NumberRandomRecruitment.class);

	/**
	 * Number of agents that become initially aware.
	 */
	@Element(required = false)
	protected int number = 0;

	protected Region region = null;

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.region = extent;
	}

	/**
	 * Does not guarantee to provide the requested number of agents entirely.
	 * 
	 * @see org.volante.abm.institutions.recruit.InstitutionTargetRecruitment#getRecruitedAgents(java.util.Collection)
	 */
	@Override
	public Collection<InnovativeBC> getRecruitedAgents(
			Collection<? extends InnovativeBC> allAgents) {
		Collection<InnovativeBC> recruitedAgents = new LinkedHashSet<InnovativeBC>();
		ArrayList<InnovativeBC> ibcs = new ArrayList<InnovativeBC>(allAgents);

		for (int i = 0; i < number; i++) {
			if (ibcs.size() == 0) {
				logger.warn("Not enough agents to make aware!");
			} else {
				int index = Utilities.nextIntFromTo(0, ibcs.size() - 1,
						region.getRandom().getURService(),
						RandomPa.RANDOM_SEED_RUN.name());

				recruitedAgents.add(ibcs.get(index));
				ibcs.remove(index);
			}
		}
		return recruitedAgents;
	}

}
