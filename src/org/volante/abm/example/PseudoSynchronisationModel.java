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
 * Created by sholzhau on 21 May 2014
 */
package org.volante.abm.example;

import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.data.Service;
import org.volante.abm.models.WorldDemandModel;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Gives the region's values back to the region as if it were world level measures. Return an service double map with
 * initial values (usually 0) as world demand and supply.
 * 
 * @author sholzhau
 * 
 */
public class PseudoSynchronisationModel implements WorldSynchronisationModel {

	ModelData mData = null;

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#initialise(org.volante.abm.data.ModelData, org.volante.abm.schedule.RunInfo)
	 */
	@Override
	public void initialise(ModelData modelData, RunInfo info) {
		this.mData = modelData;
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeNumOfCells(org.volante.abm.data.RegionSet)
	 */
	@Override
	public void synchronizeNumOfCells(RegionSet regions) {
		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldNumberCells(r.getNumCells());
		}
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeDemand(org.volante.abm.data.RegionSet)
	 */
	@Override
	public void synchronizeDemand(RegionSet regions) {
		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldDemand(((WorldDemandModel) r.getDemandModel()).getRegionalDemand());
		}
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeSupply(org.volante.abm.data.RegionSet)
	 */
	@Override
	public void synchronizeSupply(RegionSet regions) {
		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldSupply( ((WorldDemandModel) r.getDemandModel()).getRegionalSupply());
		}
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#getWorldDemand()
	 */
	@Override
	public DoubleMap<Service> getWorldDemand() {
		return this.mData.serviceMap();
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#getWorldSupply()
	 */
	@Override
	public DoubleMap<Service> getWorldSupply() {
		return this.mData.serviceMap();
	}
}
