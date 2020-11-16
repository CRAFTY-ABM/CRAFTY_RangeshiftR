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
 * Created by Sascha Holzhauer on 7 Jul 2015
 */
package org.volante.abm.example.allocation;


import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.assembler.AgentAssembler;
import org.volante.abm.agent.assembler.DefaultSocialAgentAssembler;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;


/**
 * Looks for the requested FR in ambulant agents. If the requested FR is not available, a new agent is created.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class GlobalAgentFinder implements AgentFinder {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(GlobalAgentFinder.class);

	Region region;
	ModelData mData;

	@Element(required = false)
	AgentAssembler assembler = new DefaultSocialAgentAssembler();

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.region = extent;
		this.mData = data;
		this.assembler.initialise(data, info, extent);
	}

	/**
	 * @see org.volante.abm.example.allocation.AgentFinder#findAgent(org.volante.abm.data.Cell, int, int)
	 */
	@Override
	public LandUseAgent findAgent(Cell homecell, int btId, int frId) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Find agent for FR " + frId);
		}
		// LOGGING ->

		for (LandUseAgent a : region.getAllAmbulantAgents()) {
			if (a.getFC().getFR().getSerialID() == frId) {
				return a;
			}
		}
		return this.assembler.assembleAgent(homecell, Integer.MIN_VALUE, frId);
	}
}
