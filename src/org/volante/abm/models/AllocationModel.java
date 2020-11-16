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


import org.volante.abm.data.Region;
import org.volante.abm.serialization.Initialisable;
import org.volante.abm.visualisation.Display;
import org.volante.abm.visualisation.Displayable;


/**
 * The allocation procedure deals with all land allocation in a particular region.
 * 
 * It has access to all the data - current production, demand, residuals, potential agents etc.
 * Common tasks are: * allocating empty cells * allowing potential agents to force out existing
 * agents.
 * 
 * @author dmrust
 * 
 */
public interface AllocationModel extends Initialisable, Displayable {
	public void allocateLand(Region r);

	@Override
	public AllocationDisplay getDisplay();

	public interface AllocationDisplay extends Display {
	}
}
