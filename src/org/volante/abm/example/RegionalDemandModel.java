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


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.schedule.PostTickAction;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.RegionalDemandDisplay;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.curve.LinearInterpolator;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * Model demand entirely at a regional level. Demand is averaged across all cells in the region.
 *
 * When a cell changes, calculate new demand based on the difference from new to old
 *
 * @author dmrust
 *
 */
public class RegionalDemandModel implements DemandModel, PreTickAction, PostTickAction {

	Logger log = Logger.getLogger(getClass());

	protected Region region;
	
	/**
	 * If true, the demand will be updated every time an agent changes as owner of a cell.
	 */
	@Attribute(required = false)
	protected boolean updateOnAgentChange = true;
	/**
	 * Required to update totalSupply in case of agent change.
	 */
	protected Map<Cell, DoubleMap<Service>> supply = new HashMap<Cell, DoubleMap<Service>>();
	protected DoubleMap<Service> totalSupply = null;
	protected DoubleMap<Service> residual = null;
	protected DoubleMap<Service> perCellResidual = null;
	protected DoubleMap<Service> demand = null;
	protected DoubleMap<Service> perCellDemand = null; // Per-cell demand is updating on: preTick(), setDemand(UnmodifiableNumberMap<Service> dem), 
														// updateSupply(), agentChange(Cell c), tick()
	protected RunInfo runInfo = null;
	protected ModelData modelData = null;

	/**
	 * Name of CSV file that contains per-year demand levels
	 */
	@Attribute(required = false)
	protected String demandCSV = null;
	
	/**
	 * Name of column in CSV file that specifies the year a row belongs to
	 */
	@Attribute(required = false)
	protected String yearCol = "Year";

	protected Map<Service, Curve> demandCurves = new HashMap<Service, Curve>();

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		this.region = r;
		this.runInfo = info;
		this.modelData = data;
		totalSupply = data.serviceMap();
		residual = data.serviceMap();
		perCellResidual = data.serviceMap();
		demand = data.serviceMap();
		perCellDemand = data.serviceMap();
		
		if (updateOnAgentChange) {
			for (Cell c : r.getCells()) {
				supply.put(c, data.serviceMap());
			}
		}
		if (demandCSV != null) {
			loadDemandCurves();
		}
	}

	@Override
	public DoubleMap<Service> getResidualDemand() {
		return residual;
	}

	@Override
	public DoubleMap<Service> getDemand() {
		return demand;
	}

	@Override
	public DoubleMap<Service> getDemand(Cell c) {
		return perCellDemand;
	}

	@Override
	public DoubleMap<Service> getResidualDemand(Cell c) {
		return getAveragedPerCellResidualDemand();
	}

	@Override
	public DoubleMap<Service> getSupply() {
		return totalSupply;
	}

	@Override
	public void agentChange(Cell c) {
		if (updateOnAgentChange) {
			// substitutes the cell's former supply by the cell's new supply:
			totalSupply.subtractInto(supply.get(c), totalSupply);
			c.getSupply().copyInto(supply.get(c));
			c.getSupply().addInto(totalSupply);
			recalculateResidual();
		}
	}

	public void setDemand(UnmodifiableNumberMap<Service> dem) {
		dem.copyInto(demand);
		recalculateResidual();
	}

	/**
	 *
	 */
	@Override
	public void updateSupply() {
		if (updateOnAgentChange) {
			for (Cell c : region.getCells()) {
				c.getSupply().copyInto(supply.get(c));
			}
		}
		totalSupply.clear();
		for (Cell c : region.getCells()) {
			c.getSupply().addInto(totalSupply);
		}
		recalculateResidual();
	}

	/**
	 * Assigns each cell an equal share of total demand. Calculates total residual and per cell
	 * residual by subtracting totalSupply from demand.
	 */
	public void recalculateResidual() {
		demand.multiplyInto(1.0 / supply.size(), perCellDemand);  //  supply.size() = # of cells
		demand.subtractInto(totalSupply, residual); // residual = demand - totalSupply
		residual.multiplyInto(1.0 / supply.size(), perCellResidual); // perCellResidual = residual / # of cells
	}

	@Override
	public void preTick() {
		int tick = runInfo.getSchedule().getCurrentTick();
		log.info("Loading demand from tick: " + tick);
		for (Service s : demand.getKeys()) {
			if (demandCurves.containsKey(s)) {
				double demandvalue = demandCurves.get(s).sample(tick);
				demand.put(s, demandvalue);
				if (demandvalue <= 0.0) {
					log.warn("Demand for " + s + " is set to " + demandvalue
							+ ". This likely leads to infinite competitiveness if an FR produces " + s + "!");
				}
			}
		}
		log.info("Demand: " + demand.prettyPrint());
		// important to adapt residual-per-cell values to new demands:
		recalculateResidual();
	}

	@Override
	public void postTick() {
		log.info("Demand: " + demand.prettyPrint());
		log.info("Supply: " + totalSupply.prettyPrint());
		log.info("Residual: " + residual.prettyPrint());
		log.info("Marginal Utilities: " + getMarginalUtilities().prettyPrint());
	}

	/**
	 * @todo @issue Generally shouldn't use the competition model directly as it ignores institutions, but it's OK here.
	 * 
	 * Currently only used for informational purposes.
	 * 
	 * @see org.volante.abm.models.DemandModel#getMarginalUtilities()
	 */
	@Override
	@SuppressWarnings("deprecation")
	public DoubleMap<Service> getMarginalUtilities() {
		DoubleMap<Service> utilities = modelData.serviceMap();
		CompetitivenessModel comp = region.getCompetitionModel();
		// Since getCompetitiveness() returns a single double value as utility,
		// it needs to be called for each service separately:
		for (Service s : modelData.services) {
			DoubleMap<Service> serv = modelData.serviceMap();
			serv.clear();
			serv.put(s, 1);
			if (comp instanceof CurveCompetitivenessModel) {
				((CurveCompetitivenessModel) comp).getCompetitveness(this, serv, true);
			}
			double score = comp.getCompetitiveness(this, serv);
			utilities.put(s, score);
		}
		return utilities;
	}

	void loadDemandCurves() throws IOException {
		if (log.isDebugEnabled()) {
			log.debug(this.region + "> Load demand from " + demandCSV + "...");
		}

		Map<String, LinearInterpolator> curves = runInfo.getPersister().csvVerticalToCurves(
				demandCSV, yearCol, modelData.services.names(), region.getPersisterContextExtra());
		for (Service s : modelData.services) {
			if (curves.containsKey(s.getName())) {
				demandCurves.put(s, curves.get(s.getName()));
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(this.region + "> Demand curves: " + demandCurves);
		}
	}

	@Override
	public RegionalDemandDisplay getDisplay() {
		return new RegionalDemandDisplay(this);
	}

	/**
	 * @see org.volante.abm.models.DemandModel#getAveragedPerCellResidualDemand()
	 */
	@Override
	public DoubleMap<Service> getAveragedPerCellResidualDemand() {
		return perCellResidual;
	}

	/**
	 * @see org.volante.abm.models.DemandModel#getAveragedPerCellDemand()
	 */
	@Override
	public DoubleMap<Service> getAveragedPerCellDemand() {
		return perCellDemand;
	}
}
