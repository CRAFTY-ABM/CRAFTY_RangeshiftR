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
 * Created by Sascha Holzhauer on 7 Jan 2015
 */
package org.volante.abm.institutions.innovation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.decision.pa.InnovationPa;
import org.volante.abm.institutions.Institution;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.curve.LinearInterpolator;
import com.moseph.modelutils.fastdata.DoubleMap;

/**
 * Adjustments take place as a {@link PreTickAction} which is after
 * initialisation of effective capitals but before adjustments by institutions
 * directly (via {@link Institution#adjustCapitals(Cell)}).
 * 
 * @author Sascha Holzhauer
 * 
 */
public class VariableCapitalLevelInnovation extends Innovation {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(VariableCapitalLevelInnovation.class);


	@ElementList(required = true)
	protected List<String> affectedCapitals = null;

	/**
	 * Increase in case of adoption in level of capital that is specified by
	 * affectedCapital.
	 */
	@Element(name = "capitalFactorCsvFile", required = true)
	protected String capitalFactorCsvFile = "";

	/**
	 * Name of column in CSV file that specifies the year a row belongs to
	 */
	@Element(required = false)
	String tickCol = "Year";

	/**
	 * AFTs that count in the evaluation of social network partners.
	 */
	@Element(required = false)
	protected String affectiveAFTs = "all";

	/**
	 * Adjusts for each AFT the required proportion of adopted among neighbours
	 * to trial/adopt itself; Values &lt; 1 cause the trial/adoption to be
	 * likelier, values &gt; 1 cause the trial/adoption to be less likely.
	 * Default is 1.0
	 */
	@ElementMap(entry = "trialThresholdAdjustment", key = "aft", attribute = true, inline = true, required = false)
	protected Map<String, Double> trialThresholdAdjustment = null;

	/**
	 * Adjusts for each AFT the required proportion of adopted among neighbours
	 * to trial/adopt itself; Values &lt; 1 cause the trial/adoption to be
	 * likelier, values &gt; 1 cause the trial/adoption to be less likely.
	 * Default is 1.0
	 */
	@ElementMap(entry = "adoptionThresholdAdjustment", key = "aft", attribute = true, inline = true, required = false)
	protected Map<String, Double> adoptionThresholdAdjustment = null;

	protected Set<String> affectiveAFTset;

	protected Set<Capital> affectedCapitalObjects;

	Map<Capital, Curve> capitalFactorCurves = new HashMap<Capital, Curve>();

	protected PreTickAction capitalAdjustmentAction = null;


	/**
	 * @param identifier
	 */
	public VariableCapitalLevelInnovation(
			@Attribute(name = "id") String identifier) {
		super(identifier);
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region r)
			throws Exception {
		super.initialise(data, info, r);
		r.setRequiresEffectiveCapitalData();
		this.affectedCapitalObjects = new HashSet<Capital>();
		for (String capital : affectedCapitals) {
			this.affectedCapitalObjects
					.add(modelData.capitals.forName(capital));
		}
		loadCapitalFactorCurves();
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#getWaitingBo(InnovativeBC)
	 */
	@Override
	public InnovationPa getWaitingBo(InnovativeBC ibc) {
		return null;
	}

	/**
	 * Multiplies the generic trial factor with an AFT specific adjustment
	 * factor.
	 * 
	 * @see org.volante.abm.institutions.innovation.Innovation#getTrialThreshold(InnovativeBC)
	 */
	public double getTrialThreshold(InnovativeBC ibc) {
		if (!trialThresholdAdjustment.containsKey(ibc.getAgent().getFC()
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
				* (trialThresholdAdjustment != null ? trialThresholdAdjustment
						.get(ibc.getAgent().getFC().getFR().getLabel()) : 1.0);
	}

	/**
	 * Multiplies the generic adoption factor with an AFT specific adjustment
	 * factor.
	 * 
	 * @see org.volante.abm.institutions.innovation.Innovation#getTrialThreshold(InnovativeBC)
	 */
	public double getAdoptionThreshold(InnovativeBC ibc) {
		if (!trialThresholdAdjustment.containsKey(ibc.getAgent().getFC()
				.getFR()
				.getLabel())) {
			// <- LOGGING
			logger.warn("No social partner share adjustment factor provided for "
					+ ibc.getAgent().getFC().getFR().getLabel()
					+ ". Using 1.0.");
			// LOGGING ->
			return super.getAdoptionThreshold(ibc);
		}
		return super.getAdoptionThreshold(ibc)
				* (adoptionThresholdAdjustment != null ? adoptionThresholdAdjustment
						.get(ibc.getAgent().getFC().getFR().getLabel()) : 1.0);
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#perform(org.volante.abm.agent.bt.InnovativeBC)
	 */
	@Override
	public void perform(final InnovativeBC ibc) {
		this.capitalAdjustmentAction = new PreTickAction() {
			@Override
			public void preTick() {
				for (Cell c : ((LandUseAgent) ibc.getAgent()).getCells()) {
					DoubleMap<Capital> adjusted = modelData.capitalMap();
					c.getEffectiveCapitals().copyInto(adjusted);
					for (Capital capital : affectedCapitalObjects) {
						adjusted.put(
								capital,
								c.getBaseCapitals().getDouble(capital)
										*
								capitalFactorCurves.get(capital).sample(
										rInfo.getSchedule().getCurrentTick()));
					}
					c.setEffectiveCapitals(adjusted);
				}
			}
		};
		this.rInfo.getSchedule().register(capitalAdjustmentAction);
	}

	/**
	 * @see org.volante.abm.institutions.innovation.Innovation#unperform(org.volante.abm.agent.bt.InnovativeBC)
	 */
	@Override
	public void unperform(InnovativeBC ibc) {
		this.rInfo.getSchedule().unregister(capitalAdjustmentAction);
		for (Cell c : ((LandUseAgent) ibc.getAgent()).getCells()) {
			DoubleMap<Capital> adjusted = modelData.capitalMap();
			c.getEffectiveCapitals().copyInto(adjusted);
			for (Capital capital : affectedCapitalObjects) {
				adjusted.put(capital,
						c.getBaseCapitals().getDouble(capital));
			}
			c.setEffectiveCapitals(adjusted);
		}
	}

	/**
	 * @throws IOException
	 */
	void loadCapitalFactorCurves() throws IOException {
		// <- LOGGING
		logger.info("Load capital adjustment factors from "
				+ capitalFactorCsvFile);
		// LOGGING ->

		try {
			Map<String, LinearInterpolator> curves = rInfo.getPersister()
					.csvVerticalToCurves(capitalFactorCsvFile, tickCol,
							affectedCapitals, region.getPersisterContextExtra());
			for (Capital c : modelData.capitals) {
				if (curves.containsKey(c.getName())) {
					capitalFactorCurves.put(c, curves.get(c.getName()));
				}
			}
		} catch (NumberFormatException e) {
			logger.error("A required number could not be parsed from "
					+ capitalFactorCsvFile + ". Make "
					+ "sure the CSV files contains columns "
					+ modelData.services.names());
			throw e;
		}
	}

}
