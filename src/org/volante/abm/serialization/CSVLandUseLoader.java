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
package org.volante.abm.serialization;

import org.volante.abm.data.LandUse;

public class CSVLandUseLoader extends NamedIndexLoader<LandUse>
{
	
	@Override
	LandUse getType( String name, int index ) { return new CSVLandUse( name, index ); }
	public static class CSVLandUse implements LandUse
	{
		String	name	= "";
		int		index	= 0;
		public CSVLandUse( String name, int index )
		{
			this.name = name;
			this.index = index;
		}
		@Override
		public String getName() { return name; }
		@Override
		public int getIndex() { return index; }
		
		@Override
		public String toString() { return name; }
	}
}
