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
 * Created by Sascha Holzhauer on 28 Jul 2015
 */
package org.volante.abm.institutions.global;


import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.institutions.Institution;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;


/**
 * @author Sascha Holzhauer
 *
 */
public interface GlobalInstitution extends Institution, GloballyInitialisable {

	/**
	 * Initialises the global institution, adds it to the {@link GlobalInstitutionsRegistry} and registers it at
	 * regions.
	 * 
	 * @param rinfo
	 * @param mdata
	 */
	public void initialise(ModelData mdata, RunInfo rinfo);

	public Regions getRegionSet();

}
