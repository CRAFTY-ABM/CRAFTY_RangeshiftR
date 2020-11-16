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
package org.volante.abm.schedule;


import org.volante.abm.data.RegionSet;
import org.volante.abm.serialization.Initialisable;


public interface Schedule extends Initialisable {
	/**
	 * Runs the simulation a single tick.
	 */
	public void tick();

	/**
	 * Sets the simulation's target tick to the current tick (which is the next tick that is
	 * performed).
	 */
	public void setTargetToNextTick();

	/**
	 * Returns the current tick (which is the next tick that is performed)
	 * 
	 * @return current tick
	 */
	public int getCurrentTick();

	/**
	 * Registers the object in case any actions are going to be called on it e.g. before/after
	 * ticks.
	 * 
	 * @param o
	 */
	public void register(TickAction o);

	/**
	 * Removes the given {@link TickAction} from this schedule to prevent the
	 * action from being performed in future.
	 * 
	 * @param o
	 *            tick action
	 * @return true if the given tick action could be unregistered, false
	 *         otherwise.
	 */
	public boolean unregister(TickAction o);

	public void setRegions(RegionSet set);

	/**
	 * Sets the simulation's start tick.
	 * 
	 * @param start
	 */
	public void setStartTick(int start);

	/**
	 * Sets and (intermediate) target tick.
	 * 
	 * @param target
	 */
	public void setTargetTick(int target);

	/**
	 * Sets the simualtion's final tick.
	 * 
	 * @param end
	 */
	public void setEndTick(int end);

	public int getTargetTick();

	public int getStartTick();

	public int getEndTick();

	/**
	 * Runs the simulation from predefined start to predefined end tick.
	 */
	public void run();

	/**
	 * Runs the simulation until the (intermediate) target tick.
	 * 
	 * @param end
	 */
	public void runUntil(int end);

	/**
	 * Runs the simulation from given start to end tick. Overwrites previously set values for start
	 * and end ticks.
	 * 
	 * @param start
	 * @param end
	 */
	public void runFromTo(int start, int end);

	public void finish();

	/**
	 * Registers the listener object in case any actions are going to be called on it e.g. before/after
	 * ticks.
	 * 
	 * @param o
	 */
	public void registerListeners(ScheduleStatusListener o);
}
