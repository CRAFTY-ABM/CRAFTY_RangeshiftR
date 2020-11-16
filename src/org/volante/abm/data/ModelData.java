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


import org.apache.log4j.Logger;
import org.volante.abm.example.SimpleCapital;
import org.volante.abm.example.SimpleLandUse;
import org.volante.abm.example.SimpleService;
import org.volante.abm.schedule.WorldSyncSchedule;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.NamedIndexSet;


public class ModelData {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(ModelData.class);

	public NamedIndexSet<Capital>	capitals	= SimpleCapital.simpleCapitals;
	public NamedIndexSet<Service>	services	= SimpleService.simpleServices;
	public NamedIndexSet<LandUse>	landUses	= SimpleLandUse.simpleLandUses;

	public RegionSet rootRegionSet = null;

	public DoubleMap<Capital> capitalMap() {
		return new DoubleMap<Capital>(capitals);
	}

	public DoubleMap<Service> serviceMap() {
		return new DoubleMap<Service>(services);
	}

	public void setRootRegionalSet(RegionSet rootRegionSet) {
		this.rootRegionSet = rootRegionSet;
	}

	/**
	 * Provides the root region set that gives access to all regions (in parallel applications the regions only which
	 * are executed on the present node!).
	 * 
	 * @return
	 */
	public RegionSet getRootRegionSet() {
		// TODO plug in a WorldRegionSet that synchronises between computational nodes in case of parallelisation!
		if (((WorldSyncSchedule) this.rootRegionSet.getAllRegions().iterator().next().getRinfo().getSchedule())
		        .getWorldSyncModel() == null) {
			logger.warn("When providing the RegionSet the ModelData considers only the regions present at the current node!");
		}
		if (this.rootRegionSet == null) {
			throw new IllegalStateException("Root regional set has not been set at ModelData!");
		}
		return this.rootRegionSet;
	}
}
