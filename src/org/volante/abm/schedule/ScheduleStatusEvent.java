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
package org.volante.abm.schedule;


public class ScheduleStatusEvent {
	boolean			running	= false;
	int				tick	= 0;
	ScheduleStage	stage;

	public ScheduleStatusEvent(int tick, ScheduleStage stage, boolean running) {
		super();
		this.tick = tick;
		this.stage = stage;
		this.running = running;
	}

	public static enum ScheduleStage {
		PRE_TICK,
		MAIN_LOOP,
		POST_TICK,
		PAUSED,
		FINISHING;
	}

	public boolean isRunning() {
		return running;
	}

	public int getTick() {
		return tick;
	}

	public ScheduleStage getStage() {
		return stage;
	}
}
