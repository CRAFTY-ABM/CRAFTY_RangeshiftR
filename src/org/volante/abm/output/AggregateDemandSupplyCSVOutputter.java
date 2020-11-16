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
 */
package org.volante.abm.output;


import org.simpleframework.xml.Attribute;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.data.Service;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class AggregateDemandSupplyCSVOutputter extends AggregateCSVOutputter {

	@Attribute(required = false)
	boolean	addSupply	= true;

	@Attribute(required = false)
	boolean	addDemand	= true;

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "AggregateServiceDemand";
	}

	@Override
	public void setOutputManager(Outputs outputs) {
		super.setOutputManager(outputs);

		if (addSupply) {
			for (Service s : outputs.modelData.services) {
				addColumn(new ServiceSupplyColumn(s));
			}
		}

		if (addDemand) {
			for (Service s : outputs.modelData.services) {
				addColumn(new ServiceDemandColumn(s));
			}
		}
	}

	public class ServiceDemandColumn implements TableColumn<Region> {
		Service	service;

		public ServiceDemandColumn(Service s) {
			this.service = s;
		}

		@Override
		public String getHeader() {
			return "Demand:" + service.getName();
		}

		@Override
		public String getValue(Region r, ModelData data, RunInfo info, Regions rs) {
			return doubleFmt.format(r.getDemandModel().getDemand().getDouble(service));
		}
	}

	public class ServiceSupplyColumn implements TableColumn<Region> {
		Service	service;

		public ServiceSupplyColumn(Service s) {
			this.service = s;
		}

		@Override
		public String getHeader() {
			return "ServiceSupply:" + service.getName();
		}

		@Override
		public String getValue(Region r, ModelData data, RunInfo info, Regions rs) {
			return doubleFmt.format(r.getDemandModel().getSupply().getDouble(service));
		}
	}
}
