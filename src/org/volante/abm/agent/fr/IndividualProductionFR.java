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
 * Created by Sascha Holzhauer on 20 Mar 2015
 */
package org.volante.abm.agent.fr;

import org.volante.abm.example.SimpleProductionModel;
import org.volante.abm.models.ProductionModel;


/**
 * The production model is copied exactly (no individual noise) for each new instance.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class IndividualProductionFR extends AbstractFR {

	/**
	 * @param id
	 * @param production
	 */
	public IndividualProductionFR(String id, ProductionModel production) {
		super(id, production);
	}

	public IndividualProductionFR(String id, ProductionModel production, double givingUp, double givingIn) {
		this(id, UNKNOWN_SERIAL, production, givingUp, givingIn);
	}

	public IndividualProductionFR(String id, int serialId, ProductionModel production, double givingUp, double givingIn) {
		this(id, production);
		this.givingInMean = givingIn;
		this.givingUpMean = givingUp;
		this.serialID = serialId;
	}

	/**
	 * @see org.volante.abm.agent.fr.FunctionalRole#getNewFunctionalComp()
	 */
	@Override
	public FunctionalComponent getNewFunctionalComp() {
		ProductionModel pmodel = null;
		if (this.production instanceof SimpleProductionModel) {
			pmodel = ((SimpleProductionModel) production).copyExact();
		}
		return new IndividualProductionFC(this, pmodel);
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
