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


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.GeoAgent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.agent.assembler.DefaultSocialAgentAssembler;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.example.allocation.AgentFinder;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.utils.CellVolatilityMessenger;
import org.volante.abm.models.utils.CellVolatilityObserver;
import org.volante.abm.param.BasicPa;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.SimpleAllocationDisplay;

import com.moseph.modelutils.Utilities;

import de.cesr.parma.core.PmParameterManager;

// The Allocation model is responsible for taking a Region full of Cells, and a set of PotentialAgents, and re-allocating land to new agents. This can include mechanisms such as:
// - replacing agents who have given up
// - allowing new agents to force out existing ones 
// - some form of market 
// - top down optimisation etc.
// The idea is to use the Competitiveness of Agents and PotentialAgents based on the supply, demand and ability to produce in the Region/Cell.
// Note that allocation does not take place during the initial time step. The main reason is to allow CRAFTY to synchronise demand, calculate and synchronise supply, and determine competitiveness (which take place after allocation in the time step cycle). This leads to the assumption that initial land use patters represent those of the initial time step.


/**
 * A very simple kind of allocation. Any abandoned cells get the most competitive agent assigned to them.
 * 
 * Note: Subclasses need to consider reporting allocation changes to the {@link CellVolatilityObserver}.
 * 
 * @author dmrust
 * @author Sascha Holzhauer
 * 
 */
@Root
public class SimpleAllocationModel implements AllocationModel, CellVolatilityMessenger {
	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(SimpleAllocationModel.class);

	protected Set<CellVolatilityObserver> cellVolatilityObserver = new HashSet<CellVolatilityObserver>();

	@Element(required = false)
	protected AgentFinder agentFinder = new DefaultSocialAgentAssembler();

	@Attribute(required = false)
	double proportionToAllocate = 1;

	/**
	 * @param agentFinder
	 */
	public void setAgentFinder(AgentFinder agentFinder) {
		this.agentFinder = agentFinder;
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region r) {
		try {
			this.agentFinder.initialise(data, info, r);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	};

	protected boolean networkNullErrorOccurred = false;

	/**
	 * Creates a copy of the best performing potential agent on each empty cell
	 */
	@Override
	public void allocateLand(Region r) {
		// <- LOGGING
		logger.info("Allocate land for `available(=empty)' cells in region " + r + " (allocating "
		        + r.getAvailable().size() + " cells)...");
		// LOGGING ->

		allocateAvailableCells(r);
	}

	/**
	 * @param r
	 */
	protected void allocateAvailableCells(Region r) {
		// Determine random subset of available cells:
		Collection<Cell> cells2allocate =
		        Utilities.sampleN(r.getAvailable(), (int) (r.getAvailable().size() * proportionToAllocate),
		                r.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ALLOCATION.name());

		for (Cell c : cells2allocate) {
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Create best agent for cell " + c + " of region " + r + " (current owner: " + c.getOwner()
				        + ")...");
			}
			// LOGGING ->

			createBestAgentForCell(r, c);
		}
	}

	/**
	 * @param r
	 * @param c
	 */
	protected void createBestAgentForCell(Region r, Cell c) {
		List<FunctionalRole> fComps = new ArrayList<FunctionalRole>();
		for (FunctionalRole fRole : r.getFunctionalRoleMapByLabel().values()) {
			fComps.add(fRole);
		}
		double max = -Double.MAX_VALUE;
		FunctionalRole bestFr = null;

		double random;

		// Find FR with highest competitiveness above his GU threshold:
		for (FunctionalRole fr : fComps) {
			random = r.getRandom().getURService().nextDouble(RandomPa.RANDOM_SEED_RUN_ALLOCATION.name());

			if (fr.getAllocationProbability() >= random && r.getInstitutions().isAllowed(fr, c)) {

				double s = r.getCompetitiveness(fr, c);
				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug(
					        fr + "> competitiveness: " + s + " (threshold: " + fr.getMeanGivingUpThreshold() + ")");
				}
				// LOGGING ->

				if (s > max) {
					if (s > fr.getMeanGivingUpThreshold()) {
						max = s;
						bestFr = fr;
					}
				}
			}

			// <- LOGGING
			if (logger.isTraceEnabled()) {
				NumberFormat format = (NumberFormat) PmParameterManager.getParameter(r, BasicPa.FLOAT_POINT_FORMAT);
				logger.trace(fr + (fr.getAllocationProbability() >= random ? "" : " not") + " considered (prob: "
				        + format.format(fr.getAllocationProbability()) + "/rand: " + format.format(random) + "); "
				        + (r.getInstitutions().isAllowed(fr, c) ? "" : "not ") + "allowed.");
			}
			// LOGGING ->

		}

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Chosen FR: " + bestFr);
		}
		// LOGGING ->

		if (bestFr != null) {
			// acquire an agent with requested FR (and undefined BT):
			LandUseAgent agent = agentFinder.findAgent(c, Integer.MIN_VALUE, bestFr.getSerialID());

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Ownership from " + c.getOwner() + " --> " + agent);
			}
			// LOGGING ->

			r.setOwnership(agent, c);

			for (CellVolatilityObserver o : cellVolatilityObserver) {
				o.increaseVolatility(c);
			}

			if (r.getNetworkService() != null) {
				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Linking agent " + agent);
				}
				// LOGGING ->

				if (r.getNetwork() != null) {
					if (r.getGeography() != null && agent instanceof GeoAgent) {
						((GeoAgent) agent).addToGeography();
					}
					r.getNetworkService().addAndLinkNode(r.getNetwork(), (SocialAgent) agent);
				} else {
					if (!networkNullErrorOccurred) {
						logger.warn(
						        "Network object not present during creation of new agent (subsequent error messages are suppressed)");
						networkNullErrorOccurred = true;
					}
				}
			}
		}
	}

	@Override
	public AllocationDisplay getDisplay() {
		return new SimpleAllocationDisplay(this);
	}

	/**
	 * @see org.volante.abm.models.utils.CellVolatilityMessenger#registerCellVolatilityOberserver(org.volante.abm.models.utils.CellVolatilityObserver)
	 */
	@Override
	public void registerCellVolatilityOberserver(CellVolatilityObserver observer) {
		this.cellVolatilityObserver.add(observer);
	}
}