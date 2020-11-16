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


import static java.awt.Color.ORANGE;
import static java.awt.Color.green;
import static java.awt.Color.red;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.volante.abm.schedule.FinishAction;
import org.volante.abm.schedule.Schedule;
import org.volante.abm.schedule.ScheduleStatusEvent;
import org.volante.abm.schedule.ScheduleStatusListener;


public class TimeDisplay extends JPanel implements ScheduleStatusListener {
	private static final long	serialVersionUID	= -8064466507015469913L;

	JLabel						tick				= new JLabel("0");
	JLabel						status				= new JLabel("Not started");
	JPanel						running				= new JPanel();
	int							height				= 20;

	public TimeDisplay() {
		running.setBackground(ORANGE);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		tick.setPreferredSize(new Dimension(100, height));
		status.setPreferredSize(new Dimension(100, height));
		running.setPreferredSize(new Dimension(height, height));
		add(new JLabel("Year:"));
		add(tick);
		add(new JLabel("Status:"));
		add(status);
		add(new JLabel("Running:"));
		add(running);
	}

	public TimeDisplay(Schedule s) {
		this();
		setSchedule(s);
	}

	public void setSchedule(final Schedule s) {
		tick.setText(s.getCurrentTick() + "");
		s.register(new FinishAction() {

			@Override
			public void afterLastTick() {
				tick.setText(s.getCurrentTick() + "");
				status.setText("Finished");
			}
		});
	}

	@Override
	public void scheduleStatus(ScheduleStatusEvent e) {
		tick.setText(e.getTick() + "");
		status.setText(e.getStage().name());
		running.setBackground(e.isRunning() ? green : red);
	}
}
