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
 * Created by Sascha Holzhauer on 15 Jul 2015
 */
package org.volante.abm.agent.property;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.volante.abm.example.AgentPropertyIds;


/**
 * @author Sascha Holzhauer
 *
 */
public class PropertyRegistry {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(PropertyRegistry.class);


	protected static Set<Class<? extends PropertyId>> enumClasses = new HashSet<>();

	static {
		enumClasses.add(AgentPropertyIds.class);
	}
	
	/**
	 * @param id
	 * @return AgentPropertyId
	 */
	public static PropertyId get(String id) {
		for (Class<? extends PropertyId> agentPropertyIdEnum : enumClasses) {
	        for (PropertyId prop : agentPropertyIdEnum.getEnumConstants()) {
				if (id.equals(prop.toString())) {
	                return prop;
	            }
	        }
	    }
	    logger.error("No AgentProperty called " + id + "! Returning null.");
	    return null;
	}
	
	/**
	 * @return collection of all defined {@link PropertyId}s
	 */
	public static Collection<PropertyId> getPropertyIds() {
		Collection<PropertyId> propertyIds = new ArrayList<>();
		for (Class<? extends PropertyId> agentPropertyIdEnum : enumClasses) {
			for (PropertyId prop : agentPropertyIdEnum.getEnumConstants()) {
				propertyIds.add(prop);
			}
		}
		return propertyIds;
	}

	public static void registerPropertiesEnum(Class<? extends PropertyId> enumeration) {
		enumClasses.add(enumeration);
	}
}
