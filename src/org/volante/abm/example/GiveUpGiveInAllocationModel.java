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


import static com.moseph.modelutils.Utilities.sample;
import static com.moseph.modelutils.Utilities.sampleN;
import static com.moseph.modelutils.Utilities.scoreMap;
import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.GeoAgent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.utils.CellVolatilityObserver;
import org.volante.abm.models.utils.GivingInStatisticsMessenger;
import org.volante.abm.models.utils.TakeoverMessenger;
import org.volante.abm.models.utils.TakeoverObserver;
import org.volante.abm.output.GivingInStatisticsObserver;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.BatchRunParser;

import com.moseph.modelutils.Utilities;
import com.moseph.modelutils.Utilities.Score;
import com.moseph.modelutils.Utilities.ScoreComparator;
import com.moseph.modelutils.fastdata.DoubleMap;

/**
 * in addition to the SimpleAllocationModel, this randomly samples potential agents based on their general competitiveness in the
 * region, and allows them to try to take over a number of cells.
 * 
 * 
 * @author dmrust
 * @author Sascha Holzhauer
 * @author Bumsuk Seo
 */
public class GiveUpGiveInAllocationModel extends SimpleAllocationModel
implements TakeoverMessenger, GivingInStatisticsMessenger {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(GiveUpGiveInAllocationModel.class);

	/**
	 * The number of cells a single agent (type) can search over to find maximum competitiveness
	 */
	@Attribute(required = false)
	public String numCells = "NaN";

	protected int numSearchedCells = Integer.MIN_VALUE;

	/**
	 * Alternative to {@link GiveUpGiveInAllocationModel#numCells}: specify the percentage of entire cells in the region
	 * a single agent (type) searches over.
	 */
	@Attribute(required = false)
	public String percentageCells = "NaN";

	/**
	 * The number of times an agent (type) is selected for a take over (i.e. performing the above no. of searches for a
	 * cell)
	 */
	@Attribute(required = false)
	public String numTakeovers = "NaN";

	@Element(required = false)
	public AllocationTryToComeInMode tryToComeInMode = AllocationTryToComeInMode.RANDOM_CELL_ORDER;

	/**
	 * Alternative to {@link GiveUpGiveInAllocationModel#numTakeovers}: specify the percentage of take overs per single
	 * agent (type).
	 */
	@Attribute(required = false)
	public String percentageTakeOvers = "NaN";

	public int numTakeoversDerived = Integer.MIN_VALUE;

	/**
	 * Sets the impact of a potential FR's competitiveness (on a perfect cell) during probabilistic choice of FRs. Set
	 * to 0 for uniformly random selection.
	 */
	@Attribute(required = false)
	public float probabilityExponent = 2.0f;




	/**
	 * Sets the give-in and give-up threshold to be relative to competitiveness of perfect agents. 
	 */
	@Attribute(required = false)
	public boolean relativeThresholding = false;



	protected Cell perfectCell = new Cell();
	/**
	 * @return the perfectCell
	 */
	public Cell getPerfectCell() {
		return perfectCell;
	}


	protected ModelData data = null;

	protected Set<TakeoverObserver> takeoverObserver = new HashSet<>();

	protected Set<GivingInStatisticsObserver> statisticsObserver = new HashSet<>();

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) {
		super.initialise(data, info, r);

		if (!numTakeovers.equals("NaN") && !this.percentageTakeOvers.equals("NaN")) {
			logger.error("You may not specify both, numTakeovers and percentageTakeOvers!");
			throw new IllegalStateException("You may not specify both, numTakeovers and percentageTakeOvers!");
		}

		if (numTakeovers.equals("NaN")) {
			if (this.percentageTakeOvers.equals("NaN")) {
				logger.error("You need to specify either numTakeovers or percentageTakeOvers!");
				throw new IllegalStateException("You need to specify either numTakeovers or percentageTakeOvers!");
			} else {
				this.numTakeoversDerived =
						(int) (r.getNumCells() * BatchRunParser.parseDouble(this.percentageTakeOvers, info) / 100.0);
			}
		} else {
			this.numTakeoversDerived = BatchRunParser.parseInt(this.numTakeovers, info);
		}

		if (!numCells.equals("NaN") && !this.percentageCells.equals("NaN")) {
			logger.error("You may not specify both, numCells and percentageCells!");
			throw new IllegalStateException("You may not specify both, numCells and percentageCells!");
		}

		if (numCells.equals("NaN")) {
			if (this.percentageCells.equals("NaN")) {
				logger.error("You need to specify either numCells or percentageCells!");
				throw new IllegalStateException("You need to specify either numCells or percentageCells!");
			} else {
				this.numSearchedCells =
						(int) (r.getNumCells() * BatchRunParser.parseDouble(this.percentageCells, info) / 100.0);
			}
		} else {
			this.numSearchedCells = BatchRunParser.parseInt(this.numCells, info);
		}




		this.data = data;
		perfectCell.initialise(data, info, r);

		DoubleMap<Capital> adjusted = r.getModelData().capitalMap();
		for (Capital c : data.capitals) {
			adjusted.putDouble(c, 1);
		}
		perfectCell.setBaseCapitals(adjusted);
	};

	/**
	 * Creates a copy of the best performing potential agent on each empty cell 
	 */
	@Override
	public void allocateLand(final Region r) {
		if (r.getRinfo().getSchedule().getCurrentTick() == r.getRinfo().getSchedule().getStartTick() + 1) {
			for (TakeoverObserver o : takeoverObserver) {
				o.initTakeOvers(r);
			}
			for (GivingInStatisticsObserver o : statisticsObserver) {
				o.initGivingInStatistic(r);
			}
		}

		super.allocateLand(r); // Puts the best agent on any unmanaged (=empty = available) cells

		// a function to compute competitiveness scores by FR
		Score<FunctionalRole> compScore = new Score<FunctionalRole>() {
			@Override
			public double getScore(FunctionalRole a) {
				return pow(r.getCompetitiveness(a, perfectCell), probabilityExponent);
			}
		};


		// create a set competitiveness score fComps (number of AFTs)
		Set<FunctionalRole> fComps = new LinkedHashSet<>();
		for (FunctionalRole fRole : r.getFunctionalRoleMapByLabel().values()) {
			fComps.add(fRole);
		}

		logger.info("Number of derived take overs: " + numTakeoversDerived + " (specified percentage: "
				+ this.percentageTakeOvers + ")");

		////////////
		//
		// for (int i = 0; i < numTakeovers; i++) {
		// // Resample this each time to deal with changes in supply affecting competitiveness
		// Map<PotentialAgent, Double> scores = scoreMap(r.getPotentialAgents(), compScore);
		// tryToComeIn(sample(scores, true), r);
		// }

		///////////

		// Map<FunctionalRole, Double> scores = scoreMap(fComps,
		// compScore);
		//
		// // normalise scores:
		// double maxProb = 0.0;
		// for (double d : scores.values()) {
		// maxProb += d;
		// }
		//
		// for (Map.Entry<FunctionalRole, Double> entry : scores.entrySet()) {
		// if (maxProb == 0) {
		// scores.put(entry.getKey(), 1.0 / scores.size());
		// } else {
		// scores.put(entry.getKey(), entry.getValue() / maxProb);
		// }
		// }

		/////////////////

		// <- LOGGING
		logger.info("Apply Try-to-come-in-mode " + tryToComeInMode);
		// LOGGING ->
		Map<FunctionalRole, Double> scores;
		double maxProb;
		//@TODO optimise the for loop (using parallel stream?)
		for (int i = 0; i < numTakeoversDerived; i++) {

			// updating supply/demand is done in r.setOwnership(agent, c);

			// Update scores (for each FR), which changes in the course as new ownerships are set in trytoComeIn()
			scores = scoreMap(fComps, compScore); 

			logger.debug(scores);
			// sum of the scores
			maxProb = 0.0;
			for (double d : scores.values()) {
				maxProb += d;
			}

			// normalise scores:
			for (Map.Entry<FunctionalRole, Double> entry : scores.entrySet()) {
				if (maxProb == 0) {
					scores.put(entry.getKey(), 1.0 / scores.size());
				} else {
					scores.put(entry.getKey(), entry.getValue() / maxProb);
				}
			}

			// Try to come in the order of the normalised scores (= competitiveness ^ probabilityExponent
			// Resample this each time to deal with changes in supply affecting competitiveness
			// com.moseph.modelutils.Utilities.sample() samples from the map of probabilities (i.e. T -> prob of T)  

			tryToComeIn(sample(scores, false, r.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ALLOCATION.name()),
					r);
		}
	}

	/**
	 * Tries to create one of the given agents if it can take over a cell
	 * 
	 * @param fr
	 * @param r
	 */
	/*
	 * public void tryToComeIn( final PotentialAgent a, final Region r ) { if( a == null ) return; //In the rare case
	 * that all have 0 competitiveness, a can be null final Agent agent = a.createAgent( r ); Map<Cell, Double>
	 * competitiveness = scoreMap( sampleN( r.getCells(), numCells ), new Score<Cell>() { public double getScore( Cell c
	 * ) { return r.getCompetitiveness( agent.supply( c ), c ); } }); List<Cell> sorted = new
	 * ArrayList<Cell>(competitiveness.keySet()); Collections.sort( sorted, new ScoreComparator<Cell>( competitiveness )
	 * );
	 * 
	 * 
	 * for( Cell c : sorted ) { if( competitiveness.get( c ) < a.getGivingUp() ) break; boolean canTake =
	 * c.getOwner().canTakeOver( c, competitiveness.get(c) ); if( canTake ) { r.setOwnership( agent, c ); break; } } }
	 */

	public void tryToComeIn(final FunctionalRole fr, final Region r) {
		if (fr == null) {
			return; // In the rare case that all have 0 competitiveness, a can be null
		}

		Map<Cell, Double> competitiveness = scoreMap(sampleN(r.getCells(), numSearchedCells,
				r.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ALLOCATION.name()), new Score<Cell>() {
			@Override
			public double getScore(Cell c) {
				return r.getCompetitiveness(fr, c);
			}
		});

		List<Cell> sorted = new ArrayList<>(competitiveness.keySet());

		switch (tryToComeInMode) {
			case SORTED_CELLS:
				Collections.sort(sorted, new ScoreComparator<>(competitiveness));
				break;

			case REVERSE_SORTED_CELLS:
				Collections.sort(sorted, new ScoreComparator<>(competitiveness));
				Collections.reverse(sorted);
				break;

			case RANDOM_CELL_ORDER:
				Utilities.shuffle(sorted, r.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ALLOCATION.name());
				break;
		}

		logger.debug("Try " + fr.getLabel() + " to take over on mostly " + sorted.size() + " cells (region " + r.getID()
		+ " has " + r.getNumCells() + " cells).");

		double newAgentsGU = fr.getSampledGivingUpThreshold(); 
		for (Cell c : sorted) {
			// if (competitiveness.get(c) < a.getGivingUp()) return;


			boolean canComein = competitiveness.get(c) > newAgentsGU;
			boolean canTakeOver =c.getOwner().canTakeOver(c, competitiveness.get(c));
			boolean isAllowed = r.getInstitutions().isAllowed(fr, c); // e.g., protected area


			if (canComein && canTakeOver && isAllowed) {

				LandUseAgent agent = agentFinder.findAgent(c, Integer.MIN_VALUE, fr.getSerialID());

				agent.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD, // @TODO print out in cell table
						newAgentsGU);

				for (TakeoverObserver observer : takeoverObserver) {
					observer.setTakeover(r, c.getOwner(), agent);
				}
				for (CellVolatilityObserver o : cellVolatilityObserver) {
					o.increaseVolatility(c);
				}

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Ownership from :" + c.getOwner() + " --> " + agent);
					logger.debug("Take over cell " + sorted.indexOf(c) + " of " + sorted.size());
				}
				// LOGGING ->

				for (GivingInStatisticsObserver observer : this.statisticsObserver) {
					observer.setNumberSearchedCells(r, fr, sorted.indexOf(c) + 1);
				}

				r.setOwnership(agent, c);

				if (r.getNetworkService() != null) {
					if (r.getNetwork() != null) {

						if (r.getGeography() != null && agent instanceof GeoAgent) {
							((GeoAgent) agent).addToGeography();
						}
						if (agent instanceof SocialAgent) {
							r.getNetworkService().addAndLinkNode(r.getNetwork(), (SocialAgent) agent);
						}
					} else {
						if (!networkNullErrorOccurred) {
							logger.warn(
									"Network object not present during creation of new agent (subsequent error messages are suppressed)");
							networkNullErrorOccurred = true;
						}
					}
				}

				break;
			} else {
				// not interested in

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
	public void registerGivingInStatisticOberserver(GivingInStatisticsObserver observer) {
		this.statisticsObserver.add(observer);
	}
}
