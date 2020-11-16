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
package org.volante.abm.institutions;


import java.util.HashSet;
import java.util.Set;

import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

/**
 * AbstractInstitution - provides null implementations of all methods to provide a base for creating
 * new institutions when only some methods are necessary
 * @author dmrust
 *
 */
public abstract class AbstractInstitution implements Institution
{
	protected ModelData	modelData	= null;
	protected RunInfo	rInfo	= null;
	protected Region	region	= null;

	@Override
	public void adjustCapitals( Cell c ){
	}

	/**
	 * @see org.volante.abm.institutions.Institution#adjustCompetitiveness(org.volante.abm.agent.fr.FunctionalRole,
	 *      org.volante.abm.data.Cell, com.moseph.modelutils.fastdata.UnmodifiableNumberMap, double)
	 */
	@Override
	public double adjustCompetitiveness(FunctionalRole agent, Cell location,
			UnmodifiableNumberMap<Service> provision, double competitiveness)
	{ return competitiveness; }

	@Override
	public boolean isAllowed(FunctionalComponent agent, Cell location) {
		return isAllowed(agent.getFR(), location);
	}

	@Override
	public boolean isAllowed(FunctionalRole fr, Cell location) {
		return true;
	}

	public Set<FunctionalRole> getFrsExludedFromGivingIn() {
		return new HashSet<>();
	}

	@Override
	public void update() {}

	@Override
	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		this.modelData = data;
		this.rInfo = info;
		this.region = extent;
	}
}
