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
 * 
 * Created by Sascha Holzhauer on 21.02.2014
 */
package org.volante.abm.testutils;

import org.volante.abm.agent.fr.AbstractFC;
import org.volante.abm.agent.fr.AbstractFR;
import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.example.SimpleProductionModel;

import de.cesr.more.basic.MManager;

/**
 * @author Sascha Holzhauer
 *
 */
public class CraftyTestUtils {

	public static void initMoreTestEnvironment() {
		MManager.init();
	}

	public static class PseudoFR extends AbstractFR {

		public PseudoFR(String label, int serialID) {
			super(label, new SimpleProductionModel());
			this.label = label;
			this.serialID = serialID;
		}

		/**
		 * @see org.volante.abm.agent.fr.FunctionalRole#getNewFunctionalComp(org.volante.abm.agent.Agent)
		 */
		@Override
		public FunctionalComponent getNewFunctionalComp() {
			return new AbstractFC(this, this.production) {
			};
		}

		/**
		 * @see org.volante.abm.agent.fr.FunctionalRole#getSampledGivingUpThreshold()
		 */
		@Override
		public double getSampledGivingUpThreshold() {
			return this.givingUpMean;
		}

		/**
		 * @see org.volante.abm.agent.fr.FunctionalRole#getSampledGivingInThreshold()
		 */
		@Override
		public double getSampledGivingInThreshold() {
			return this.givingInMean;
		}
	}
}
