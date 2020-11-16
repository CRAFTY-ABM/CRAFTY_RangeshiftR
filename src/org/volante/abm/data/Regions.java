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
 */
package org.volante.abm.data;


import java.util.Collection;
import java.util.Map;

import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.serialization.Initialisable;

public interface Regions extends Initialisable
{
	public String getID();

	/**
	 * Provides the region itself and all sub-regions.
	 * 
	 * @return set of regions itself and all sub-regions.
	 */
	public Iterable<Region> getAllRegions();

	public Iterable<LandUseAgent> getAllAllocatedAgents();

	public Iterable<LandUseAgent> getAllAmbulantAgents();
	public Iterable<Cell> getAllCells();

	public Map<String, BehaviouralType> getBehaviouralTypeMapByLabel();
	public Map<Integer, BehaviouralType> getBehaviouralTypeMapBySerialId();
	public Map<String, FunctionalRole> getFunctionalRoleMapByLabel();
	public Map<Integer, FunctionalRole> getFunctionalRoleMapBySerialId();

	public Collection<FunctionalRole> getFunctionalRoles();
	public Extent getExtent();
	public int getNumCells();
}
