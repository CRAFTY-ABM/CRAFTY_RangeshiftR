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


import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Service;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.visualisation.CurveCompetitivenessDisplay;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * A more complex model of competitiveness allowing the applications of functions.
 * 
 * @author dmrust
 * 
 */
public class NormalisedCurveCompetitivenessModel extends CurveCompetitivenessModel {

	/**
	 * Logger
	 */
	static private Logger log = Logger.getLogger(NormalisedCurveCompetitivenessModel.class);

	/**
	 * Residuals are normalised by per cell demand of the particular service. Used to balance differences in services'
	 * dimension before the competition function is applied (therefore, the competition function does not need to take
	 * differences in dimensions into account). Example: If the demand supply gap of cereal is 20% and that of meat is
	 * 50%, the normalised residual is higher for meat, but the absolute residual would be higher for cereal in case the
	 * absolute demand for cereal is much higher.
	 */
	@Attribute(required = false)
	boolean normaliseCellResidual = true;

	/**
	 * Supply as multiplied with the competition curve value is normalised by per cell demand for the particular
	 * service. When true, it is assumed that the value of production is relative to the demand (i.e., it is more
	 * profitable to produce a service whose relative (to demand) cell production is higher, not matter the absolute
	 * production).
	 * 
	 * Actually, a thorough representation would need to consider the market-wide ability to produce the particular
	 * service (which is currently not represented in the CRAFTY framework itself but modelled by the current supply as
	 * subject to competition).
	 */
	@Attribute(required = false)
	boolean normaliseCellSupply = true;

	/**
	 * Adds up marginal utilities (determined by competitiveness for unmet demand) of all services.
	 * 
	 * @param residualDemand
	 * @param supply
	 * @param showWorking
	 *        if true, log details in DEBUG mode
	 * @return summed marginal utilities of all services
	 */
	public double addUpMarginalUtilities(UnmodifiableNumberMap<Service> residualDemand,
	        UnmodifiableNumberMap<Service> supply, boolean showWorking) { // @TODO showWorking is not being used?
		double sum = 0;

		for (Service s : supply.getKeySet()) {
			Curve c = curves.get(s); /* Gets the curve parameters for this service */

			double perCellDemand = region.getDemandModel().getAveragedPerCellDemand().get(s);
			perCellDemand = (perCellDemand == 0) ? Double.MIN_VALUE : perCellDemand;

			if (c == null) {
				String message = "Missing curve for: " + s.getName() + " got: " + curves.keySet();
				log.fatal(message);
				throw new IllegalStateException(message);
			}
			double res = residualDemand.getDouble(s);
			// String message = "residualDemand=" + res + " perCellDemand="+perCellDemand + " in " + s.getName() ;
			// log.info(message);

			if (normaliseCellResidual) {
				res /= perCellDemand;
			}
			// message = "residualDemand/perCellDemand = " + res ;
			// log.info(message);

 
			if (res > 1.0) {
				String message = "residualDemand/perCellDemand > 1 : " + s.getName() + " got: " + curves.keySet()
				        + " res = " + res;
				log.fatal(message);
				throw new IllegalStateException(message);
			}

			double marginal = c.sample(res); /*
			                                  * Get the corresponding 'value' (y-value) for this level of unmet demand
			                                  */
			// message = "marginal = " + marginal;
			// log.info(message);

			double amount = supply.getDouble(s);

			// message = "amount = " + amount;
			// log.info(message);

			if (this.normaliseCellSupply) {
				amount /= perCellDemand;
			}
			// message = "amount/perCellDemand= " + amount;
			// log.info(message);

			if (removeNegative && marginal < 0) {
				marginal = 0;
			}

			double comp = ((marginal == 0 || amount == 0) ? 0 : marginal * amount);

			if (log.isTraceEnabled() || (log.isDebugEnabled() && removeNegative && comp < 0)) {
				log.debug(String.format(
				        "\t\tService %10s: Residual (%5f) > Marginal (%5f; Curve: %s) * Amount (%5f) = %5f",
				        s.getName(), res, marginal, c.toString(), amount, marginal * amount));
			}
			// message = "comp = " + comp;
			// log.info(message);

			sum += comp;
		}
		log.trace("Competitiveness sum: " + sum);

		return sum;
	}

	@Override
	public CurveCompetitivenessDisplay getDisplay() {
		return new CurveCompetitivenessDisplay(this);
	}

	/**
	 * @see org.volante.abm.models.CompetitivenessModel#getDeepCopy()
	 * 
	 *      TODO test
	 */
	@Override
	public CompetitivenessModel getDeepCopy() {
		NormalisedCurveCompetitivenessModel copy = new NormalisedCurveCompetitivenessModel();
		for (Entry<Service, Curve> entry : this.curves.entrySet()) {
			copy.curves.put(entry.getKey(), entry.getValue());
		}
		copy.data = this.data;
		copy.info = this.info;
		copy.region = this.region;

		copy.serviceColumn = this.serviceColumn;
		copy.slopeColumn = this.slopeColumn;
		copy.interceptColumn = this.interceptColumn;
		copy.linearCSV = this.linearCSV;
		copy.removeCurrentLevel = this.removeCurrentLevel;
		copy.removeNegative = this.removeNegative;

		copy.normaliseCellResidual = this.normaliseCellResidual;
		copy.normaliseCellSupply = this.normaliseCellSupply;

		return copy;
	}
}
