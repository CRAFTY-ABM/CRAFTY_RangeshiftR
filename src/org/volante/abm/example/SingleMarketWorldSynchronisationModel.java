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


import org.apache.log4j.Logger;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.data.Service;
import org.volante.abm.models.WorldDemandModel;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.schedule.MpiUtilities;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Requests number of cells, demand, and supply from every region, sums the
 * values up and reports them back to each region via {@link MpiUtilities}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class SingleMarketWorldSynchronisationModel implements WorldSynchronisationModel {

	/**
	 * Logger
	 */
	static private Logger	logger		= Logger.getLogger(SingleMarketWorldSynchronisationModel.class);

	ModelData modelData = null;
	RunInfo info = null;

	DoubleMap<Service> worldDemandMap = null;
	DoubleMap<Service> worldSupplyMap = null;

	@Override
	public void initialise(ModelData data, RunInfo info) {
		this.modelData = data;
		this.info = info;

		worldDemandMap = this.modelData.serviceMap();
		worldSupplyMap = this.modelData.serviceMap();
	}

	@Override
	public void synchronizeNumOfCells(RegionSet regions) {
		int numRegionalCells = 0;
		for (Region r : regions.getAllRegions()) {
			numRegionalCells += r.getNumCells();
		}

		int numWorldCells = MpiUtilities.distributeNumOfCells(numRegionalCells);

		// <- LOGGING
		logger.info("Number of cells in the world: " + numWorldCells);
		// LOGGING ->

		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldNumberCells(numWorldCells);
		}
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeDemand(org.volante.abm.data.RegionSet)
	 */
	@Override
	public void synchronizeDemand(RegionSet regions) {
		DoubleMap<Service> demand = modelData.serviceMap();
		for (Region r : regions.getAllRegions()) {
			if (r.getDemandModel() instanceof WorldDemandModel) {
				((WorldDemandModel) r.getDemandModel()).getRegionalDemand().addInto(demand);
			} else {
				throw new IllegalStateException("The demand model of region " + r
						+ " does not implement" +
						"WorldDemandModel but is meant to be applied to a global market!");
			}
		}

		double[] worldDemand = MpiUtilities.distributeWorldDemand(demand.getAll());
		this.worldDemandMap = new DoubleMap<Service>(modelData.services, worldDemand);

		// <- LOGGING
		logger.info("World Demand: " + worldDemandMap.prettyPrint());
		// LOGGING ->

		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldDemand(worldDemandMap);
		}
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#synchronizeSupply(org.volante.abm.data.RegionSet)
	 */
	@Override
	public void synchronizeSupply(RegionSet regions) {
		DoubleMap<Service> supply = modelData.serviceMap();
		for (Region r : regions.getAllRegions()) {
			if (r.getDemandModel() instanceof WorldDemandModel) {
				((WorldDemandModel) r.getDemandModel()).getRegionalSupply().addInto(supply);

				// <- LOGGING
				logger.info("Supply of region " + r + ": "
						+ ((WorldDemandModel) r.getDemandModel()).getRegionalSupply().prettyPrint());
				// LOGGING ->

			} else {
				throw new IllegalStateException("The demand model of region " + r
						+ " does not implement" +
						"WorldDemandModel but is meant to be applied to a global market!");
			}
		}

		double[] worldSupply = MpiUtilities.distributeWorldSupply(supply.getAll());
		this.worldSupplyMap = new DoubleMap<Service>(modelData.services, worldSupply);

		// <- LOGGING
		logger.info("World Supply: " + worldSupplyMap.prettyPrint());
		// LOGGING ->

		for (Region r : regions.getAllRegions()) {
			((WorldDemandModel) r.getDemandModel()).setWorldSupply(worldSupplyMap);
		}
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#getWorldDemand()
	 */
	@Override
	public DoubleMap<Service> getWorldDemand() {
		return this.worldDemandMap.copy();
	}

	/**
	 * @see org.volante.abm.models.WorldSynchronisationModel#getWorldSupply()
	 */
	@Override
	public DoubleMap<Service> getWorldSupply() {
		return this.worldSupplyMap.copy();
	}
}
