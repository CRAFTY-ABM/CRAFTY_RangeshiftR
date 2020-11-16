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


import java.util.HashSet;
import java.util.Set;


/**
 * @author Sascha Holzhauer
 *
 */
public class GlobalInstitutionsRegistry {

	static GlobalInstitutionsRegistry instance = null;

	protected Set<GlobalInstitution> globalInstitutions = new HashSet<GlobalInstitution>();
	private GlobalInstitutionsRegistry() {

	}

	public static GlobalInstitutionsRegistry getInstance() {
		if (instance == null) {
			instance = new GlobalInstitutionsRegistry();
		}
		return instance;
	}

	/**
	 * @param institution
	 */
	public void registerGlobalInstitution(GlobalInstitution institution) {
		this.globalInstitutions.add(institution);
	}

	/**
	 * @return set of registered global institutions
	 */
	public Set<GlobalInstitution> getGlobalInstitutions() {
		return new HashSet<>(globalInstitutions);
	}

	public static void reset() {
		instance = null;
	}
}
