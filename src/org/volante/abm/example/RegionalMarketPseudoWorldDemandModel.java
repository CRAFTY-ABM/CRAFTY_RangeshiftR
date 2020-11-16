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
 * Created by Sascha Holzhauer on 5 Oct 2015
 */
package org.volante.abm.example;

import org.volante.abm.data.Service;
import org.volante.abm.models.WorldDemandModel;

import com.moseph.modelutils.fastdata.DoubleMap;

/**
 * @author Sascha Holzhauer
 *
 */
public class RegionalMarketPseudoWorldDemandModel extends RegionalDemandModel implements WorldDemandModel {

	/**
	 * @see org.volante.abm.models.WorldDemandModel#setWorldNumberCells(int)
	 */
	@Override
	public void setWorldNumberCells(int numCells) {
		// nothing to do
	}

	/**
	 * @see org.volante.abm.models.WorldDemandModel#getRegionalDemand()
	 */
	@Override
	public DoubleMap<Service> getRegionalDemand() {
		return this.demand;
	}

	/**
	 * @see org.volante.abm.models.WorldDemandModel#setWorldDemand(com.moseph.modelutils.fastdata.DoubleMap)
	 */
	@Override
	public void setWorldDemand(DoubleMap<Service> worldDemand) {
		// noting to do
	}

	/**
	 * @see org.volante.abm.models.WorldDemandModel#getRegionalSupply()
	 */
	@Override
	public DoubleMap<Service> getRegionalSupply() {
		return this.totalSupply;
	}

	/**
	 * @see org.volante.abm.models.WorldDemandModel#setWorldSupply(com.moseph.modelutils.fastdata.DoubleMap)
	 */
	@Override
	public void setWorldSupply(DoubleMap<Service> worldSupply) {
		// nothing to do
	}
}
