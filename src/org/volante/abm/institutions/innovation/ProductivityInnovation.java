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
 * Created by Sascha Holzhauer on 06.03.2014
 */
package org.volante.abm.institutions.innovation;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.agent.fr.IndividualProductionFunctionalComponent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.decision.pa.InnovationPa;
import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.example.socialInteraction.NormaliseProductivityRegionHelper;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.PostTickAction;
import org.volante.abm.schedule.RunInfo;

/**
 * Adapts productivity of the specified <code>affectedService</code> by
 * <code>>effectOnProductivityFactor</code>.
 * 
 * @author Sascha Holzhauer
 *
 */
public class ProductivityInnovation extends Innovation implements PostTickAction {

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(ProductivityInnovation.class);

	/**
	 * Specifies for each AFT the required proportions of adopted among
	 * neighbours to adopt itself; Values &gt; 1 cause the trial/adoption to be
	 * likelier, values &lt; 1 cause to adoption to be less likely. Default is
	 * 1.0
	 */
	@ElementMap(entry = "socialPartnerShareAdjustment", key = "aft", attribute = true, inline = true, required = false)
	protected Map<String, Double> socialPartnerShareAdjustment = new HashMap<String, Double>();

	/**
	 * Normalise changes in productivity (subtract the average increase from every agent's
	 * productivity)?
	 */
	@Element(name = "normaliseProductivity", required = false)
	protected Boolean				normaliseProductivity	= false;

	/**
	 * Increase of productivity in case of adoption.
	 */
	@Element(name = "effectOnProductivityFactor", required = false)
	protected double				effectOnProductivityFactor	= 1.002;

	/**
	 * Factor that is applied to {@link #effectOnProductivityFactor} every tick.
	 */
	@Element(name = "effectDiscountFactor", required = false)
	protected double				effectDiscountFactor		= 1.000;

	/**
	 * AFTs that count in the evaluation of social network partners.
	 */
	@Element(required = false)
	protected String				affectiveAFTs			= "all";

	@Element(required = true)
	protected String				affectedServices		= "";


	protected Set<String>			affectiveAFTset;

	protected Set<Service>			affectedServiceSet;


	/**
	 * @param identifier
	 */
	public ProductivityInnovation(@Attribute(name = "id") String identifier) {
		super(identifier);
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		super.initialise(data, info, r);

		this.affectedServiceSet = new HashSet<Service>();
		for (String service : affectedServices.split(",")) {
			service = service.trim();
			this.affectedServiceSet.add(modelData.services.forName(service));
		}

		if (this.normaliseProductivity) {
			for (Service service : this.affectedServiceSet) {
				this.region.registerHelper(this, new NormaliseProductivityRegionHelper(
						this.region, service, this.normaliseProductivity));
			}
		}

		if (this.effectDiscountFactor != 1.0) {
			info.getSchedule().register(this);
		}
	}

	/**
	 * Updates effect on productivity factor by discount factor
	 * 
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void postTick() {
		this.effectOnProductivityFactor = this.effectOnProductivityFactor
				* this.effectDiscountFactor;
	}

	/**
	 * Multiplies the generic adoption factor with AFT specific social partner
	 * share adjustment factor.
	 * 
	 * @see org.volante.abm.institutions.innovation.Innovation#getTrialThreshold(InnovativeBC)
	 */
	public double getTrialThreshold(InnovativeBC ibc) {
		if (!socialPartnerShareAdjustment.containsKey(ibc.getAgent().getFC()
				.getFR()
				.getLabel())) {
			// <- LOGGING
			logger.warn("No social partner share adjustment factor provided for "
					+ ibc.getAgent().getFC().getFR().getLabel()
					+ ". Using 1.0.");
			// LOGGING ->
			return super.getTrialThreshold(ibc);
		}
		return super.getTrialThreshold(ibc)
				* socialPartnerShareAdjustment.get(ibc.getAgent().getFC()
						.getFR()
						.getLabel());
	}

	/**
	 * @param effectOnProductivityFactor
	 */
	public void setEffectOnProductivityFactor(double effectOnProductivityFactor) {
		this.effectOnProductivityFactor = effectOnProductivityFactor;
	}

	/**
	 * @return effect on productivity factor
	 */
	public double getEffectOnProductivityFactor() {
		return this.effectOnProductivityFactor;
	}


	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#perform(org.volante.abm.agent.bt.InnovativeBC)
	 */
	@Override
	public void perform(InnovativeBC ibc) {
		ProductionModel pModel = ibc.getAgent().getFC().getProduction();

		if (!(ibc.getAgent().getFC() instanceof IndividualProductionFunctionalComponent)) {
			logger.warn("The affected functional role is not an IndividualProductionFunctionalComponent, and changes are likely to"
					+ " have side effects on other agents!");
		}

		if (pModel instanceof SimpleProductionModel) {
			for (Service service : this.affectedServiceSet) {
				((SimpleProductionModel) pModel).setWeight(service,
						((SimpleProductionModel) pModel).getProductionWeights().getDouble(
								service) * effectOnProductivityFactor);

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug(ibc
							+ "> New productivity: "
							+ ((SimpleProductionModel) pModel).getProductionWeights().getDouble(
									service) * effectOnProductivityFactor);
				}
				// LOGGING ->
			}
		}
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#unperform(org.volante.abm.agent.bt.InnovativeBC)
	 */
	@Override
	public void unperform(InnovativeBC ibc) {
		ProductionModel pModel = ibc.getAgent().getFC().getProduction();
		if (pModel instanceof SimpleProductionModel) {
			for (Service service : this.affectedServiceSet) {
				((SimpleProductionModel) pModel).setWeight(service,
							((SimpleProductionModel) pModel).getProductionWeights().getDouble(
								service) / effectOnProductivityFactor);

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Unperform " + this);
				}
				// LOGGING ->
			}
		}		
	}

	public double getEffectDiscountFactor() {
		return this.effectDiscountFactor;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#getWaitingBo(org.volante.abm.agent.bt.InnovativeBC)
	 */
	@Override
	public InnovationPa getWaitingBo(InnovativeBC bComp) {
		return null;
	}
}
