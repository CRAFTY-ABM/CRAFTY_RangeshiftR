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
package org.volante.abm.output;


import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;
import org.volante.abm.example.SimpleProductionModel;


/**
 * Currently only supports {@link SimpleProductionModel}.
 * 
 * @author Sascha Holzhauer
 *
 */
public class AgentProductivityRasterOutputter extends RasterOutputter {

	/**
	 * Logger
	 */
	static private Logger	logger		= Logger.getLogger(AgentProductivityRasterOutputter.class);

	@Attribute(name = "service", required = true)
	String	serviceName	= "HUMAN";
	Service	service		= null;

	public AgentProductivityRasterOutputter() {
	}

	public AgentProductivityRasterOutputter(String serviceName) {
		this.serviceName = serviceName;
	}

	public AgentProductivityRasterOutputter(Service service) {
		this.service = service;
	}

	@Override
	public double apply(Cell c) {
		if (c.getOwner().getProductionModel() instanceof SimpleProductionModel) {
			return ((SimpleProductionModel) c.getOwner().getProductionModel())
					.getProductionWeights().getDouble(service);
		} else {
			return Double.NaN;
		}
	}

	@Override
	public String getDefaultOutputName() {
		return "Productivity-" + service.getName();
	}

	@Override
	public void initialise() throws Exception {
		super.initialise();
		service = modelData.services.forName(serviceName);
		if(service == null) {
			logger.error("There is not service for name " + serviceName + " (available services: "
					+ modelData.services + ")!");
			throw new IllegalArgumentException("There is not service for name " + serviceName
					+ " (available services: " + modelData.services + ")!");
		}
	}

	@Override
	public boolean isInt() {
		return false;
	}
}
