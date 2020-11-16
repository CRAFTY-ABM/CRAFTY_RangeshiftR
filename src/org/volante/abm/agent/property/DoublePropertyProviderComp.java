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
 * Created by Sascha Holzhauer on 19 Mar 2015
 */
package org.volante.abm.agent.property;

import org.apache.log4j.Logger;

/**
 * @author Sascha Holzhauer
 *
 */
public class DoublePropertyProviderComp extends PropertyProviderComp<Object> implements DoublePropertyProvider {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(DoublePropertyProviderComp.class);


	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#getProperty(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public Double getProperty(PropertyId property) {
		if (!properties.containsKey(property) || !(properties.get(property) instanceof Double)) {
			// <- LOGGING
			logger.warn("This DoublePropertyProvider does not contain an entry for key '"
 + property
					+ "'! Returning NaN.", new IllegalStateException());
			// LOGGING ->
			return Double.NaN;
		} else {
			return (Double) properties.get(property);
		}
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#setProperty(org.volante.abm.agent.property.PropertyId,
	 *      java.lang.Double)
	 */
	@Override
	public void setProperty(PropertyId propertyId, Double value) {
		this.setObjectProperty(propertyId, value);
	}
}
