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
 * Created by Sascha Holzhauer on 2 Nov 2015
 */
package org.volante.abm.agent;


import java.util.Set;

import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;
import org.volante.abm.models.ProductionModel;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * @author Sascha Holzhauer
 *
 */
public interface LandUseAgent extends Agent {

	/**
	 * Returns all the cells the agent manages
	 * 
	 * @return cells the agent manages
	 */
	public Set<Cell> getCells();

	/**
	 * Removes the cell from the set the agent manages
	 * 
	 * @param c
	 */
	public void removeCell(Cell c);

	/**
	 * Returns the production model of this agent (refers to the FR).
	 * 
	 * @return production model
	 */
	public ProductionModel getProductionModel();

	/**
	 * Updates the agent's competitiveness, in response to demand changes etc.
	 */
	public void updateCompetitiveness();

	/**
	 * Recalculates the services this agent can supply
	 */
	public void updateSupply();

	/**
	 * Asks this agent if it wants to give up
	 */
	public void considerGivingUp();

	/**
	 * Returns what this agent could supply on the given cell
	 * 
	 * @param c
	 * @return unmodifiable supply map
	 */
	public UnmodifiableNumberMap<Service> supply(Cell c);

	/**
	 * Adds the cell to the cells this agent manages
	 * 
	 * @param c
	 */
	public void addCell(Cell c);

	/**
	 * Returns true if this agent has lost all its cells and should be removed
	 * 
	 * @return true if this agent is to remove
	 */
	public boolean notAllocated();

	/**
	 * Return true if this agent is happy to cede to an agent with the given level of competitiveness
	 * 
	 * @param c
	 * @param competitiveness
	 *        competing agents competitiveness
	 * @return true if an agent with the given competitiveness can take over the given cell from this agents
	 */
	public boolean canTakeOver(Cell c, double competitiveness);
}
