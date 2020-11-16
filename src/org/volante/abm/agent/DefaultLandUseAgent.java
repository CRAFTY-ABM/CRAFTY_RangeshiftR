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
package org.volante.abm.agent;


import static java.lang.Math.pow;

import org.apache.log4j.Logger;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.fr.LazyFR;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.example.GiveUpGiveInAllocationModel;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.models.nullmodel.NullProductionModel;
import org.volante.abm.param.RandomPa;

import com.moseph.modelutils.Utilities.Score;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * This is a default agent
 * 
 * @author jasper
 * @author seo-b
 */
public class DefaultLandUseAgent extends AbstractLandUseAgent {

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(DefaultLandUseAgent.class);
	
	
	/**
	 * Default absolute thresholding makes difficult to determine the giving-in and giving-up threshold values as the
	 * benefit level changes over time. When relative thresholding is used, it's converted to a proportion of 
	 * the mean benefit value across the current population of agents, which is modelled by the current benefit value of perfect agent
	 * (= perfect cells) and is compared to the benefit values of a cell. 
	 * 
 	 * @see org.volante.abm.example.NormalisedCurveCompetitivenessModel#addUpMarginalUtilities()
 	 * 
	 */
	private boolean relativeThresholding
	; 
	/**
	 * @return the relativeThresholding
	 */
	public boolean isRelativeThresholding() {
		return relativeThresholding;
	}

	/**
	 * @param relativeThresholding the relativeThresholding to set
	 */
	public void setRelativeThresholding(boolean relativeThresholding) {
		this.relativeThresholding = relativeThresholding;
	}

 
	public DefaultLandUseAgent(String id, ModelData data) {
		this(LazyFR.getInstance(), id, data, null,
				NullProductionModel.INSTANCE, -Double.MAX_VALUE,
				Double.MAX_VALUE);
	}

	public DefaultLandUseAgent(FunctionalRole fRole, ModelData data, Region r,
			ProductionModel prod,
			double givingUp, double givingIn) {
		this(fRole, "NA", data, r, prod, givingUp, givingIn);
	}

	public DefaultLandUseAgent(FunctionalRole fRole, String id, ModelData data,
			Region r) {
		this(fRole, id, data, r, fRole.getProduction(), fRole
				.getMeanGivingUpThreshold(),
				fRole.getMeanGivingInThreshold());
	}

	public DefaultLandUseAgent(FunctionalRole fRole, String id, ModelData data,
			Region r, ProductionModel prod, double givingUp, double givingIn) {
		super(r);
		this.id = id;
		this.propertyProvider.setProperty(
				AgentPropertyIds.GIVING_UP_THRESHOLD, givingUp);
		this.propertyProvider.setProperty(
				AgentPropertyIds.GIVING_IN_THRESHOLD, givingIn);
		fRole.assignNewFunctionalComp(this);
		productivity = new DoubleMap<Service>(data.services);


	}

	@Override
	public void updateSupply() {
		this.productivity.clear();
		for (Cell c : cells) {
			this.getProductionModel().production(c, c.getModifiableSupply());

			if (logger.isDebugEnabled()) {
				logger.debug(this + "(cell " + c.getX() + "|" + c.getY() + "): " + c.getModifiableSupply().prettyPrint());
			}
			c.getSupply().addInto(productivity);
		}
	}

	/**
	 * @see org.volante.abm.agent.Agent#getProductionModel()
	 */
	@Override
	public ProductionModel getProductionModel() {
		return this.getFC().getProduction();
	}

 



