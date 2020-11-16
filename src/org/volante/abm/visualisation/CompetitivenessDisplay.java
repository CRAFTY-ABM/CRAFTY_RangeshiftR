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

import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;


public class CompetitivenessDisplay extends DatatypeDisplay<FunctionalComponent> implements Display,
		ActionListener {
	private static final long	serialVersionUID	= 7016557610830690077L;

	FunctionalRole fr = null;

	@Override
	public double getVal(Cell c) {
		if (fr == null) {
			return Double.NaN;
		}
		return c.getRegion().getCompetitiveness(fr, c);
	}

	@Override
	public Collection<String> getNames() {
		Set<String> names = new HashSet<String>();
		for (FunctionalRole fr : region.getFunctionalRoleMapByLabel().values()) {
			names.add(fr.getLabel());
		}
		return names;
	}

	@Override
	public void setupType(String type) {
		fr = null;
		for (FunctionalRole fr : region.getFunctionalRoleMapByLabel().values()) {
			if (fr.getLabel().equals(type)) {
				this.fr = fr;
			}
		}
	}

}
