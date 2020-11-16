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


import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.FunctionalRoleProductionObserver;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.CellCapitalObserver;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.utils.CellVolatilityObserver;
import org.volante.abm.models.utils.ProductionWeightReporter;
import org.volante.abm.models.utils.TakeoverObserver;
import org.volante.abm.output.GivingInStatisticsObserver;
import org.volante.abm.schedule.RunInfo;


/**
 * Cells that sampled potential agents seek to take over are selected based on the potential agents'
 * potential production of its main service on the particular cell.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class BestProductionFirstGiveUpGiveInAllocationModel extends GiveUpGiveInAllocationModel
		implements CellCapitalObserver, FunctionalRoleProductionObserver {

	/**
	 * Logger
	 */
	static private Logger	logger				= Logger.getLogger(BestProductionFirstGiveUpGiveInAllocationModel.class);

	protected Region						region;

	Map<FunctionalRole, SortedList<Cell>> cellProductions = new HashMap<>();

	/**
	 * Applied to sampled indices from the list of sorted cells. A curve object can be assigned to
	 * the factory used to sample probabilities for selection of indices. The curve object is
	 * provided with the index to select or not.
	 */
	@Element(required = false)
	protected IterativeCellSamplerFactory		samplerFactory		= new IterativeCellSamplerFactory();

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) {
		// <- LOGGING
		logger.info("Init...");
		// LOGGING ->
		super.initialise(data, info, r);
		this.region = r;
		this.initCellProductions();

		for (Cell c : region.getAllCells()) {
			c.registerCellCapitalObserver(this);
		}
		for (FunctionalRole fr : region.getFunctionalRoles()) {
			fr.registerFunctionalRoleProductionObserver(this);
		}
	};

	protected void initCellProductions() {
		for (final FunctionalRole fr : this.region.getFunctionalRoles()) {
			final Service mainService;
			if (fr.getProduction() instanceof ProductionWeightReporter) {
				mainService = ((ProductionWeightReporter) fr.getProduction())
						.getProductionWeights().getMax();
			} else {
				mainService = null;
			}

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Main service for " + fr + ": " + mainService);
			}
			// LOGGING ->

			cellProductions.put(fr,
					new SortedList<>(FXCollections.<Cell> observableArrayList(new HashSet<>(this.region.getCells())),
					new Comparator<Cell>() {
						@Override
						public int compare(Cell cell1, Cell cell2) {
							return (-1) * Double.compare(
fr.getExpectedSupply(cell1).getDouble(mainService), fr
													.getExpectedSupply(cell2).getDouble(mainService));
						}
					}));
		}
	}

	/**
	 * Tries to create one of the given agents if it can take over a cell
	 * 
	 * @param fr
	 * @param r
	 */
	public void tryToComeIn(final FunctionalRole fr, final Region r) {
		if (fr == null) {
			return; // In the rare case that all have 0 competitiveness, a can be null
		}
		
		IterativeCellSampler cellsampler = this.samplerFactory.getIterativeCellSampler(
				r.getNumCells(),
				numSearchedCells, r);

		logger.debug("Try " + fr.getLabel() + " to take over on mostly " + numSearchedCells
				+ " cells (region "
				+ r.getID() + " has "
				+ r.getNumCells() + " cells).");

		Cell c;
		Double competitiveness;
		
		boolean takenover = false;
		while (!takenover && cellsampler.hasMoreToSample()) {
			c = cellProductions.get(fr).get(cellsampler.sample());
			competitiveness = r.getCompetitiveness(fr, c);

			if (logger.isDebugEnabled()) {
				logger.debug(cellsampler.numSampled() + "th sampled cell: " + c + " (owners["
 + c.getOwnersFrSerialID()
						+
						"] competitiveness:" + r.getCompetitiveness(c) + " / challenger (" + fr
						+ "): " +
						competitiveness + ")");
			}

			if (competitiveness > fr.getMeanGivingUpThreshold()
					&& c.getOwner().canTakeOver(c, competitiveness)) {

				LandUseAgent agent = agentFinder.findAgent(c, Integer.MIN_VALUE, fr.getSerialID());

				for (TakeoverObserver observer : takeoverObserver) {
					observer.setTakeover(r, c.getOwner(), agent);
				}
				for (CellVolatilityObserver o : cellVolatilityObserver) {
					o.increaseVolatility(c);
				}

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Ownership from :" + c.getOwner() + " --> " + agent);
					logger.debug("Take over " + cellsampler.numSampled() + "th cell (" + c
							+ ") of "
							+ numSearchedCells);
				}
				// LOGGING ->

				for (GivingInStatisticsObserver observer : this.statisticsObserver) {
					observer.setNumberSearchedCells(r, fr, cellsampler.numSampled());
				}

				r.setOwnership(agent, c);
				takenover = true;
			}
		}
	}

	@Override
	public void registerTakeoverOberserver(TakeoverObserver observer) {
		takeoverObserver.add(observer);

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Register TakeoverObserver " + observer);
		}
		// LOGGING ->
	}

	@Override
	public void cellCapitalChanged(Cell cell, boolean remove) {
		for (final FunctionalRole fr : this.region.getFunctionalRoles()) {
			if (remove) {
				cellProductions.get(fr).remove(cell);
			} else {
				cellProductions.get(fr).add(cell);
			}
		}
	}

	@Override
	public void functionalRoleProductionChanged(final FunctionalRole fr) {
		final Service mainService;
		if (fr.getProduction() instanceof ProductionWeightReporter) {
			mainService = ((ProductionWeightReporter) fr.getProduction())
					.getProductionWeights().getMax();
		} else {
			mainService = null;
		}

		cellProductions.put(
				fr,
				new SortedList<>(
						FXCollections.<Cell> observableArrayList(
new HashSet<>(this.region.getCells())),
				new Comparator<Cell>() {
					@Override
					public int compare(Cell cell1, Cell cell2) {
						return (-1) * Double.compare(
fr.getExpectedSupply(cell1).getDouble(mainService), fr
										.getExpectedSupply(cell2).getDouble(mainService));
					}
				}));
	}
}
