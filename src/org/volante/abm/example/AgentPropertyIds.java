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
 * Created by Sascha Holzhauer on 12 Jan 2015
 */
package org.volante.abm.example;

import org.volante.abm.agent.property.PropertyId;

/**
 * Common agent property IDs
 * 
 * @author Sascha Holzhauer
 *
 */
public enum AgentPropertyIds implements PropertyId {

	AGE,
	EDUCATION,
	EXPERIENCE,
	FARM_SIZE,
	COMPETITIVENESS,
	GIVING_IN_THRESHOLD,
	GIVING_UP_THRESHOLD,

	/**
	 * Probability of giving up in case the competitiveness falls below the giving-up thresholds.
	 */
	GIVING_UP_PROB,

	/**
	 * Power for the relative competition gap (difference between competition and giving up threshold, divided by the
	 * maximum difference per FR). Assign 0 to bypass this effect. The result is multiplied with the
	 * {@link AgentPropertyIds#GIVING_UP_PROB} to obtain final giving up probability.
	 */
	GIVING_UP_PROB_WEIGHT,

	/**
	 * Probability to allocate new land to this agent
	 */
	ALLOCATE_PROB,

	/**
	 * If set (not {@link Double#NaN}) and above 1,
	 * {@link AgentPropertyIds#GIVING_IN_THRESHOLD} may not be overwritten,
	 * below 1 only by a higher value.
	 */
	FORBID_GIVING_IN_THRESHOLD_OVERWRITE,

	/**
	 * If set (not {@link Double#NaN}) and above 1,
	 * {@link AgentPropertyIds#GIVING_UP_THRESHOLD} may not be overwritten,
	 * below 1 only by a lower value.
	 */
	FORBID_GIVING_UP_THRESHOLD_OVERWRITE;
}
