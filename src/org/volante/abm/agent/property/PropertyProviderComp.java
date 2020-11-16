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
 * Created by Sascha Holzhauer on 13 May 2016
 */
package org.volante.abm.agent.property;


import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * @author Sascha Holzhauer
 *
 */
public class PropertyProviderComp<P> implements PropertyProvider<P> {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(PropertyProviderComp.class);

	Map<PropertyId, P> properties = new HashMap<>();

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#isProvided(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public boolean isProvided(PropertyId property) {
		return properties.containsKey(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#getProperty(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public P getObjectProperty(PropertyId property) {
		if (!properties.containsKey(property)) {
			// <- LOGGING
			logger.warn("This PropertyProvider does not contain an entry for key '" + property + "'! Returning Null.",
					new IllegalStateException());
			// LOGGING ->
			return null;
		} else {
			return properties.get(property);
		}
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#setProperty(org.volante.abm.agent.property.PropertyId,
	 *      double)
	 */
	@Override
	public void setObjectProperty(PropertyId propertyId, P value) {
		this.properties.put(propertyId, value);
	}
}
