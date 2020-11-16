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
package org.volante.abm.models;


import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;
import org.volante.abm.serialization.Initialisable;
import org.volante.abm.visualisation.Display;
import org.volante.abm.visualisation.Displayable;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Models the allocation and satisfaction of demand
 * 
 * @author dmrust
 * 
 */
public interface DemandModel extends Initialisable, Displayable {
	/**
	 * Should be called to get the level of demand in a particular cell This can include any regional demand
	 * 
	 * @param c
	 * @return demand map from given cell
	 */
	public DoubleMap<Service> getDemand(Cell c);

	/**
	 * Returns the level of demand for the Region
	 * 
	 * 
	 * @return demand map for entire region
	 */
	public DoubleMap<Service> getDemand();

	/**
	 * @return demand map per cell averaged over region or world.
	 */
	public DoubleMap<Service> getAveragedPerCellDemand();

	/**
	 * The spatialised demand for a single cell
	 * 
	 * @param c
	 * @return residual demand map for given cell
	 */
	public DoubleMap<Service> getResidualDemand(Cell c);

	/**
	 * @return residual demand map for given cell averaged over region or world.
	 */
	public DoubleMap<Service> getAveragedPerCellResidualDemand();

	/**
	 * Returns the level of residual demand for the region
	 * 
	 * @return residual demand map for entire region
	 */
	public DoubleMap<Service> getResidualDemand();

	/**
	 * Gets the marginal utility of producing a unit of each service at the
	 * current supply levels Uses the competitiveness model, but ignores
	 * cell/agent adjustments
	 * 
	 * @return marginal utilities map
	 */
	public DoubleMap<Service> getMarginalUtilities();

	/**
	 * Called when an agent changes on a cell, to allow updating on agent changes i.e. as demand
	 * gets satisfied through agent change
	 * 
	 * @param c
	 */
	public void agentChange(Cell c);

	/**
	 * Called after all agent changes have been done, and production has been updated.
	 * 
	 * A good place to recalculate residual demand etc.
	 */
	public void updateSupply();

	public DoubleMap<Service> getSupply();

	@Override
	public DemandDisplay getDisplay();

	public static interface DemandDisplay extends Display {
	};
}
