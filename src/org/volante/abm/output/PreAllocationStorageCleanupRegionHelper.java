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
 * Created by Sascha Holzhauer on 23 Sep 2015
 */
package org.volante.abm.output;


import java.util.HashMap;
import java.util.Map;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.CleanupRegionHelper;
import org.volante.abm.data.Region;
import org.volante.abm.example.AgentPropertyIds;


/**
 * Applied by {@link CellTable}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class PreAllocationStorageCleanupRegionHelper implements CleanupRegionHelper {

	public class PreAllocData {
		public PreAllocData(int agentID, double competitiveness, double guThreshold) {
			this.agentId = agentID;
			this.competitiveness = competitiveness;
			this.guThreshold = guThreshold;
		}

		public int agentId;
		public double competitiveness;
		public double guThreshold;
	}

	protected Map<Cell, PreAllocData>	preAllocDataMap	= new HashMap<>();

	/**
	 * @see org.volante.abm.data.CleanupRegionHelper#cleanUpAgent(org.volante.abm.data.Region,
	 *      org.volante.abm.agent.LandUseAgent)
	 */
	@Override
	public void cleanUpAgent(Region region, LandUseAgent a) {
		for (Cell c : a.getCells()) {
			preAllocDataMap.put(c,
					new PreAllocData(a.getFC().getFR().getSerialID(), a.getProperty(AgentPropertyIds.COMPETITIVENESS),
							a.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD)));
		}
	}

	@Override
	public void cleanUp(Region region) {
		for (Cell c : region.getAllCells()) {
			if (!c.getOwner().equals(Agent.NOT_MANAGED)){
				preAllocDataMap.put(
						c,
						new PreAllocData(c.getOwner().getFC().getFR().getSerialID(), c.getOwner().getProperty(
								AgentPropertyIds.COMPETITIVENESS), c.getOwner().getProperty(
								AgentPropertyIds.GIVING_UP_THRESHOLD)));
			}
		}
	}

	public PreAllocData getPreAllocData(Cell c) {
		return preAllocDataMap.get(c);
	}

	public void clear() {
		preAllocDataMap.clear();
	}
}
