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
 * Created by Sascha Holzhauer on 20 Jan 2015
 */
package org.volante.abm.schedule;

import de.cesr.more.util.MSchedule;

/**
 * Extends {@link MSchedule} and triggers its step method by an
 * {@link PostTickAction} registered at the given {@link Schedule}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CraftyMoreSchedule extends MSchedule {

	Schedule schedule;

	public CraftyMoreSchedule(Schedule schedule) {
		this.schedule = schedule;
		this.schedule.register(new PostTickAction() {

			@Override
			public void postTick() {
				CraftyMoreSchedule.this.step(CraftyMoreSchedule.this.schedule
						.getCurrentTick());
			}
		});
	}
}
