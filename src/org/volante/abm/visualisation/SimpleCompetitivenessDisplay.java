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


import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.example.SimpleCompetitivenessModel;
import org.volante.abm.models.CompetitivenessModel.CompetitivenessDisplay;
import org.volante.abm.schedule.RunInfo;

public class SimpleCompetitivenessDisplay extends AbstractDisplay implements CompetitivenessDisplay
{
	private static final long	serialVersionUID	= 5425101587105776270L;

	SimpleCompetitivenessModel	model				= null;

	public SimpleCompetitivenessDisplay(SimpleCompetitivenessModel model) {
		this.model = model;
	}

	@Override
	public void update() {
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
		super.initialise(data, info, region);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JLabel modelName = new JLabel("SimpleCompetitivenessModel");

		Box b = new Box(BoxLayout.X_AXIS);
		JLabel lab = new JLabel("removeCurrentLevel: ");
		lab.setPreferredSize(new Dimension(170, 15));
		b.add(lab);

		JLabel disp = new JLabel("" + model.isRemoveCurrentLevel());
		// disp.setPreferredSize(new Dimension(80, 15));
		// disp.setMinimumSize(new Dimension(80, 15));
		b.add(disp);
		b.setAlignmentX(1);

		Box b2 = new Box(BoxLayout.X_AXIS);
		JLabel lab2 = new JLabel("removeNegative: ");
		lab.setPreferredSize(new Dimension(170, 15));
		b2.add(lab2);

		JLabel disp2 = new JLabel("" + model.isRemoveNegative());
		// disp.setPreferredSize(new Dimension(80, 15));
		// disp.setMinimumSize(new Dimension(80, 15));
		b2.add(disp2);
		b2.setAlignmentX(1);

		add(modelName);
		add(b);
		add(b2);
		invalidate();
	}
}
