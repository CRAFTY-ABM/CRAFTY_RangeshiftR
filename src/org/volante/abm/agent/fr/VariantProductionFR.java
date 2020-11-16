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
package org.volante.abm.agent.fr;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.models.AgentAwareProductionModel;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.distribution.Distribution;

/**
 * Supports distributions of giving in and giving up thresholds, as well as
 * serviceLevelNoise and capitalImportanceNoise.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class VariantProductionFR extends AbstractFR {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(VariantProductionFR.class);

	protected ModelData data = null;

	@Element(required = false)
	Distribution givingUpDistribution = null;
	@Element(required = false)
	Distribution givingInDistribution = null;

	// These only work with the SimpleProductionModel
	@Element(required = false)
	Distribution serviceLevelNoise = null;
	@Element(required = false)
	Distribution capitalImportanceNoise = null;

	/**
	 * @param label
	 * @param production
	 */
	public VariantProductionFR(@Attribute(name = "label") String label,
			@Element(name = "production") ProductionModel production) {
		super(label, production);
	}

	/**
	 * @param id
	 * @param production
	 * @param givingUp
	 * @param givingIn
	 */
	public VariantProductionFR(String id, ProductionModel production,
			double givingUp, double givingIn) {
		super(id, production, givingUp, givingIn);
	}

	/**
	 * @param id
	 * @param serialId
	 * @param production
	 * @param givingUp
	 * @param givingIn
	 */
	public VariantProductionFR(String id, int serialId,
			ProductionModel production, double givingUp, double givingIn) {
		super(id, serialId, production, givingUp, givingIn);
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region r)
			throws Exception {
		super.initialise(data, info, r);
		this.data = data;

		if (givingUpDistribution != null) {
			this.givingUpDistribution.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
			// make sure that potential agent's GU value correspond to the
			// normal distribution's
			// mean:
			if (this.givingUpDistribution instanceof NormalDistribution) {
				if (this.givingUpMean != ((NormalDistribution) this.givingUpDistribution)
						.getMean()) {
					// <- LOGGING
					logger.warn("Distirbution mean did not correspond to potential agent's value for givingUp threshold: "
							+ "Set givingUp treshold to distribution mean!");
					// LOGGING ->
					this.givingUpMean = ((NormalDistribution) this.givingUpDistribution)
							.getMean();
				}
			}
		}
		if (givingInDistribution != null) {
			this.givingInDistribution.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
			// make sure that potential agent's GI value correspond to the
			// normal distribution's
			// mean:
			if (this.givingInDistribution instanceof NormalDistribution) {
				if (this.givingInMean != ((NormalDistribution) this.givingInDistribution)
						.getMean()) {
					// <- LOGGING
					logger.warn("Distirbution mean did not correspond to potential agent's value for givingIn threshold: "
							+ "Set givingIn treshold to distribution mean!");
					// LOGGING ->
					this.givingInMean = ((NormalDistribution) this.givingInDistribution)
							.getMean();
				}
			}
		}
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#getMeanGivingInThreshold()
	 */
	@Override
	public double getSampledGivingInThreshold() {
		return givingInDistribution == null ? givingInMean : givingInDistribution
				.sample();
	}

	public double getSampledGivingUpThreshold() {
		return givingUpDistribution == null ? givingUpMean : givingUpDistribution
				.sample();
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#assignNewFunctionalComp(org.volante.abm.agent.Agent)
	 */
	@Override
	public Agent assignNewFunctionalComp(Agent agent) {
		agent.setFC(getNewFunctionalComp());
		agent.setProperty(AgentPropertyIds.GIVING_IN_THRESHOLD,
				getSampledGivingInThreshold());
		agent.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD,
				getSampledGivingUpThreshold());

		agent.setProperty(AgentPropertyIds.GIVING_UP_PROB, this.givingUpProbability);
		agent.setProperty(AgentPropertyIds.GIVING_UP_PROB_WEIGHT, this.givingUpProbabilityWeight);
		agent.setProperty(AgentPropertyIds.ALLOCATE_PROB, this.allocationProbability);

		if (agent.getFC().getProduction() instanceof AgentAwareProductionModel) {
			((AgentAwareProductionModel) agent.getFC().getProduction()).setAgent(agent);
		}

		return agent;
	}

	/**
	 * Returns a noisy version of the production model. Uses the
	 * serviceLevelNoise distribution to create variance in the optimal levels
	 * of service production, and capitalImportanceNoise to create variance in
	 * the importance of the capitals to this production.
	 * 
	 * Only works on SimpleProduction models at the moment.
	 * 
	 * @param production
	 * @param r
	 * @return production model
	 */
	public ProductionModel productionModel(final ProductionModel production,
			final Region r) {
		if (!(production instanceof SimpleProductionModel)) {
			return production;
		}

		if (this.serviceLevelNoise != null && !this.serviceLevelNoise.isInitialised()) {
			this.serviceLevelNoise.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
		}

		if (this.capitalImportanceNoise != null && !this.capitalImportanceNoise.isInitialised()) {
			this.capitalImportanceNoise.init(r.getRandom().getURService(),
					RandomPa.RANDOM_SEED_INIT_AGENTS.name());
		}

		return ((SimpleProductionModel) production).copyWithNoise(this.data,
				serviceLevelNoise, capitalImportanceNoise);
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#getNewFunctionalComp()
	 */
	@Override
	public FunctionalComponent getNewFunctionalComp() {
		return new VariantProductionFC(this, productionModel(production,
				this.region));
	}
}
