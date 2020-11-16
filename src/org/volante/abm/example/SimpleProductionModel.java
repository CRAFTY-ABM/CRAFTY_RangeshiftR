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

import static java.lang.Math.pow;
import static org.volante.abm.example.SimpleCapital.simpleCapitals;
import static org.volante.abm.example.SimpleService.simpleServices;

import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.models.utils.ProductionWeightReporter;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.distribution.Distribution;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.DoubleMatrix;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

/**
 * Simple exponential multiplicative function, i.e.:
 * 
 * p_s = p_max * c_1 ^ w_1 * c_2 ^ w_2 *...*c_n ^ w_n
 * @author dmrust
 *
 */
public class SimpleProductionModel implements ProductionModel, ProductionWeightReporter, DuplicatableProductionModel
{

	/**
	 * Logger
	 */
	static private Logger			logger				= Logger.getLogger(SimpleProductionModel.class);

	protected DoubleMatrix<Capital, Service> capitalWeights =
			new DoubleMatrix<Capital, Service>( simpleCapitals, simpleServices );
	protected DoubleMap<Service> productionWeights = new DoubleMap<Service>(simpleServices, 1);
	
	@Attribute(required=false)
	String csvFile = null;
	
	/**
	 * If true, the noise term is not added to production weights but multiplied when updating production weights.
	 */
	@Attribute(required = false)
	protected boolean multiplyProductionNoise = false;

	/**
	 * If true, prevents the introduction of negative weights via noise. In case a capital weight would become negative,
	 * it is set to zero. Useful since negative capital weights lead to infinity when the capital is zero.
	 */
	@Attribute(required = false)
	protected boolean preventNegativeCapitalWeights = true;

	@Attribute(required = false)
	String doubleFormat = "0.000";
	
	public SimpleProductionModel() {}
	/**
	 * Takes an array of capital weights, in the form:
	 * {
	 * 	{ c1s1, c2s1 ... } //Weights for service 1
	 * 	{ c1s2, c2s2 ... } //Weights for service 2
	 *  ...
	 * i.e. first index is Services, second is baseCapitals
	 * @param weights
	 * @param productionWeights
	 */
	public SimpleProductionModel( double[][] weights, double[] productionWeights )
	{
		this.capitalWeights.putT(weights);
		this.productionWeights.put( productionWeights );
	}
	
	/**
	 * Initialises capital weights and production weights only when not already
	 * compliant with model data.
	 * 
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise( ModelData data, RunInfo info, Region r ) throws Exception
	{
		if( csvFile != null ) {
			initWeightsFromCSV(data, info, r);
		} else
		{
			if (!capitalWeights.rows().equals(data.services)
					|| !capitalWeights.cols().equals(data.capitals)) {
				capitalWeights = new DoubleMatrix<Capital, Service>(
						data.capitals, data.services);
			}

			if (!productionWeights.getKeySet().equals(data.services)) {
				productionWeights = new DoubleMap<Service>(data.services);
			}
		}
	}
	
	void initWeightsFromCSV(ModelData data, RunInfo info, Region region) throws Exception
	{
		capitalWeights = info.getPersister().csvToMatrix(csvFile, data.capitals, data.services,
				region != null ? region.getPersisterContextExtra() : null);
		productionWeights = info.getPersister().csvToDoubleMap(csvFile, data.services,
				"Production", region != null ? region.getPersisterContextExtra() : null);
	}
	
	/**
	 * Sets the effect of a capital on provision of a service
	 * @param c
	 * @param s
	 * @param weight
	 */
	public void setWeight( Capital c, Service s, double weight )
	{
		capitalWeights.put( c, s, weight );
	}
	/**
	 * Sets the maximum level for a service
	 * @param s
	 * @param weight
	 */
	public void setWeight( Service s, double weight )
	{
		productionWeights.put( s, weight );
	}
	
	public UnmodifiableNumberMap<Service> getProductionWeights() { return productionWeights; }
	public DoubleMatrix<Capital, Service> getCapitalWeights() { return capitalWeights; }
	
	@Override
	public void production( Cell cell, DoubleMap<Service> production )
	{
		UnmodifiableNumberMap<Capital> capitals = cell.getEffectiveCapitals();
		production(capitals, production, cell);
	}

