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
 */
package org.volante.abm.institutions;


import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.institutions.global.GlobalInstitution;
import org.volante.abm.output.ActionReporter;
import org.volante.abm.schedule.DefaultSchedule;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * @author Sascha Holzhauer
 * 
 */
@Root
public class Institutions implements Institution, PreTickAction {

	Set<Institution> institutions = new HashSet<Institution>();

	Region region = null;
	ModelData data = null;
	RunInfo info = null;
	Logger log = Logger.getLogger(getClass());

	boolean initialised = false;

	public void addInstitution(Institution i) {
		institutions.add(i);
		try {
			if (this.initialised) {
				i.initialise(data, info, region);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * @param i
	 * @return true if the given institution was registered at this Institutions
	 */
	public boolean hasInstitution(Institution i) {
		return institutions.contains(i);
	}

	/**
	 * @see org.volante.abm.institutions.Institution#isAllowed(org.volante.abm.agent.fr.FunctionalComponent,
	 *      org.volante.abm.data.Cell)
	 */
	@Override
	public boolean isAllowed(FunctionalComponent a, Cell c) {
		for (Institution i : institutions) {
			if (!i.isAllowed(a, c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see org.volante.abm.institutions.Institution#isAllowed(org.volante.abm.agent.fr.FunctionalRole,
	 *      org.volante.abm.data.Cell)
	 */
	@Override
	public boolean isAllowed(FunctionalRole fr, Cell c) {
		for (Institution i : institutions) {
			if (!i.isAllowed(fr, c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @see org.volante.abm.institutions.Institution#getFrsExludedFromGivingIn()
	 */
	public Set<FunctionalRole> getFrsExludedFromGivingIn() {
		Set<FunctionalRole> excluded = new HashSet<>();
		for (Institution i : institutions) {
			excluded.addAll(i.getFrsExludedFromGivingIn());
		}
		return excluded;
	}

	/**
	 * @see org.volante.abm.institutions.Institution#adjustCapitals(org.volante.abm.data.Cell)
	 */
	@Override
	public void adjustCapitals(Cell c) {
		for (Institution i : institutions) {
			i.adjustCapitals(c);
		}
	}

	/**
	 * @see org.volante.abm.institutions.Institution#adjustCompetitiveness(org.volante.abm.agent.fr.FunctionalRole,
	 *      org.volante.abm.data.Cell, com.moseph.modelutils.fastdata.UnmodifiableNumberMap, double)
	 */
	@Override
	public double adjustCompetitiveness(FunctionalRole fComp, Cell location, UnmodifiableNumberMap<Service> provision,
			double competitiveness) {
		double result = competitiveness;
		for (Institution i : institutions) {
			result = i.adjustCompetitiveness(fComp, location, provision, result);
		}
		return result;
	}

	/**
	 * Delegates to {@link Agent#tickStartUpdate()} in case the institutions is an {@link Agent}.
	 */
	public void tickStartUpdate() {
		for (Institution i : institutions) {
			if (i instanceof Agent && !(i instanceof GlobalInstitution)) {
				((Agent) i).tickStartUpdate();
			}
		}
	}

	@Override
	public void update() {
		for (Institution i : institutions) {
			i.update();
		}
	}

	/**
	 * @see DefaultSchedule#tick()
	 */
	public void updateCapitals() {
		log.info("Adjusting capitals for Region " + region);
		for (Cell c : region.getAllCells()) {
			adjustCapitals(c);
		}
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.data = data;
		this.info = info;
		this.region = extent;
		for (Institution i : this.institutions) {
			i.initialise(data, info, region);
		}
		this.initialised = true;
	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void preTick() {
		update();
	}

	/**
	 * @return true if any institution registered
	 */
	public boolean hasInstitutions() {
		return !institutions.isEmpty();
	}
	
	/**
	 * @param reporter
	 */
	public void registerPaReporter(ActionReporter reporter) {
		for (Institution institution : this.institutions) {
			if (institution instanceof Agent) {
				reporter.registerAtAgent((Agent) institution);
			}
		}
	}

	/**
	 * @param id
	 * @return institution or null if id cannot be found
	 */
	public Institution getInstitution(String id) {
		for (Institution inst : institutions) {
			if (inst instanceof Agent && ((Agent) inst).getID().equals(id)) {
				return inst;
			}
		}
		return null;
	}
}
