/**
 * This file is part of
 * 
 * MORe - Managing Ongoing Relationships
 *
 * Copyright (C) 2010 Center for Environmental Systems Research, Kassel, Germany
 * 
 * MORe - Managing Ongoing Relationships is free software: You can redistribute 
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *  
 * MORe - Managing Ongoing Relationships is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Center for Environmental Systems Research, Kassel
 * 
 * Created by holzhauer on 27.09.2011
 */
package org.volante.abm.param;


import de.cesr.parma.core.PmParameterDefinition;
import de.cesr.parma.core.PmParameterManager;

/**
 * Definition of random streams related parameters for MORe
 *
 * @author Sascha Holzhauer
 * @date 27.09.2011 
 *
 */
public enum RandomPa implements PmParameterDefinition {
	/**
	 * Random seed used for all random streams throughout the model that not have a specialised
	 * random stream defined. Default: <code>0</code>.
	 */
	RANDOM_SEED(Integer.class, 0),

	/**
	 * Random seed used for initialisation processes. Default: <code>RANDOM_SEED</code>.
	 */
	RANDOM_SEED_INIT(Integer.class, RANDOM_SEED),
	
	/**
	 * Random seed used for initialisation of agents. Default:
	 * <code>RANDOM_SEED_INITIALISATION</code>.
	 */
	RANDOM_SEED_INIT_AGENTS(Integer.class, RANDOM_SEED_INIT),

	/**
	 * Random seed used for network building processes. Default:
	 * <code>RANDOM_SEED_INITIALISATION</code>.
	 */
	RANDOM_SEED_INIT_NETWORK(Integer.class, RANDOM_SEED_INIT),

	/**
	 * Random seed used for processes during simulation run. Default: <code>RANDOM_SEED</code>.
	 */
	RANDOM_SEED_RUN(Integer.class, RANDOM_SEED),

	/**
	 * Random seed used for allocation during simulation run. Default: <code>RANDOM_SEED_RUN</code>.
	 */
	RANDOM_SEED_RUN_ALLOCATION(Integer.class, RANDOM_SEED_RUN),

	/**
	 * Random seed used for giving up during simulation run. Default: <code>RANDOM_SEED_RUN</code>.
	 */
	RANDOM_SEED_RUN_GIVINGUP(Integer.class, RANDOM_SEED_RUN),

	/**
	 * Random seed used for adoption decisions. Default: <code>RANDOM_SEED_RUN</code>.
	 */
	RANDOM_SEED_RUN_ADOPTION(Integer.class, RANDOM_SEED_RUN),

	/**
	 * Random seed used for institutional decision making and action, e.g. noise of monitoring. Default:
	 * <code>RANDOM_SEED_RUN</code>.
	 */
	RANDOM_SEED_RUN_INSTITUTIONS(Integer.class, RANDOM_SEED_RUN);
	;
	
	private Class<?> type;
	private Object defaultValue;
	
	/**
	 * @param type
	 */
	RandomPa(Class<?> type) {
		this(type, null);
	}

	/**
	 * @param type
	 * @param defaultValue
	 */
	RandomPa(Class<?> type, Object defaultValue) {
		this.type = type;
		this.defaultValue = defaultValue;
	}

	RandomPa(Class<?> type, PmParameterDefinition defaultDefinition) {
		this.type = type;
		if (defaultDefinition != null) {
			this.defaultValue = defaultDefinition.getDefaultValue();
			PmParameterManager.setDefaultParameterDef(this, defaultDefinition);
		} else {
			this.defaultValue = null;
		}
	}

	/**
	 * @see de.cesr.parma.core.PmParameterDefinition#getType()
	 */
	@Override
	public Class<?> getType() {
		return type;
	}
	
	/**
	 * @see de.cesr.parma.core.PmParameterDefinition#getDefaultValue()
	 */
	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}
}