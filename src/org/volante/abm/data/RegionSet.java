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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.schedule.RunInfo;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Generic set of regions, allowing for multi-scale representation
 * 
 * @author dmrust
 * 
 */
public class RegionSet implements Regions {
	protected Set<Regions>	regions	= new LinkedHashSet<Regions>();
	Extent extent = new Extent();
	String id = "Unknown";

	public RegionSet() {
	}

	/**
	 * @param regions
	 *            one or several regions
	 */
	public RegionSet(Region... regions) {
		for (Region r : regions) {
			addRegion(r);
		}
	}

	/**
	 * Initialisation: Initialises regions and updates extent.
	 * 
	 * TODO does not consider provided region
	 * 
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region region)
			throws Exception {
		for (Regions r : regions) {
			r.initialise(data, info, null);
			extent.update(r.getExtent());
		}
	}

	/***********************************************************************
	 * Access methods. Uses Guava to concatenate iterables for sub-regions
	 * *********************************************************************/

	/**
	 * @see org.volante.abm.data.Regions#getAllRegions()
	 */
	@Override
	public Iterable<Region> getAllRegions() {
		return Iterables.concat(Iterables.transform(regions,
				new Function<Regions, Iterable<Region>>() {
					@Override
					public Iterable<Region> apply(Regions r) {
						return r.getAllRegions();
					}
				}));
	}

	/**
	 * @see org.volante.abm.data.Regions#getAllAmbulantAgents()
	 */
	@Override
	public Iterable<LandUseAgent> getAllAmbulantAgents() {
		return Iterables.concat(Iterables.transform(regions,
 new Function<Regions, Iterable<LandUseAgent>>() {
					@Override
			public Iterable<LandUseAgent> apply(Regions r) {
						return r.getAllAmbulantAgents();
					}
				}));
	}

	/**
	 * @see org.volante.abm.data.Regions#getAllAllocatedAgents()
	 */
	@Override
	public Iterable<LandUseAgent> getAllAllocatedAgents() {
		return Iterables.concat(Iterables.transform(regions,
 new Function<Regions, Iterable<LandUseAgent>>() {
					@Override
			public Iterable<LandUseAgent> apply(Regions r) {
						return r.getAllAllocatedAgents();
					}
				}));
	}

	/**
	 * @see org.volante.abm.data.Regions#getAllCells()
	 */
	@Override
	public Iterable<Cell> getAllCells() {
		return Iterables.concat(Iterables.transform(regions,
				new Function<Regions, Iterable<Cell>>() {
					@Override
					public Iterable<Cell> apply(Regions r) {
						return r.getAllCells();
					}
				}));
	}

	/**
	 * @param r
	 */
	public void addRegion(Region r) {
		extent.update(r.getExtent());
		regions.add(r);
	}

	/**
	 * @return collection of regions
	 */
	public Collection<Regions> getRegions() {
		return Collections.unmodifiableCollection(regions);
	}

	/**
	 * @see org.volante.abm.data.Regions#getExtent()
	 */
	@Override
	public Extent getExtent() {
		return extent;
	}

	/**
	 * @see org.volante.abm.data.Regions#getID()
	 */
	@Override
	public String getID() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * @see org.volante.abm.data.Regions#getNumCells()
	 */
	@Override
	public int getNumCells() {
		int c = 0;
		for (Regions r : regions) {
			c += r.getNumCells();
		}
		return c;
	}

	/**
	 * @see org.volante.abm.data.Regions#getBehaviouralTypeMapByLabel()
	 */
	@Override
	public Map<String, BehaviouralType> getBehaviouralTypeMapByLabel() {
		Map<String, BehaviouralType> behaviouralTypes = new HashMap<String, BehaviouralType>();

		for (Regions region : regions) {
			behaviouralTypes.putAll(region.getBehaviouralTypeMapByLabel());
		}
		return behaviouralTypes;
	}

	/**
	 * @see org.volante.abm.data.Regions#getBehaviouralTypeMapBySerialId()
	 */
	@Override
	public Map<Integer, BehaviouralType> getBehaviouralTypeMapBySerialId() {
		Map<Integer, BehaviouralType> behaviouralTypes = new HashMap<Integer, BehaviouralType>();

		for (Regions region : regions) {
			behaviouralTypes.putAll(region.getBehaviouralTypeMapBySerialId());
		}
		return behaviouralTypes;
	}

	/**
	 * @see org.volante.abm.data.Regions#getFunctionalRoleMapByLabel()
	 */
	@Override
	public Map<String, FunctionalRole> getFunctionalRoleMapByLabel() {
		Map<String, FunctionalRole> functionalRoles = new HashMap<String, FunctionalRole>();

		for (Regions region : regions) {
			functionalRoles.putAll(region.getFunctionalRoleMapByLabel());
		}
		return functionalRoles;
	}

	/**
	 * @see org.volante.abm.data.Regions#getFunctionalRoleMapBySerialId()
	 */
	@Override
	public Map<Integer, FunctionalRole> getFunctionalRoleMapBySerialId() {
		Map<Integer, FunctionalRole> functionalRoles = new HashMap<Integer, FunctionalRole>();

		for (Regions region : regions) {
			functionalRoles.putAll(region.getFunctionalRoleMapBySerialId());
		}
		return functionalRoles;
	}
	
	/**
	 * @see org.volante.abm.data.Regions#getFunctionalRoleMapBySerialId()
	 */
	@Override
	public Collection<FunctionalRole> getFunctionalRoles() {
		Set<FunctionalRole> functionalRoles = new HashSet<FunctionalRole>();

		for (Regions region : regions) {
			functionalRoles.addAll(region.getFunctionalRoles());
		}
		return functionalRoles;
	}
}
