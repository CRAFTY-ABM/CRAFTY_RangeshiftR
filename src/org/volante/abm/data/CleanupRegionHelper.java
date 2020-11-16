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
package org.volante.abm.data;


import org.volante.abm.agent.LandUseAgent;


/**
 * Helps to clean up components after updating competitiveness and before allocation (cleanUp) and when an agents is
 * removed from the region (cleanUpAgent; called at {@link Region#removeAgent(LandUseAgent)}).
 * 
 * @author Sascha Holzhauer
 * 
 */
public interface CleanupRegionHelper extends RegionHelper {

	public void cleanUp(Region region);

	public void cleanUpAgent(Region region, LandUseAgent agentToRemove);

}
