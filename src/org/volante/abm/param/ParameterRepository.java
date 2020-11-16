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
 * Created by Sascha Holzhauer on 07.05.2014
 */
package org.volante.abm.param;


import java.util.LinkedHashMap;
import java.util.Map;

import org.volante.abm.data.Region;


/**
 * @author Sascha Holzhauer
 *
 */
public class ParameterRepository extends LinkedHashMap<Region, LinkedHashMap<String, Object>> {

	private static final long	serialVersionUID	= -4842345951833453523L;

	public void addParameter(Region r, String name, Object param) {
		if (!this.containsKey(r)) {
			this.put(r, new LinkedHashMap<String, Object>());
		}
		this.get(r).put(name, param);
	}

	public Map<String, Object> getRegionParameters(Region r) {
		return this.get(r);
	}
}
