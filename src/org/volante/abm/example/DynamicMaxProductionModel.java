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
 * Created by Sascha Holzhauer on 12 Oct 2015
 */
package org.volante.abm.example;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.nfunk.jep.JEP;
import org.nfunk.jep.ParseException;
import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.agent.property.PropertyRegistry;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.util.DeepCopyJEP;
import org.volante.abm.models.AgentAwareProductionModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.distribution.Distribution;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * @author Sascha Holzhauer
 *
 */
public class DynamicMaxProductionModel extends SimpleProductionModel implements AgentAwareProductionModel {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(DynamicMaxProductionModel.class);

	/**
	 * Sets {@link JEP#setImplicitMul(boolean)} to <code>TRUE</code>.
	 */
	@Attribute(required = false)
	boolean allowImplicitMultiplication = true;

	/**
	 * Set this true when changes to the parser on an individual bases are indented.
	 */
	@Attribute(required = false)
	boolean copyProductionFunctionParser = false;

	protected Map<Service, DeepCopyJEP> maxProductionParsers = new HashMap<>();
	
	protected Region region = null;
	
	protected Agent agent = null;

	/**
	 * lazy initialisation: call {@link DynamicMaxProductionModel#getMaxProductionFunctions()}!
	 */
	protected Map<String, String> maxProductionFunctions = null;

	/**
	 * Since productions weights are reinitialised by the functions parser regularly, a possible noise term needs to be
	 * added explicitly after that.
	 */
	protected Map<Service, Double> productionNoise;

	protected RunInfo rInfo;

	/**
	 * Default constructor
	 */
	public DynamicMaxProductionModel() {
		productionNoise = new HashMap<>();
	}

	/**
	 * Takes an array of capital weights, in the form: { { c1s1, c2s1 ... } //Weights for service 1 { c1s2, c2s2 ... }
	 * //Weights for service 2 ... i.e. first index is Services, second is baseCapitals
	 * 
	 * @param weights
	 * @param productionWeights
	 */
	public DynamicMaxProductionModel(double[][] weights, double[] productionWeights) {
		this.capitalWeights.putT(weights);
		this.productionWeights.put(productionWeights);
		productionNoise = new HashMap<>();
	}

	/**
	 * @see org.volante.abm.example.SimpleProductionModel#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		super.initialise(data, info, r);
		this.rInfo = info;
		this.initMaxProductionParsersFromCSV(data, info, r);
		for (Service service : data.services) {
			productionNoise.put(service, new Double(0));
		}
	}

	protected Map<String, String> getMaxProductionFunctions() {
		if (this.maxProductionFunctions == null) {
			try {
				this.maxProductionFunctions =
						this.rInfo.getPersister().csvToStringMap(csvFile, "Service", "Production",
								this.region != null ? this.region.getPersisterContextExtra() : null);
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		return this.maxProductionFunctions;
	}

	/**
	 * Avoids reading production weights.
	 * 
	 * @see org.volante.abm.example.SimpleProductionModel#initWeightsFromCSV(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	void initWeightsFromCSV(ModelData data, RunInfo info, Region region) throws Exception {
		capitalWeights =
				info.getPersister().csvToMatrix(csvFile, data.capitals, data.services,
						region != null ? region.getPersisterContextExtra() : null);

		productionWeights = new DoubleMap<Service>(data.services);
	}

	/**
	 * Parses String in column "Production" with JEP function parser.
	 * 
	 * @see "http://www.cse.msu.edu/SENS/Software/jep-2.23/doc/website/"
	 * 
	 * @param data
	 * @param info
	 * @param region
	 * @throws Exception
	 */
	protected void initMaxProductionParsersFromCSV(ModelData data, RunInfo info, Region region) throws Exception {
		this.region = region;

		for (Service service : data.services) {
			DeepCopyJEP productionParser = new DeepCopyJEP();
			productionParser.addStandardFunctions();
			productionParser.addStandardConstants();

			productionParser.setImplicitMul(allowImplicitMultiplication);

			for (Capital capital : data.capitals) {
				productionParser.addVariable(capital.getName(), 0);
			}
			productionParser.addVariable("CTICK", 0);
			
			for (PropertyId propid : PropertyRegistry.getPropertyIds()) {
				productionParser.addVariable(propid.toString(), 0);
			}
			
			this.addVariablesHook(productionParser);

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Parse function '" + getMaxProductionFunctions().get(service.getName()) + "' for service "
						+ service.getName() + "...");
			}
			// LOGGING ->