	@Override
	public void considerGivingUp() {
 		 
		
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this + "> Consider giving up: "
					+ this.getProperty(AgentPropertyIds.COMPETITIVENESS)
					+ " (competitiveness) < "
					+ this.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD)
					+ " (threshold)?");
		}
		// LOGGING ->

 
		double givingUpThreshold =  this.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD);

 		double compThresholdDiff = 0; 

		if (relativeThresholding) { 
			
			/* Use competitiveness of perfect agents (function of residual demand and prescribed production parameter and competitiveness functions). 
			 * It changes over time and does not reflect cell-level capitals. 
			 */
 			
			Cell perfectCell =  ((GiveUpGiveInAllocationModel) this.region.getAllocationModel()).getPerfectCell();

			double compPerfect = this.region.getCompetitiveness(this.getFC().getFR(), perfectCell);
			
 			
			compThresholdDiff = givingUpThreshold * compPerfect  - this.getProperty(AgentPropertyIds.COMPETITIVENESS);
			logger.debug(this + "> Use relative thresholding (compPerfect=" + compPerfect+")");
			
		} else { 
			// Original absolute thresholding
			compThresholdDiff = givingUpThreshold - this.getProperty(AgentPropertyIds.COMPETITIVENESS);  
		}



		if (compThresholdDiff > 0.0) { // try to give-up  
			logger.debug(this + "> compThresholdDiff > 0.0, consider giving-up)");

			double random = this.region.getRandom().getURService().nextDouble(RandomPa.RANDOM_SEED_RUN_GIVINGUP.name());

			double probability = this.getProperty(AgentPropertyIds.GIVING_UP_PROB)
					* Math.pow(
							compThresholdDiff
							/ this.region.getMaxGivingUpThresholdDeviation().get(this.getFC().getFR()),
							this.getProperty(AgentPropertyIds.GIVING_UP_PROB_WEIGHT).doubleValue());
			if (random < probability) {
				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug(this + "> GivingUp (random number: " + random + ", probability: " + probability + ")");
				}
				// LOGGING ->

				giveUp();
			} else {
				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug(this + "> GivingUp rejected! (random number: " + random + ", probability: "
							+ probability + ")");
				}
				// LOGGING ->
			}
		}
	}

	@Override
	public boolean canTakeOver(Cell c, double incoming) {

 
 		double givingInThreshold =  this.getProperty(AgentPropertyIds.GIVING_IN_THRESHOLD);
		double competitiveness = this.getProperty(AgentPropertyIds.COMPETITIVENESS);

		boolean takeover; // able to give in?

		
 		if (relativeThresholding) { 
			
			/* Use competitiveness of perfect agents (function of residual demand and prescribed production parameter and competitiveness functions). 
			 * It changes over time and does not reflect cell-level capitals. 
			 */
			Cell perfectCell =  ((GiveUpGiveInAllocationModel) this.region.getAllocationModel()).getPerfectCell();
			double compPerfect = this.region.getCompetitiveness(this.getFC().getFR(), perfectCell);
			logger.debug(this + "> canTakeOver using relative thresholding (compPerfect=" + compPerfect+")");

			 
			takeover = incoming > (competitiveness + givingInThreshold * compPerfect); // 

			// Could do a direct comparison (x% higher than the current competitiveness  also like (not implemented)  
			// takeover = incoming > (competitiveness * ( 1 + givingInThreshold )); 

		} else { 
			// Original absolute thresholding
			 takeover = incoming > (competitiveness +  givingInThreshold  );  
 
		}


		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this + "> canTakeOver?" + takeover);
		}

		return (takeover);
	}

	@Override
	public UnmodifiableNumberMap<Service> supply(Cell c) {
		DoubleMap<Service> prod = productivity.duplicate();
		this.getFC().getProduction().production(c, prod);
		return prod;
	}

	public void setProductionFunction(ProductionModel f) {
		this.getFC().setProductionFunction(f);
	}

	public ProductionModel getProductionFunction() {
		return this.getFC().getProduction();
	}

	@Override
	public String infoString() {
		return "Giving up: "
				+ this.propertyProvider
				.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD)
				+ ", Giving in: "
				+ this.propertyProvider
				.getProperty(AgentPropertyIds.GIVING_IN_THRESHOLD)
				+ ", nCells: " + cells.size();
	}

	@Override
	public void receiveNotification(
			de.cesr.more.basic.agent.MoreObservingNetworkAgent.NetworkObservation observation,
			Agent object) {
	}

	/**
	 * @see org.volante.abm.agent.Agent#die()
	 */
	@Override
	public void die() {
		// nothing to do
	}
}
