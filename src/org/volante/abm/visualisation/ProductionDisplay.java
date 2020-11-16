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
package org.volante.abm.visualisation;

import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;

public class ProductionDisplay extends DatatypeDisplay<Service> implements Display, ActionListener
{
	private static final long	serialVersionUID	= 7582664446706175610L;
	Service service = null;

	@Override
	public double getVal( Cell c )
	{
		if( service == null ) {
			return Double.NaN;
		}
		return c.getSupply().getDouble( service );
	}

	@Override
	public Collection<String> getNames()
	{
		Set<String> names = new HashSet<String>();
		for( Service s : data.services ) {
			names.add( s.getName() );
		}
		return names;
	}
	
	@Override
	public void setupType( String type )
	{
		service = data.services.forName( type );
	}
	
	
	
//	@Override
//	public void update() {
		
		
//		for (Display display : displays.values()) {
//			display.update();
//		}

//		if (competitionDisplay != null) {
//			competitionDisplay.update();
//		}
//		if (demandDisplay != null) {
//			demandDisplay.update();
//		}
//	}
	
	
}
