/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 20 Mar 2015
 */
package org.volante.abm.agent.assembler;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.DefaultLandUseAgent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.agent.fr.LazyFR;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.example.GiveUpGiveInAllocationModel;
import org.volante.abm.example.allocation.AgentFinder;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class DefaultAgentAssembler implements AgentAssembler, AgentFinder {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(DefaultAgentAssembler.class);

	protected Region region;
	protected ModelData mData;
	protected RunInfo rinfo;

	@Element(required = false)
	protected int defaultBtId = Integer.MIN_VALUE;

	@Element(required = false)
	protected int defaultFrId = Integer.MIN_VALUE;

	/**
	 * Assigns the ID of the first entry of
	 * {@link Region#getBehaviouralTypeMapBySerialId()} as defaultBtId is not
	 * yet defined.
	 * 
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData mData, RunInfo runInfo, Region region) {
		this.region = region;
		this.mData = mData;
		this.rinfo = runInfo;
		if (this.defaultBtId == Integer.MIN_VALUE) {
			if (region.getBehaviouralTypeMapBySerialId().isEmpty()) {
				logger.warn("Cannot determine default Behavioural Type ID since the regional list of BT is empty!");
			} else {
				this.defaultBtId = region.getBehaviouralTypeMapBySerialId()
						.entrySet().iterator().next().getValue().getSerialID();
				logger.warn("Assigning first Behavioural Type ID of regional list (as default not set)!");
			}
		}
	}

	/**
	 * @see org.volante.abm.agent.assembler.AgentAssembler#assembleAgent(org.volante.abm.data.Cell, int, int)
	 */
	@Override
	public LandUseAgent assembleAgent(Cell homecell, int btIdInitial, int frIdInitial,
			String id) {
		if (region == null) {
			throw new IllegalStateException(
					"Agent assembler has not been initialised!");
		}

		int btId;
		if (btIdInitial == Integer.MIN_VALUE) {
			btId = this.defaultBtId;
		} else {
			btId = btIdInitial;
		}

		int frId;
		if (frIdInitial == Integer.MIN_VALUE) {
			frId = this.defaultFrId;
		} else {
			frId = frIdInitial;
		}

		LandUseAgent agent =
				new DefaultLandUseAgent((id != null ? id : "Agent_"
						+ (homecell == null ? "" : homecell.toString())), mData);
		agent.setRegion(this.region);
		this.region.setAmbulant(agent);

		if (this.region.getFunctionalRoleMapBySerialId().containsKey(frId)) {
			this.region.getFunctionalRoleMapBySerialId().get(frId)
			.assignNewFunctionalComp(agent);
		} else if (this.defaultFrId == Integer.MIN_VALUE) {
			LazyFR.getInstance().assignNewFunctionalComp(agent);
			logger.warn("Requested FunctionalRole (" + frId + ") not found. Using LazyFR!");
		} else {
			logger.error("Couldn't find FunctionalRole by id: " + frId);
		}

		if (this.region.getBehaviouralTypeMapBySerialId().containsKey(btId)) {
			this.region.getBehaviouralTypeMapBySerialId().get(btId)
			.assignNewBehaviouralComp(agent);
		} else {
			logger.error("Couldn't find BehaviouralType by id: " + btId);
		}

		agent.setHomeCell(homecell);

		
		// set relative thresholding in case GuGi allocation
		AllocationModel allocation = this.region.getAllocationModel();

		if (allocation instanceof GiveUpGiveInAllocationModel) { 
			
			((DefaultLandUseAgent) agent).setRelativeThresholding(((GiveUpGiveInAllocationModel)allocation).relativeThresholding);
 
		}


		return agent;
	}


	public LandUseAgent assembleAgent(Cell homecell, String btLabel, String frLabel,
			String agentLabel) {

		if (region == null) {
			throw new IllegalStateException(
					"Agent assembler has not been initialised!");
		}

		int btId;
		if (!this.region.getBehaviouralTypeMapByLabel().containsKey(btLabel)) {
			logger.warn("Couldn't find BehaviouralType by label: " + btLabel
					+ ". Assigning default!");
			btId = Integer.MIN_VALUE;
		} else {
			btId = this.region.getBehaviouralTypeMapByLabel().get(btLabel)
					.getSerialID();
		}

		int frId;
		if (!this.region.getFunctionalRoleMapByLabel().containsKey(frLabel)) {
			logger.warn("Couldn't find FunctionRole by label: " + frLabel
					+ ". Assigning default!");
			frId = Integer.MIN_VALUE;
		} else {
			frId = this.region.getFunctionalRoleMapByLabel().get(frLabel)
					.getSerialID();
		}

		return this.assembleAgent(homecell, btId, frId, agentLabel);
	}

	/**
	 * @see org.volante.abm.agent.assembler.AgentAssembler#assembleAgent(org.volante.abm.data.Cell,
	 *      int, int, java.lang.String)
	 */
	@Override
	public LandUseAgent assembleAgent(Cell homecell, int btId, int frId) {
		return this.assembleAgent(homecell, btId, frId, null);
	}

	/**
	 * @see org.volante.abm.agent.assembler.AgentAssembler#assembleAgent(org.volante.abm.data.Cell,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public LandUseAgent assembleAgent(Cell homecell, String btLabel, String frLabel) {
		return assembleAgent(homecell, btLabel, frLabel, null);
	}

	/**
	 * @see org.volante.abm.example.allocation.AgentFinder#findAgent(org.volante.abm.data.Cell, int, int)
	 */
	@Override
	public LandUseAgent findAgent(Cell homecell, int btId, int frId) {
		return this.assembleAgent(homecell, btId, frId);
	}
}
