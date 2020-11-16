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
 * Created by Sascha Holzhauer on 27 May 2015
 */
package org.volante.abm.agent.fr;

import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;

/**
 * @author Sascha Holzhauer
 *
 */
public class LazyFR extends AbstractFR implements FunctionalComponent {

	protected static LazyFR instance = null;

	public static LazyFR getInstance() {
		if (instance == null) {
			instance = new LazyFR("Lazy FR");
		}
		return instance;
	}

	/**
	 * @param id
	 * @param serialId
	 * @param production
	 * @param givingUp
	 * @param givingIn
	 */
	private LazyFR(String id) {
		super(id, new ProductionModel() {

			@Override
			public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
				// nothing to do
			}

			@Override
			public void production(Cell c, DoubleMap<Service> v) {
				// nothing to do
			}
		});
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#getNewFunctionalComp()
	 */
	@Override
	public FunctionalComponent getNewFunctionalComp() {
		return this;
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalComponent#getFR()
	 */
	@Override
	public FunctionalRole getFR() {
		return this;
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalComponent#setProductionFunction(org.volante.abm.models.ProductionModel)
	 */
	@Override
	public void setProductionFunction(ProductionModel f) {
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#getSampledGivingUpThreshold()
	 */
	@Override
	public double getSampledGivingUpThreshold() {
		return getMeanGivingUpThreshold();
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#getSampledGivingInThreshold()
	 */
	@Override
	public double getSampledGivingInThreshold() {
		return getMeanGivingInThreshold();
	}
}
