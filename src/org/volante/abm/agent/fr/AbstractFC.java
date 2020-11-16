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
 * Created by Sascha Holzhauer on 19 Mar 2015
 */
package org.volante.abm.agent.fr;

import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;
import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.models.ProductionModel;

import com.moseph.modelutils.fastdata.DoubleMap;

/**
 * @author Sascha Holzhauer
 *
 */
public abstract class AbstractFC implements FunctionalComponent {

	protected ProductionModel	production	= new SimpleProductionModel();
	
	protected FunctionalRole functionalRole = null;


	public AbstractFC(FunctionalRole fRole,
			ProductionModel production) {
		this.functionalRole = fRole;
		this.production = production;
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalComponent#getExpectedSupply(org.volante.abm.data.Cell)
	 */
	@Override
	public DoubleMap<Service> getExpectedSupply(Cell cell) {
		DoubleMap<Service> map = cell.getRegion().getModelData()
				.serviceMap();
		production.production(cell, map);
		return map;
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalComponent#getFR()
	 */
	@Override
	public FunctionalRole getFR() {
		return this.functionalRole;
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalComponent#getProduction()
	 */
	@Override
	public ProductionModel getProduction() {
		return this.production;
	}

	public void setProductionFunction(ProductionModel f) {
		this.production = f;
	}
}
