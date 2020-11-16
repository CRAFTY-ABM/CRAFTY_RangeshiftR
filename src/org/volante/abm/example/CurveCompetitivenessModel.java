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


import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.core.Persist;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.visualisation.CurveCompetitivenessDisplay;

import com.csvreader.CsvReader;
import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.curve.LinearFunction;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * A more complex model of competitiveness allowing the applications of functions.
 * 
 * @author dmrust
 * 
 */
public class CurveCompetitivenessModel implements CompetitivenessModel {
	/**
	 * If set to true, then the current supply will be added back to the residual demand, so
	 * competitiveness is calculated as if the cell is currently empty
	 */
	@Attribute(required = false)
	boolean				removeCurrentLevel	= false;

	/**
	 * If set to true, all negative demand (i.e. oversupply) is removed from the dot product
	 */
	@Attribute(required = false)
	boolean				removeNegative		= false;

	/**
	 * A set of curves which are loaded in
	 */
	@ElementMap(inline = true, entry = "curve", attribute = true, required = false, key = "service")
	Map<String, Curve> serialCurves = new LinkedHashMap<>();

	/**
	 * If this points to a csv file with the columns "Service","Intercept","Slope" this will be
	 * loaded as a set of linear functions with the given parameters
	 */
	@Attribute(required = false)
	String				linearCSV			= null;
	@Attribute(required = false)
	String				serviceColumn		= "Service";
	@Attribute(required = false)
	String				interceptColumn		= "Intercept";
	@Attribute(required = false)
	String				slopeColumn			= "Slope";

	Map<Service, Curve> curves = new LinkedHashMap<>();

	Logger				log					= Logger.getLogger(getClass());
	ModelData			data				= null;
	RunInfo				info				= null;
	Region				region				= null;

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.data = data;
		this.info = info;
		this.region = extent;
		if (linearCSV != null) {
			loadLinearCSV(linearCSV);
		}
		for (String s : serialCurves.keySet()) {
			Service service = data.services.forName(s);
			Curve c = serialCurves.get(s);
			if (service != null) {
				curves.put(service, c);
			} else {
				log.error("Invalid Service: " + s + " got: " + data.services);
			}
			log.info("Loaded curve for " + service + ": " + c);
		}
	}

	/**
	 * @see org.volante.abm.models.CompetitivenessModel#getCompetitiveness(org.volante.abm.models.DemandModel,
	 *      com.moseph.modelutils.fastdata.UnmodifiableNumberMap)
	 */
	@Override
	public double getCompetitiveness(DemandModel demand, UnmodifiableNumberMap<Service> supply) {
		return getCompetitveness(demand, supply, false);
	}

	/**
	 * Calculates (averaged) per-cell residuals and calls
	 * {@link CurveCompetitivenessModel#addUpMarginalUtilities(UnmodifiableNumberMap, UnmodifiableNumberMap, boolean)} .
	 * 
	 * @param demand
	 * @param supply
	 * @param showWorking
	 * @return summed marginal utilities
	 */
	public double getCompetitveness(DemandModel demand, UnmodifiableNumberMap<Service> supply,
			boolean showWorking) {
		DoubleMap<Service> residual = demand.getAveragedPerCellResidualDemand().copy();

		if (showWorking) {
			log.info("Using residual: " + residual.prettyPrint());
		}

		return addUpMarginalUtilities(residual, supply, showWorking);
	}

	@Override
	public double getCompetitiveness(DemandModel demand, UnmodifiableNumberMap<Service> supply,
			Cell cell) {
		DoubleMap<Service> residual = demand.getResidualDemand(cell).copy();
		if (removeCurrentLevel) {
			cell.getSupply().addInto(residual);
		}
		return addUpMarginalUtilities(residual, supply);
	}

	@Override
	public double addUpMarginalUtilities(UnmodifiableNumberMap<Service> residualDemand,
			UnmodifiableNumberMap<Service> supply) {
		return addUpMarginalUtilities(residualDemand, supply, false);
	}

	/**
	 * Adds up marginal utilities (determined by competitiveness for unmet
	 * demand) of all services.
	 * 
	 * @param residualDemand
	 * @param supply
	 * @param showWorking
	 *            if true, log details in DEBUG mode
	 * @return summed marginal utilities of all services
	 */
	public double addUpMarginalUtilities(UnmodifiableNumberMap<Service> residualDemand,
			UnmodifiableNumberMap<Service> supply, boolean showWorking) {
		double sum = 0;
		for (Service s : supply.getKeySet()) {
			Curve c = curves.get(s); /* Gets the curve parameters for this service */
			if (c == null) {
				String message = "Missing curve for: " + s.getName() + " got: " + curves.keySet();
				log.fatal(message);
				throw new IllegalStateException(message);
			}
			double res = residualDemand.getDouble(s);
			double marginal = c.sample(res); /*
											 * Get the corresponding 'value' (y-value) for this
											 * level of unmet demand
											 */
			double amount = supply.getDouble(s);
			if (removeNegative && marginal < 0) {
				marginal = 0;
			}
			double comp = marginal * amount;
			if (showWorking) {
				log.debug(String.format("Service: %10s, Residual: %5f, Marginal: %5f, Amount: %5f",
						s.getName(), res, marginal, amount));
				log.debug("Curve: " + c.toString());
			}
			sum += comp;
		}
		return sum;
	}

	public boolean isRemoveCurrentLevel() {
		return removeCurrentLevel;
	}

	public void setRemoveCurrentLevel(boolean removeCurrentLevel) {
		this.removeCurrentLevel = removeCurrentLevel;
	}

	public boolean isRemoveNegative() {
		return removeNegative;
	}

	public void setRemoveNegative(boolean removeNegative) {
		this.removeNegative = removeNegative;
	}

	public void loadLinearCSV(String csvFile) throws IOException {
		ABMPersister persister = info.getPersister();
		if (!persister.csvFileOK(getClass(), csvFile, region.getPersisterContextExtra(),
				serviceColumn, interceptColumn, slopeColumn)) {
			return;
		}
		CsvReader reader = info.getPersister().getCSVReader(csvFile,
				region.getPersisterContextExtra());
		while (reader.readRecord()) {
			Service service = data.services.forName(reader.get(serviceColumn));
			double intercept = Double.parseDouble(reader.get(interceptColumn));
			double slope = Double.parseDouble(reader.get(slopeColumn));
			curves.put(service, new LinearFunction(intercept, slope));
		}
	}

	@Persist
	public void onWrite() {
		serialCurves.clear();
		for (Service s : curves.keySet()) {
			serialCurves.put(s.getName(), curves.get(s));
		}
	}

	public Map<Service, Curve> getCurves() {
		return curves;
	}

	public void setCurve(Service s, Curve c) {
		curves.put(s, c);
	}

	/**
	 * @return set of services
	 */
	public Set<Service> getDefinedServices() {
		return this.curves.keySet();
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
		CurveCompetitivenessModel copy = new CurveCompetitivenessModel();
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

		return copy;
	}
}
