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
package org.volante.abm.visualisation;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.volante.abm.schedule.Schedule;


public class ScheduleControls extends JPanel {
	private static final long	serialVersionUID	= 3122750609514377601L;

	JButton						step				= null;
	JButton						stepTillEnd			= null;
	JTextField					runUntil			= null;
	JButton						stop				= null;
	AbstractAction				stepAction			= null;
	AbstractAction				stepTillEndAction	= null;
	AbstractAction				stopNextTick		= null;
	Schedule					schedule			= null;

	public ScheduleControls() {
		stepAction = getAction("Step", new Runnable() {
			@Override
			public void run() {
				schedule.setTargetToNextTick();
			}
		});
		stepTillEndAction = getAction("Step Until", new Runnable() {
			@Override
			public void run() {
				schedule.setTargetTick(Integer.parseInt(runUntil.getText()));
			}
		});
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		stopNextTick = getAction("Finish", new Runnable() {
			@Override
			public void run() {
				schedule.setTargetTick(schedule.getEndTick());
				stepAction.setEnabled(false);
				stepTillEnd.setEnabled(false);
				stopNextTick.setEnabled(false);
			}
		});

		step = new JButton(stepAction);
		stepTillEnd = new JButton(stepTillEndAction);
		stop = new JButton(stopNextTick);
		runUntil = new JTextField("...", 5);

		add(step);
		add(stepTillEnd);
		add(runUntil);
		add(stop);
	}

	public ScheduleControls(Schedule s) {
		this();
		setSchedule(s);
		// runUntil.setText( schedule.getEndTick()+"" );
	}

	public void setSchedule(Schedule s) {
		this.schedule = s;
	}

	public AbstractAction getAction(String name, final Runnable run) {
		return new AbstractAction(name) {
			private static final long	serialVersionUID	= -2377276489333239452L;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(run);
			}
		};

	}
}
