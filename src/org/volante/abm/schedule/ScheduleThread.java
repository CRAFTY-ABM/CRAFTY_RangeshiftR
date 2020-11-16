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

public class ScheduleThread implements Runnable
{
	Schedule	schedule	= null;
	
	public ScheduleThread( Schedule sched )
	{
		this.schedule = sched;
	}

	public void start()
	{
		Thread t = new Thread( this );
		t.start();
	}
	@Override
	public void run()
	{
		//while( schedule.getEndTick() < 0 || schedule.getEndTick() >= schedule.getCurrentTick() )
		while( true )
		{
			if( shouldRun() ) {
				schedule.tick();
			}
			else {
				try { Thread.sleep( 1000 ); } 
					catch (InterruptedException e) { } //Don't care if we're interrupted
			}
		}
	}
	
	public boolean shouldRun()
	{
		if (schedule.getCurrentTick() <= schedule.getTargetTick())
		{
			return true;
		}
		return false;
	}
}
