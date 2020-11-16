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


import org.volante.abm.data.Service;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Basically, only world residuals are required to implement a global/super-regional market.
 * However, in order to calculate per-cell residuals entire-market-level demands, supplies and
 * number of cells are required. Therefore, these values are passed to the demand model which then
 * has more flexibility to calculate per-cell residuals (e.g. adding noise to some input data).
 * 
 * @author Sascha Holzhauer
 * 
 */
public interface WorldDemandModel extends DemandModel {

	public void setWorldNumberCells(int numCells);

	public DoubleMap<Service> getRegionalDemand();

	public void setWorldDemand(DoubleMap<Service> worldDemand);

	public DoubleMap<Service> getRegionalSupply();

	public void setWorldSupply(DoubleMap<Service> worldSupply);

}
