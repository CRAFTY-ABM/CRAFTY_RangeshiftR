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
 * Created by Sascha Holzhauer on 19 Apr 2016
 */
package org.volante.abm.agent.bt;


import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.volante.abm.data.Region;


/**
 * Used to allow error handling when non-existing keys (BT labels or BT IDs) are requested.
 * 
 * @author Sascha Holzhauer
 * @param <Key>
 * 
 */
public class BehaviouralTypeMap<Key> extends LinkedHashMap<Key, BehaviouralType> {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(BehaviouralTypeMap.class);

	protected Region region = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8151585554553965955L;

	public BehaviouralTypeMap(Region region) {
		this.region = region;
	}

	public BehaviouralType get(Object key) {
		if (!this.containsKey(key)) {
			String message =
					"The region (" + region + ") does not define the requested BT (" + key + ") - defined are "
							+ this.keySet();
			logger.error(message);
			throw new IllegalStateException(message);
		}
		return super.get(key);
	}
}
