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


import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;


public class SupplyRasterOutput extends RasterOutputter {
	@Attribute(name = "service", required = true)
	String	serviceName	= "HUMAN";
	Service	service		= null;

	public SupplyRasterOutput() {
	}

	public SupplyRasterOutput(String serviceName) {
		this.serviceName = serviceName;
	}

	public SupplyRasterOutput(Service service) {
		this.service = service;
	}

	@Override
	public double apply(Cell c) {
		return c.getSupply().getDouble(service);
	}

	@Override
	public String getDefaultOutputName() {
		return "Supply-" + service.getName();
	}

	@Override
	public void initialise() throws Exception {
		super.initialise();
		service = modelData.services.forName(serviceName);
	}

	@Override
	public boolean isInt() {
		return false;
	}
}