			productionParser.parseExpression(getMaxProductionFunctions().get(service.getName()));

			if (productionParser.hasError()) {
				logger.error("Error while parsing maximum production function: " + productionParser.getErrorInfo());
				throw new IllegalStateException("Error while parsing maximum production function.");
			}

			maxProductionParsers.put(service, productionParser);
		}
	}

	/**
	 * Can be overridden to add variables to the production parser.
	 * 
	 * @param productionParser
	 */
	public void addVariablesHook(JEP productionParser) {
		// nothing to do here
	}

	/**
	 * Updates variables in maximum production function and puts function values in <code>productionWeights</code>.
	 * Considers {@link SimpleProductionModel#multiplyProductionNoise} when updating production weights.
	 * 
	 * @see org.volante.abm.example.SimpleProductionModel#production(com.moseph.modelutils.fastdata.UnmodifiableNumberMap,
	 *      com.moseph.modelutils.fastdata.DoubleMap, org.volante.abm.data.Cell)
	 */
	public void production(UnmodifiableNumberMap<Capital> capitals, DoubleMap<Service> production, Cell cell) {
		updateProductionWeigths(capitals, cell);
		basicProduction(capitals, production, cell);
	}

	public void basicProduction(UnmodifiableNumberMap<Capital> capitals, DoubleMap<Service> production, Cell cell) {
		super.production(capitals, production, cell);
	}

	/**
	 * Considers {@link SimpleProductionModel#multiplyProductionNoise}.
	 * 
	 * @param capitals
	 */
	protected void updateProductionWeigths(UnmodifiableNumberMap<Capital> capitals, Cell cell) {
		for (Service service : capitalWeights.rows()) {
			for (Capital capital : capitals.getKeySet()) {
				maxProductionParsers.get(service).addVariable(capital.getName(), capitals.getDouble(capital));
			}
			maxProductionParsers.get(service).addVariable("CTICK", rInfo.getSchedule().getCurrentTick());

			for (PropertyId propid : PropertyRegistry.getPropertyIds()) {
				maxProductionParsers.get(service).addVariable(
				        propid.toString(),
				        this.getAgent() != null && this.getAgent().isProvided(propid) ? this.getAgent().getProperty(
				                propid) : 0);
			}

			productionWeights.put(
					service,
					this.multiplyProductionNoise ? maxProductionParsers.get(service).getValue()
			        * productionNoise.get(service)
			        : (maxProductionParsers.get(service).getValue() != 0 ? maxProductionParsers.get(service).getValue()
			                + productionNoise.get(service) : 0.0));

			if (maxProductionParsers.get(service).hasError()) {
				logger.error("Error while parsing maximum production function: "
						+ maxProductionParsers.get(service).getErrorInfo());
				throw new IllegalStateException("Error while parsing maximum production function.");
			}
		}
	}

	public DynamicMaxProductionModel copyWithNoise(ModelData data, Distribution production, Distribution importance) {
		DynamicMaxProductionModel pout = new DynamicMaxProductionModel();

		fillCopyWithNoise(data, production, importance, pout);
		return pout;
	}

	/**
	 * Considers {@link SimpleProductionModel#preventNegativeCapitalWeights}.
	 * 
	 * @param data
	 * @param productionDist
	 * @param importanceDist
	 * @param pout
	 */
	protected void fillCopyWithNoise(ModelData data, Distribution productionDist, Distribution importanceDist,
			DynamicMaxProductionModel pout) {
		pout.allowImplicitMultiplication = this.allowImplicitMultiplication;
		pout.multiplyProductionNoise = this.multiplyProductionNoise;
		pout.preventNegativeCapitalWeights = this.preventNegativeCapitalWeights;

		pout.csvFile = this.csvFile;
		pout.doubleFormat = this.doubleFormat;
		pout.rInfo = this.rInfo;

		pout.capitalWeights = capitalWeights.duplicate();
		pout.productionWeights = productionWeights.duplicate();

		for (Service s : data.services) {
			if (productionDist == null) {
				pout.productionNoise.put(s, 0.0);
			} else {
				double randomSample = productionDist.sample();
				pout.productionNoise.put(s, randomSample);

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Production noise random sample: " + randomSample);
				}
				// LOGGING ->
			}

			for (Capital c : data.capitals) {
				// if there is no sensitivity, it remains no sensitivity:
				if (importanceDist == null || capitalWeights.get(c, s) == 0.0) {
					pout.setWeight(c, s, capitalWeights.get(c, s));
				} else {
					double randomSample = importanceDist.sample();
					double noisyWeight = capitalWeights.get(c, s) + randomSample;
					pout.setWeight(c, s, noisyWeight < 0 && this.preventNegativeCapitalWeights ? 0 : noisyWeight);

					// <- LOGGING
					if (noisyWeight < 0 && !this.preventNegativeCapitalWeights) {
						logger.warn("Negative weight for capital " + c + " set! Noise term: " + randomSample);
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Capital "
								+ c
								+ ": "
								+ (noisyWeight < 0 && this.preventNegativeCapitalWeights ? "Capital weight set to 0."
										: "") + "Random sample: " + randomSample);
					}
					// LOGGING ->
				}
			}
		}

		// copy function parsers:
		if (copyProductionFunctionParser) {
			for (Entry<Service, DeepCopyJEP> entry : this.maxProductionParsers.entrySet()) {
				DeepCopyJEP parser;
				try {
					parser = entry.getValue().deepCopyJepParser();
					if (parser.getTopNode().jjtGetNumChildren() == 0) {

						parser.parseExpression(getMaxProductionFunctions().get(entry.getKey().getName()));
					}
					pout.maxProductionParsers.put(entry.getKey(), parser);
				} catch (ParseException exception) {
					exception.printStackTrace();
				}
			}
		} else {
			pout.maxProductionParsers = this.maxProductionParsers;
		}
	}

	/**
	 * @see org.volante.abm.example.SimpleProductionModel#copyExact()
	 */
	public SimpleProductionModel copyExact() {
		DynamicMaxProductionModel pout = new DynamicMaxProductionModel();
		pout.capitalWeights = capitalWeights.duplicate();
		capitalWeights.copyInto(pout.capitalWeights);
		pout.productionWeights = productionWeights.copy();

		pout.allowImplicitMultiplication = this.allowImplicitMultiplication;
		pout.productionNoise = (Map<Service, Double>) ((HashMap) this.productionNoise).clone();

		
		// copy function parsers:
		try {
			for (Entry<Service, DeepCopyJEP> entry : this.maxProductionParsers.entrySet()) {
				pout.maxProductionParsers.put(entry.getKey(), entry.getValue().deepCopyJepParser());
			}
		} catch (ParseException exception) {
			exception.printStackTrace();
			}

		return pout;
	}

	/**
	 * @see org.volante.abm.models.AgentAwareProductionModel#setAgent(org.volante.abm.agent.Agent)
	 */
	@Override
	public void setAgent(Agent agent) {
		this.agent = agent;
	}

	/**
	 * @see org.volante.abm.models.AgentAwareProductionModel#getAgent()
	 */
	@Override
	public Agent getAgent() {
		return this.agent;
	}
}
