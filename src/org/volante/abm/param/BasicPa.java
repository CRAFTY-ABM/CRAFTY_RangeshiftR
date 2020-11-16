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


import java.text.DecimalFormat;
import java.text.NumberFormat;

import de.cesr.parma.core.PmParameterDefinition;
import de.cesr.parma.core.PmParameterManager;

/**
 * CRAFTY
 * 
 * @author Sascha Holzhauer
 * @date 29.06.2010
 * 
 */
public enum BasicPa implements PmParameterDefinition {

	/**
	 * Time step for output of shape files (0 to omit; negative values are interpreted as interval starting at 0).
	 * ScheduleParameters.END = Infinity is possible
	 */
	WRITE_SHAPEFILES(Integer.class, 1),

	/**
	 * Folder to write shapefiles to (without tailing '/'). A folder named with the RunID is added to the specified
	 * folder automatically.
	 */
	SHAPEFILE_OUTPUT_FOLDER(String.class, "output/shapes"),

	/**
	 * If false, edges are not added to geography and thus are not displayed
	 */
	ADD_EDGES_TO_GEOGRAPHY(Boolean.class, true),

	/**
	 * Used for logging integer numbers
	 */
	INTEGER_FORMAT(NumberFormat.class, new DecimalFormat("0000")),

	/**
	 * Used for logging floating point numbers
	 */
	FLOAT_POINT_FORMAT(NumberFormat.class, new DecimalFormat("0.000"));

	private Class<?>	type;
	private Object		defaultValue;

	BasicPa(Class<?> type) {
		this(type, null);
	}

	BasicPa(Class<?> type, Object defaultValue) {
		this.type = type;
		this.defaultValue = defaultValue;
	}

	BasicPa(Class<?> type, PmParameterDefinition defaultDefinition) {
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