	public void production( UnmodifiableNumberMap<Capital> capitals, DoubleMap<Service> production) {
		production( capitals, production , null);
	}
	public void production( UnmodifiableNumberMap<Capital> capitals, DoubleMap<Service> production, Cell cell)
	{
		if (logger.isDebugEnabled() && cell != null) {
			StringBuffer buffer = new StringBuffer();
			DecimalFormat format = new DecimalFormat(doubleFormat);

			if (cell != null) {
				buffer.append("Cell " + cell.getX() + "|" + cell.getY() + " ");
			}
			buffer.append("(" + cell.getOwnersFrLabel() + ") ");
			buffer.append("Production: ");

			for( Service s : capitalWeights.rows() )
			{
				buffer.append(System.getProperty("line.separator") + " Service " + s + "> ");

				double val = 1;
				for( Capital c : capitalWeights.cols() ) {
					buffer.append(format.format(capitals.getDouble(c)) + "^" + capitalWeights.get(c, s) + " * ");

					val = val * pow( capitals.getDouble( c ), capitalWeights.get( c, s ) ) ;
				}
				buffer.append(format.format(productionWeights.get(s)) + " = " + productionWeights.get(s) * val + " ");

				production.putDouble( s, productionWeights.get(s) * val);
			}
			logger.debug(buffer.toString());

		} else {
			for( Service s : capitalWeights.rows() )
			{
				double val = 1;
				for( Capital c : capitalWeights.cols() ) {
					val = val * pow( capitals.getDouble( c ), capitalWeights.get( c, s ) ) ;
					
					if (Double.isInfinite(val) || Double.isNaN(val)) {
						logger.error("Production for " + s + " at cell " + cell + " (" + val
								+ ") became Infinity or NaN after processing capital " + c + " (capital value: "
								+ capitals.getDouble(c) + "/capital weight: " + capitalWeights.get(c, s) + "!");
						break;
					}
				}
				production.putDouble( s, productionWeights.get(s) * val );
			}
		}
	}
	
	@Override
	public String toString()
	{
		return "Production Weights: " + productionWeights.prettyPrint() + "\nCapital Weights:"+capitalWeights.toMap();
	}

	/**
	 * Creates a copy of this model, but with noise added to either the
	 * production weights or the importance weights. Either or both
	 * distributions can be null for zero noise
	 * 
	 * @param data
	 * @param production
	 * @param importance
	 * @return new production model
	 */
	public SimpleProductionModel copyWithNoise(ModelData data, Distribution production,
			Distribution importance)
	{
		SimpleProductionModel pout = new SimpleProductionModel();

		pout.csvFile = csvFile;
		pout.doubleFormat = doubleFormat;

		pout.capitalWeights = capitalWeights.duplicate();
		pout.productionWeights = productionWeights.duplicate();
		pout.multiplyProductionNoise = this.multiplyProductionNoise;
		pout.preventNegativeCapitalWeights = this.preventNegativeCapitalWeights;

		for( Service s : data.services )
		{
			// if there is no production, it remains no production:
			if (production == null || productionWeights.getDouble(s) == 0.0) {
				pout.setWeight( s, productionWeights.getDouble( s ) );
			} else {
				double randomSample = production.sample();
				pout.setWeight(s, this.multiplyProductionNoise ? this.productionWeights.getDouble(s) * randomSample
						: this.productionWeights.getDouble(s) + randomSample);

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Random sample: " + randomSample);
				}
				// LOGGING ->
			}
			
			for( Capital c : data.capitals )
			{
				// if there is no sensitivity, it remains no sensitivity:
				if (importance == null || capitalWeights.get(c, s) == 0.0) {
					pout.setWeight( c, s, capitalWeights.get( c, s ) );
				} else {
					double randomSample = importance.sample();
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
		return pout;
	}

	/**
	 * Creates a new instance of {@link SimpleProductionModel} and copied capital weights and
	 * production weights. CsvFile is not required after initialisation.
	 * 
	 * @return exact copy of this production model
	 */
	public SimpleProductionModel copyExact() {
		SimpleProductionModel pout = new SimpleProductionModel();
		pout.capitalWeights = capitalWeights.duplicate();
		capitalWeights.copyInto(pout.capitalWeights);
		pout.productionWeights = productionWeights.copy();
		return pout;
	}
}
	