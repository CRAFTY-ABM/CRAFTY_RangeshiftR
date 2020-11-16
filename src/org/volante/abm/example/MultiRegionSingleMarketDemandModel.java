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
package org.volante.abm.example;


import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.WorldDemandModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * Calculates per-cell residuals by dividing world demand by the number of cells
 * in the world.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class MultiRegionSingleMarketDemandModel extends RegionalDemandModel implements
		WorldDemandModel {

	protected DoubleMap<Service>	worldDemand		= null;

	protected DoubleMap<Service>	worldSupply		= null;

	protected DoubleMap<Service>	worldResidual	= null;

	protected int					worldNumCells	= 0;

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		super.initialise(data, info, r);
		this.worldDemand = data.serviceMap();
		this.worldSupply = data.serviceMap();
		this.worldResidual = data.serviceMap();
	}

	/**
	 * Needed to be re-implemented because
	 * {@link MultiRegionSingleMarketDemandModel#updateSupply()} does not call
	 * {@link MultiRegionSingleMarketDemandModel#recalculateResidual()} anymore.
	 * 
	 * @see org.volante.abm.example.RegionalDemandModel#setDemand(com.moseph.modelutils.fastdata.UnmodifiableNumberMap)
	 */
	@Override
	public void setDemand(UnmodifiableNumberMap<Service> dem) {
		dem.copyInto(demand);
		updateSupply();
		recalculateResidual();
	}

	/**
	 * In contrast to {@link RegionalDemandModel#updateSupply()} this version does not call
	 * {@link RegionalDemandModel#recalculateResidual()}!
	 *
	 * @see org.volante.abm.example.RegionalDemandModel#updateSupply()
	 */
	@Override
	public void updateSupply() {

		if (log.isDebugEnabled()) {
			// consider not managed (-1)
			int[] pagentNumbers = new int[region.getFunctionalRoles().size() + 1];
			for (Cell c : region.getCells()) {
				pagentNumbers[c.getOwner().getFC().getFR().getSerialID() + 1]++;
			}

			for (int i = 0; i < pagentNumbers.length; i++) {
				log.debug("Cells of Type " + (i - 1) + ": " + pagentNumbers[i]);
			}
		}

		if (updateOnAgentChange) {
			for (Cell c : region.getCells()) {
				c.getSupply().copyInto(supply.get(c));
			}
		}
		totalSupply.clear();
		for (Cell c : region.getCells()) {
			c.getSupply().addInto(totalSupply);
		}

		if (log.isDebugEnabled()) {
			log.debug("Region's supply after update: " + totalSupply.prettyPrint());
		}
	}

	/**
	 * This yields most likely the same values in all regions but could be changed for certain
	 * regions, that e.g. have no full access to global market.
	 *
	 * @see org.volante.abm.example.RegionalDemandModel#recalculateResidual()
	 */
	@Override
	public void recalculateResidual() {
		worldDemand.multiplyInto(1.0 / this.worldNumCells, perCellDemand);
		worldDemand.subtractInto(worldSupply, worldResidual);
		worldResidual.multiplyInto(1.0 / this.worldNumCells, perCellResidual);
	}

	@Override
	public DoubleMap<Service> getResidualDemand() {
		return worldResidual;
	}

	@Override
	public void postTick() {
		log.info("Demand: " + demand.prettyPrint());
		log.info("Supply: " + totalSupply.prettyPrint());
		log.info("WorldResidual: " + worldResidual.prettyPrint());
		log.info("Marginal Utilities: " + getMarginalUtilities().prettyPrint());
	}

	@Override
	public void setWorldNumberCells(int numCells) {
		this.worldNumCells = numCells;
	}

	@Override
	public DoubleMap<Service> getRegionalDemand() {
		return this.demand;
	}

	@Override
	public void setWorldDemand(DoubleMap<Service> worldDemand) {
		this.worldDemand = worldDemand;
	}

	@Override
	public DoubleMap<Service> getRegionalSupply() {
		return this.totalSupply;
	}

	@Override
	public void setWorldSupply(DoubleMap<Service> worldSupply) {
		this.worldSupply = worldSupply;
	}
}
