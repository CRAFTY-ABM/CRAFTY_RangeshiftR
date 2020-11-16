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
 * Created by Sascha Holzhauer on 10 Dec 2014
 */
package org.volante.abm.institutions.innovation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.decision.pa.InnovationPa;
import org.volante.abm.institutions.innovation.repeat.CsvCapitalLevelInnovationRepComp;
import org.volante.abm.institutions.innovation.repeat.InnovationRepComp;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;

/**
 * @author Sascha Holzhauer
 *
 */
public class RepeatingCapitalLevelInnovation extends Innovation implements
		RepeatingInnovation {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(RepeatingCapitalLevelInnovation.class);

	/**
	 * 
	 */
	@Element(name = "repComp", required = false)
	protected InnovationRepComp repComp = new CsvCapitalLevelInnovationRepComp();

	@Element(required = true)
	protected String affectedCapital = "";

	/**
	 * Increase in case of adoption in level of capital that is specified by
	 * affectedCapital.
	 */
	@Element(name = "effectOnCapitalFactor", required = false)
	protected double effectOnCapitalFactor = 1.002;

	/**
	 * AFTs that count in the evaluation of social network partners.
	 */
	@Element(required = false)
	protected String affectiveAFTs = "all";

	/**
	 * Specifies for each AFT the required proportions of adopted among
	 * neighbours to adopt itself; Values &gt; 1 cause the trial/adoption to be
	 * likelier, values &lt; 1 cause to adoption to be less likely. Default is
	 * 1.0
	 */
	@ElementMap(entry = "socialPartnerShareAdjustment", key = "aft", attribute = true, inline = true, required = false)
	protected Map<String, Double> socialPartnerShareAdjustment = new HashMap<String, Double>();

	protected Set<String> affectiveAFTset;

	protected Capital affectedCapitalObject;


	/**
	 * @param identifier
	 */
	public RepeatingCapitalLevelInnovation(
			@Attribute(name = "id") String identifier) {
		super(identifier);
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData mData, RunInfo rInfo, Region region)
			throws Exception {
		super.initialise(mData, rInfo, region);
		region.setRequiresEffectiveCapitalData();
		this.repComp.initialise(mData, rInfo, region);

		affectedCapital = affectedCapital.trim();
		this.affectedCapitalObject = mData.capitals.forName(affectedCapital);
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
	 * @see org.volante.abm.institutions.innovation.RepeatingInnovation#getNewInnovation()
	 */
	@Override
	public RepeatingInnovation getNewInnovation() {
		RepeatingCapitalLevelInnovation innovation = new RepeatingCapitalLevelInnovation(
				"none");
		innovation.adoptionThreshold = adoptionThreshold;
		innovation.affectedCapital = affectedCapital;
		innovation.affectedAFTs = affectedAFTs;
		innovation.affectiveAFTs = affectiveAFTs;
		innovation.identifier = identifier + "_"
				+ rInfo.getSchedule().getCurrentTick();
		innovation.lifeSpan = lifeSpan;
		innovation.trialThreshold = trialThreshold;
		innovation.repComp = repComp;
		innovation.socialPartnerShareAdjustment = socialPartnerShareAdjustment;
		innovation.effectOnCapitalFactor = effectOnCapitalFactor;
		try {
			innovation.initialise(modelData, rInfo, region);
		} catch (Exception exception) {
			logger.error("Error during initialisation of renewed innovation!");
			exception.printStackTrace();
		}
		innovation = this.getRepetitionComp().adjustRenewedInnovation(
				innovation);
		return innovation;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.RepeatingInnovation#getRepetitionComp()
	 */
	@Override
	public InnovationRepComp getRepetitionComp() {
		return this.repComp;
	}


	/**
	 * OPerates on the cell's base capital levels!
	 * 
	 * @see org.volante.abm.institutions.innovation.Innovation#perform(org.volante.abm.agent.bt.InnovativeBC)
	 */
	@Override
	public void perform(InnovativeBC ibc) {
		for (Cell c : ((LandUseAgent) ibc.getAgent()).getCells()) {
			DoubleMap<Capital> adjusted = modelData.capitalMap();
			c.getBaseCapitals().copyInto(adjusted);
			adjusted.put(this.affectedCapitalObject, adjusted.get(affectedCapitalObject) * this.effectOnCapitalFactor);
			c.setBaseCapitals(adjusted);
		}
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#unperform(org.volante.abm.agent.bt.InnovativeBC)
	 */
	@Override
	public void unperform(InnovativeBC ibc) {
		for (Cell c : ((LandUseAgent) ibc.getAgent()).getCells()) {
			DoubleMap<Capital> adjusted = modelData.capitalMap();
			c.getBaseCapitals().copyInto(adjusted);
			adjusted.put(this.affectedCapitalObject, adjusted.get(affectedCapitalObject) / this.effectOnCapitalFactor);
			c.setBaseCapitals(adjusted);
		}
	}

	public double getEffectOnCapitalFactor() {
		return effectOnCapitalFactor;
	}

	public void setEffectOnCapitalFactor(double effectOnCapitalFactor) {
		this.effectOnCapitalFactor = effectOnCapitalFactor;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#getWaitingBo(org.volante.abm.agent.bt.InnovativeBC)
	 */
	@Override
	public InnovationPa getWaitingBo(InnovativeBC bComp) {
		return null;
	}
}
