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
 */
package org.volante.abm.example;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.DemandModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.StaticPerCellDemandDisplay;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * Keeps maps for demands and residuals with keys for each cell.
 * 
 * @author Dave Murray-Rust
 * 
 */
public class StaticPerCellDemandModel implements DemandModel {
	/**
	 * Logger
	 */
	static private Logger			log			= Logger.getLogger(StaticPerCellDemandModel.class);

	Map<Cell, DoubleMap<Service>>	demand		= new HashMap<Cell, DoubleMap<Service>>();
	Map<Cell, DoubleMap<Service>>	residual	= new HashMap<Cell, DoubleMap<Service>>();

	ModelData						data		= null;
	Region							region		= null;

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) {
		this.data = data;
		this.region = r;
		for (Cell c : r.getCells()) {
			demand.put(c, data.serviceMap());
			residual.put(c, data.serviceMap());
		}
	}

	@Override
	public DoubleMap<Service> getDemand() {
		log.fatal("Regional demand not implemented in StaticPerCellDemandModel");
		return null;
	}

	@Override
	public DoubleMap<Service> getSupply() {
		log.fatal("Regional supply not implemented in StaticPerCellDemandModel");
		return null;
	}

	@Override
	public DoubleMap<Service> getMarginalUtilities() {
		log.fatal("Regional marginal utilities not implemented StaticPerCellDemandModel");
		return null;
	}

	/**
	 */
	@Override
	public DoubleMap<Service> getResidualDemand() {
		log.fatal("Regional marginal utilities not implemented StaticPerCellDemandModel");
		return null;
	}

	@Override
	public DoubleMap<Service> getDemand(Cell c) {
		return demand.get(c);
	}

	@Override
	public DoubleMap<Service> getResidualDemand(Cell c) {
		return residual.get(c);
	}

	@Override
	public void agentChange(Cell c) {
		demand.get(c).subtractInto(c.getSupply(), residual.get(c));
	}

	public void setResidual(Cell c, UnmodifiableNumberMap<Service> res) {
		res.copyInto(residual.get(c));
	}

	public void setDemand(Cell c, UnmodifiableNumberMap<Service> dem) {
		dem.copyInto(demand.get(c));
		updateSupply(c);
	}

	public void updateSupply(Cell c) {
		demand.get(c).subtractInto(c.getSupply(), residual.get(c));
	}

	/**
	 * Do nothing
	 */
	@Override
	public void updateSupply() {
		for (Cell c : region.getAllCells()) {
			updateSupply(c);
		}
	}

	@Override
	public StaticPerCellDemandDisplay getDisplay() {
		return new StaticPerCellDemandDisplay(this);
	}

	/**
	 * @see org.volante.abm.models.DemandModel#getAveragedPerCellResidualDemand()
	 */
	@Override
	public DoubleMap<Service> getAveragedPerCellResidualDemand() {
		log.fatal("Average per cell demand residual not implemented in StaticPerCellDemandModel");
		return null;
	}

	/**
	 * @see org.volante.abm.models.DemandModel#getAveragedPerCellDemand()
	 */
	@Override
	public DoubleMap<Service> getAveragedPerCellDemand() {
		DoubleMap<Service> demandsum = data.serviceMap();
		for (Cell c : demand.keySet()) {
			demand.get(c).addInto(demandsum);
		}
		demandsum.multiplyInto(1.0 / demand.size(), demandsum);

		return demandsum;
	}
}
