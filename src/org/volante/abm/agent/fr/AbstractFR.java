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
 * Created by Sascha Holzhauer on 18 Mar 2015
 */
package org.volante.abm.agent.fr;


import java.util.HashSet;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.FunctionalRoleProductionObserver;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.models.nullmodel.NullProductionModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * @author Sascha Holzhauer
 * 
 */
public abstract class AbstractFR implements FunctionalRole {

	protected Region region;

	@Attribute(required = true)
	protected String label = "NN";

	@Attribute
	protected int serialID = UNKNOWN_SERIAL;

	@Element
	protected ProductionModel production = NullProductionModel.INSTANCE;

	@Attribute(required = true)
	protected double givingUpMean = -Double.MAX_VALUE;
	@Attribute(required = true)
	protected double givingInMean = -Double.MAX_VALUE;

	@Attribute(required = false)
	protected double allocationProbability = 1.0;

	@Attribute(required = false)
	protected double givingUpProbability = 1.0;

	@Attribute(required = false)
	protected double givingUpProbabilityWeight = 0;

	@Element(required = false)
	protected String description = "";

	protected boolean initialised = false;

	protected Set<FunctionalRoleProductionObserver> productionObserver = new HashSet<>();

	public AbstractFR(String id, ProductionModel production) {
		this.label = id;
		this.production = production;
	}

	public AbstractFR(String id, ProductionModel production, double givingUp, double givingIn) {
		this(id, UNKNOWN_SERIAL, production, givingUp, givingIn);
	}

	public AbstractFR(String id, int serialId, ProductionModel production, double givingUp, double givingIn) {
		this(id, production);
		this.givingInMean = givingIn;
		this.givingUpMean = givingUp;
		this.serialID = serialId;
	}

	public void initialise(ModelData data, RunInfo info, Region region) throws Exception {
		this.region = region;
		production.initialise(data, info, region);
		this.initialised = true;
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#isInitialised()
	 */
	public boolean isInitialised() {
		return this.initialised;
	}

	
	
	// #### todo print out gi threshold property at the end of the timestep  
	
	
	/**
	 * TODO test!
	 * 
	 * @see org.volante.abm.agent.fr.FunctionalRole#assignNewFunctionalComp(org.volante.abm.agent.Agent)
	 */
	@Override
	public Agent assignNewFunctionalComp(Agent agent) {
		agent.setFC(this.getNewFunctionalComp());

		if (!agent.isProvided(AgentPropertyIds.FORBID_GIVING_IN_THRESHOLD_OVERWRITE)
				|| Double.isNaN(agent.getProperty(AgentPropertyIds.FORBID_GIVING_IN_THRESHOLD_OVERWRITE))
				|| (agent.getProperty(AgentPropertyIds.FORBID_GIVING_IN_THRESHOLD_OVERWRITE) < 1 && agent
						.getProperty(AgentPropertyIds.GIVING_IN_THRESHOLD) > this.givingInMean)) {
			agent.setProperty(AgentPropertyIds.GIVING_IN_THRESHOLD, this.givingInMean);
		}
		if (!agent.isProvided(AgentPropertyIds.FORBID_GIVING_UP_THRESHOLD_OVERWRITE)
				|| Double.isNaN(agent.getProperty(AgentPropertyIds.FORBID_GIVING_UP_THRESHOLD_OVERWRITE))
				|| (agent.getProperty(AgentPropertyIds.FORBID_GIVING_UP_THRESHOLD_OVERWRITE) < 1 && agent
						.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD) < this.givingUpMean)) {
			agent.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD, this.givingUpMean);
		}
		agent.setProperty(AgentPropertyIds.GIVING_UP_PROB, this.givingUpProbability);
		agent.setProperty(AgentPropertyIds.GIVING_UP_PROB_WEIGHT, this.givingUpProbabilityWeight);
		agent.setProperty(AgentPropertyIds.ALLOCATE_PROB, this.allocationProbability);

		return agent;
	}

	@Override
	public DoubleMap<Service> getExpectedSupply(Cell cell) {
		DoubleMap<Service> map = cell.getRegion().getModelData().serviceMap();
		production.production(cell, map);
		return map;
	}

	/**
	 * In case the production model is changed, a call to {@link AbstractFR#productionModelChanged()} is mandatory!
	 * 
	 * TODO make {@link ProductionModel} immutable
	 * 
	 * @see org.volante.abm.agent.fr.FunctionalRole#getProduction()
	 */
	public ProductionModel getProduction() {
		return this.production;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int getSerialID() {
		return serialID;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return String.format("Functional role: %s", label);
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#getMeanGivingInThreshold()
	 */
	@Override
	public double getMeanGivingInThreshold() {
		return this.givingInMean;
	}

	public double getMeanGivingUpThreshold() {
		return this.givingUpMean;
	}
	
	public double getAllocationProbability() {
		return this.allocationProbability;
	}
	
	public void productionModelChanged() {
		for (FunctionalRoleProductionObserver observer : this.productionObserver) {
			observer.functionalRoleProductionChanged(this);
		}
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#registerFunctionalRoleProductionObserver(org.volante.abm.agent.FunctionalRoleProductionObserver)
	 */
	@Override
	public void registerFunctionalRoleProductionObserver(FunctionalRoleProductionObserver observer) {
		this.productionObserver.add(observer);
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#removeFunctionalRoleProductionObserver(org.volante.abm.agent.FunctionalRoleProductionObserver)
	 */
	@Override
	public void removeFunctionalRoleProductionObserver(FunctionalRoleProductionObserver observer) {
		this.productionObserver.remove(observer);
	}
}
