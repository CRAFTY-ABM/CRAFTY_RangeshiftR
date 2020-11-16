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
 */
package org.volante.abm.institutions;


import java.util.Set;

import org.simpleframework.xml.Root;
import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

@Root
public interface Institution extends Initialisable {
	/**
	 * Allows the institution to adjust the effective capitals present in the
	 * cell
	 * 
	 * @param c
	 */
	public void adjustCapitals(Cell c);

	/**
	 * When given an agent, a cell and the level of (potential) provision, adjusts the competitiveness level.
	 * 
	 * Must be able to deal with the agent being null if the cell is unoccupied.
	 * 
	 * If this method is implemented the implementing class should call
	 * {@link Region#setHasCompetitivenessAdjustingInstitution()} on the institution's initialisation!
	 * 
	 * @param agent
	 *        the agent the competitiveness is calculated for
	 * @param location
	 *        the cell
	 * @param provision
	 *        (potential) service provision of the given agent on the given cell
	 * @param competitiveness
	 *        unadjusted competitiveness
	 * @return adjusted competitiveness
	 */
	public double adjustCompetitiveness(FunctionalRole agent, Cell location,
			UnmodifiableNumberMap<Service> provision, double competitiveness);

	/**
	 * Determines whether this agent is forbidden from occupying that cell
	 * according to this institution
	 * 
	 * @param agent
	 * @param location
	 * @return true if the agent is allowed to occupy the given cell
	 */
	public boolean isAllowed(FunctionalComponent agent, Cell location);

	/**
	 * Determines whether this agent is forbidden from occupying that cell according to this institution
	 * 
	 * @param fr
	 * @param location
	 * @return true if the agent is allowed to occupy the given cell
	 */
	public boolean isAllowed(FunctionalRole fr, Cell location);

	public Set<FunctionalRole> getFrsExludedFromGivingIn();

	/**
	 * Called at the start of each tick to allow this institution to perform any
	 * internal updates necessary.
	 */
	public void update();
}
