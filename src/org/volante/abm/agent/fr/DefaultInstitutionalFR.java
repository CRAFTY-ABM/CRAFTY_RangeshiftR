/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2016 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 15 Dec 2016
 */
package org.volante.abm.agent.fr;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.models.ProductionModel;


/**
 * {@link DefaultFR} which implements {@link InstitutionalFR}
 * 
 * @author Sascha Holzhauer
 * 
 */
public class DefaultInstitutionalFR extends DefaultFR implements InstitutionalFR {

	public DefaultInstitutionalFR(@Attribute(name = "label") String label,
	        @Element(name = "production") ProductionModel production) {
		super(label, production);
	}

	public DefaultInstitutionalFR(String label, ProductionModel production, double givingUp, double givingIn) {
		super(label, production, givingUp, givingIn);
	}

	public DefaultInstitutionalFR(String label, int serialId, ProductionModel production, double givingUp,
	        double givingIn) {
		super(label, serialId, production, givingUp, givingIn);
	}
}
