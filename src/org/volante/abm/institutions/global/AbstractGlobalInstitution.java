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
 * Created by Sascha Holzhauer on 29 Jul 2015
 */
package org.volante.abm.institutions.global;


import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.institutions.AbstractInstitution;
import org.volante.abm.schedule.RunInfo;


/**
 * A {@link GlobalInstitution} requires a different initialisation method.
 * 
 * @author Sascha Holzhauer
 * 
 */
public abstract class AbstractGlobalInstitution extends AbstractInstitution implements GlobalInstitution {


	/**
	 * In case of overriding, make sure to call {@link AbstractGlobalInstitution#initialise(ModelData, RunInfo)} or
	 * register at {@link GlobalInstitutionsRegistry} and regions.
	 * 
	 * @see org.volante.abm.institutions.global.GlobalInstitution#initialise(ModelData, RunInfo)
	 */
	@Override
	public void initialise(ModelData mdata, RunInfo rinfo) {
		// register
		this.modelData = mdata;
		this.rInfo = rinfo;
		GlobalInstitutionsRegistry.getInstance().registerGlobalInstitution(this);
		for (Region region : this.modelData.getRootRegionSet().getAllRegions()) {
			region.getInstitutions().addInstitution(this);
		}
	}
}
