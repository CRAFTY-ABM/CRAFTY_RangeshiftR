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
 * Created by Sascha Holzhauer on 12.03.2014
 */
package org.volante.abm.output;


import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.Cell;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.status.InnovationStates;


/**
 * Outputs whether an agent belonging to a cell adopted a specific innovation.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class AgentAdoptionStateRasterOutputter extends RasterOutputter {

	@Attribute(name = "innovationID", required = true)
	public String			innovationID	= null;

	protected Innovation	innovation;
	boolean					initialised		= false;

	/**
	 * Return the adoption state or 0 if the agent is not an
	 * {@link InnovativeBC}.
	 * 
	 * @see org.volante.abm.serialization.CellToDouble#apply(org.volante.abm.data.Cell)
	 */
	@Override
	public double apply(Cell c) {
		if (!initialised) {
			this.innovation = c.getRegion().getInnovationRegistry().getInnovation(innovationID);
		}
		if (c.getOwner() instanceof InnovativeBC) {
			return ((InnovativeBC) c.getOwner()).getState(this.innovation)
					.getID();
		} else {
			return InnovationStates.UNAWARE.getID();
		}
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "AdoptionState-" + innovationID;
	}

	/**
	 * @see org.volante.abm.output.RasterOutputter#isInt()
	 */
	@Override
	public boolean isInt() {
		return true;
	}
}