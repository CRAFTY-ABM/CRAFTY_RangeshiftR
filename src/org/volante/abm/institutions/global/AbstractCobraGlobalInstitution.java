/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2016 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 31 Mar 2016
 */
package org.volante.abm.institutions.global;


import java.util.Map.Entry;

import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.property.PropertyRegistry;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.example.GlobalBtRepository;
import org.volante.abm.institutions.AbstractCobraInstitution;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public abstract class AbstractCobraGlobalInstitution extends AbstractCobraInstitution implements
		GlobalInstitution {

	/**
	 * @param id
	 */
	public AbstractCobraGlobalInstitution(@Attribute(name = "id") String id) {
		super(id);
	}

	/**
	 * @see org.volante.abm.institutions.global.GlobalInstitution#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo)
	 */
	@Override
	public void initialise(ModelData mdata, RunInfo rinfo) {
		this.modelData = mdata;
		this.rInfo = rinfo;

		// register
		GlobalInstitutionsRegistry.getInstance().registerGlobalInstitution(this);

		for (Region region : this.modelData.getRootRegionSet().getAllRegions()) {
			region.getInstitutions().addInstitution(this);
		}

		for (Entry<String, Object> property : params.entrySet()) {
			if (PropertyRegistry.get(property.getKey()) != null) {
				if (property.getValue() instanceof Number) {
					this.propertyProvider.setProperty(PropertyRegistry.get(property.getKey()),
							(Double) property.getValue());
				}
			}
		}

		// assign BC
		// this.region.getBehaviouralTypeMapByLabel().get(btLabel).assignNewBehaviouralComp(this);
		// TODO

	}

	/**
	 * @see org.volante.abm.institutions.AbstractCobraInstitution#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, GlobalBtRepository.getInstance().getPseudoRegion());
	}


		/**
	 * @see org.volante.abm.institutions.global.GlobalInstitution#getRegionSet()
	 */
	@Override
	public Regions getRegionSet() {
		return this.modelData.getRootRegionSet();
	}
}
