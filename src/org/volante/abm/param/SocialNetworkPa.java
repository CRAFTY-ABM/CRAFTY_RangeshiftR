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
 * School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * Created by Sascha Holzhauer on 03.02.2014
 */
package org.volante.abm.param;


import de.cesr.more.manipulate.agent.MPseudoEgoNetworkProcessor;
import de.cesr.parma.core.PmParameterDefinition;
import de.cesr.parma.core.PmParameterManager;


/**
 * CRAFTY
 * 
 * @author Sascha Holzhauer
 * @date 27.03.2014
 * 
 */
public enum SocialNetworkPa implements PmParameterDefinition {

	OUTPUT_NETWORK_AFTER_CREATION(Boolean.class, false),

	OUTPUT_NETWORK_AFTER_CREATION_TICKPATTERN(String.class, "%o-%s-%i-%r-%y"),

	/**
	 * Needs to implement MoreEgoNetworkProcessor.
	 */
	DYN_EDGE_MANAGER(Class.class, MPseudoEgoNetworkProcessor.class),

	/**
	 * Needs to implement MoreEgoNetworkProcessor.
	 */
	DYN_EDGE_WEIGHT_UPDATER(Class.class, MPseudoEgoNetworkProcessor.class);

	private Class<?>	type;
	private Object		defaultValue;

	SocialNetworkPa(Class<?> type) {
		this(type, null);
	}

	SocialNetworkPa(Class<?> type, Object defaultValue) {
		this.type = type;
		this.defaultValue = defaultValue;
	}

	SocialNetworkPa(Class<?> type, PmParameterDefinition defaultDefinition) {
		this.type = type;
		if (defaultDefinition != null) {
			this.defaultValue = defaultDefinition.getDefaultValue();
			PmParameterManager.setDefaultParameterDef(this, defaultDefinition);
		} else {
			this.defaultValue = null;
		}
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}
}
