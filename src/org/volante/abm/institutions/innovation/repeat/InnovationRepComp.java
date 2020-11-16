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
 * Created by Sascha Holzhauer on 4 Dec 2014
 */
package org.volante.abm.institutions.innovation.repeat;


import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.serialization.Initialisable;


/**
 * {@link InnovationRepComp}s encapsulates an {@link Innovation}s renewal
 * interval and the method to obtain renewed innovations.
 * 
 * @author Sascha Holzhauer
 *
 */
public interface InnovationRepComp extends Initialisable {

	/**
	 * Return the number of ticks the associated innovation it to be renewed.
	 * 
	 * @return renewal interval
	 */
	public int getRenewalInterval();

	/**
	 * Adjust the renewed innovation to reflect defined effects.
	 * 
	 * @param innovation
	 * @return innovation type
	 */
	public <InnovationType extends Innovation> InnovationType adjustRenewedInnovation(
			InnovationType innovation);

}
