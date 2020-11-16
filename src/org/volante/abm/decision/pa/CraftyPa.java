/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 11 Mar 2015
 */
package org.volante.abm.decision.pa;

import java.util.Map;

import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.schedule.RunInfo;

import de.cesr.lara.components.LaraBehaviouralOption;
import de.cesr.lara.components.LaraPreference;

/**
 * @author Sascha Holzhauer
 * @param <BO>
 * 
 *            <BO extends CraftyPo<BO>>
 */
public abstract class CraftyPa<BO extends CraftyPa<BO>> extends
        LaraBehaviouralOption<LaraBehaviouralComponent, CraftyPa<BO>> implements CraftyPaFeatures {

	protected int initialTick = Integer.MIN_VALUE;
	protected int lastReportedTick = Integer.MIN_VALUE;
	protected RunInfo rinfo;

	/**
	 * @param key
	 * @param agent
	 * @param preferenceUtilities
	 *        Initial preference utilities
	 */
	public CraftyPa(String key,
			LaraBehaviouralComponent agent,
			Map<LaraPreference, Double> preferenceUtilities) {
		super(key, agent, preferenceUtilities);
	}

	/**
	 * @param key
	 * @param agent
	 */
	public CraftyPa(String key, LaraBehaviouralComponent agent) {
		super(key, agent);
	}

	/**
	 * @see org.volante.abm.decision.pa.CraftyPaFeatures#reportRenewedActionPerformance()
	 */
	public void reportRenewedActionPerformance() {
		if (this.initialTick == Integer.MIN_VALUE) {
			this.rinfo = this.getAgent().getAgent().getRegion().getRinfo();
			this.initialTick = this.rinfo.getSchedule().getCurrentTick();
		}
		if (this.initialTick < this.rinfo.getSchedule().getCurrentTick()
		        && this.lastReportedTick < this.rinfo.getSchedule().getCurrentTick()) {
			this.getAgent().getLaraComp().reportActionPerformance(this);
			this.lastReportedTick = this.rinfo.getSchedule().getCurrentTick();
		}
	}
}
