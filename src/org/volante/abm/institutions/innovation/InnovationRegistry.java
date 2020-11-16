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
 * Created by Sascha Holzhauer on 12.03.2014
 */
package org.volante.abm.institutions.innovation;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.volante.abm.data.Region;


/**
 * Applies the Multiton design pattern.
 * 
 * @author Sascha Holzhauer
 *
 */
public class InnovationRegistry {

	/**
	 * Logger
	 */
	static private Logger				logger		= Logger.getLogger(InnovationRegistry.class);

	protected Region					region;

	protected Map<String, Innovation>	innovations	= new HashMap<String, Innovation>();

	public InnovationRegistry(Region region) {
		this.region = region;
	}

	public void registerInnovation(Innovation innovation, String id) {
		if (innovations.containsKey(id)) {
			throw new IllegalStateException("Identifier " + id + " already in use!");
		} else {
			innovations.put(id, innovation);
		}
	}

	/**
	 * @param innovation
	 * @return
	 */
	public boolean hasInnovationRegistered(Innovation innovation) {
		return innovations.containsValue(innovation);
	}

	/**
	 * @param identifier
	 * @return
	 */
	public boolean hasInnovationRegistered(String identifier) {
		return innovations.containsKey(identifier);
	}

	public Innovation getInnovation(String id) {
		if (!innovations.containsKey(id)) {
			logger.warn("The InnovationRegistry (" + this
					+ ") does not contain innovation with ID " + id);
			return null;
		} else {
			return innovations.get(id);
		}
	}

	/**
	 * Removes all registered innovations from this registry.
	 */
	public void reset() {
		this.innovations = new HashMap<String, Innovation>();
	}
}
