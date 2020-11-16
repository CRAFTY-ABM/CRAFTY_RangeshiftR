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
import javax.swing.JFrame;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.data.Service;
import org.volante.abm.example.RegionalDemandModel;
import org.volante.abm.models.DemandModel.DemandDisplay;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;


public class RegionalDemandDisplay extends AbstractDisplay implements DemandDisplay {
	private static final long	serialVersionUID		= -5116937463830581470L;

	RegionalDemandModel			model					= null;
	DoubleMapDisplay			supplyDisplay			= new DoubleMapTextDisplay("Supply");
	DoubleMapDisplay			demandDisplay			= new DoubleMapTextDisplay("Demand");
	DoubleMapDisplay			residualDisplay			= new DoubleMapTextDisplay("Residual");
	DoubleMapDisplay			marginalDisplay			= new DoubleMapTextDisplay(
																"Marginal Utilities");
	DoubleMapDisplay			supplyPerCellDisplay	= new DoubleMapTextDisplay("Supply");
	DoubleMapDisplay			demandPerCellDisplay	= new DoubleMapTextDisplay("Demand");
	DoubleMapDisplay			residualPerCellDisplay	= new DoubleMapTextDisplay("Residual");
	DoubleMapDisplay			marginalPerCellDisplay	= new DoubleMapTextDisplay("Marginal Util");

	public RegionalDemandDisplay(RegionalDemandModel model) {
		this.model = model;
	}

	@Override
	public void update() {
		DoubleMap<Service> supp = model.getSupply().copy();
		DoubleMap<Service> dem = model.getDemand().copy();
		DoubleMap<Service> res = model.getResidualDemand().copy();
		supplyDisplay.setMap(supp.toMap());
		demandDisplay.setMap(dem.toMap());
		residualDisplay.setMap(res.toMap());
		marginalDisplay.setMap(model.getMarginalUtilities().toMap());
		supp.multiplyInto(1.0 / region.getNumCells(), supp);
		dem.multiplyInto(1.0 / region.getNumCells(), dem);
		res.multiplyInto(1.0 / region.getNumCells(), res);
		supplyPerCellDisplay.setMap(supp.toMap());
		demandPerCellDisplay.setMap(dem.toMap());
		residualPerCellDisplay.setMap(res.toMap());
		marginalPerCellDisplay.setMap(model.getMarginalUtilities().toMap());
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
		super.initialise(data, info, region);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		Box total = new Box(BoxLayout.Y_AXIS);
		Box perCell = new Box(BoxLayout.Y_AXIS);
		total.add(supplyDisplay.getDisplay());
		total.add(demandDisplay.getDisplay());
		total.add(residualDisplay.getDisplay());
		total.add(marginalDisplay.getDisplay());
		perCell.add(supplyPerCellDisplay.getDisplay());
		perCell.add(demandPerCellDisplay.getDisplay());
		perCell.add(residualPerCellDisplay.getDisplay());
		perCell.add(marginalPerCellDisplay.getDisplay());
		total.setBorder(new TitledBorder(new EtchedBorder(), "Total"));
		perCell.setBorder(new TitledBorder(new EtchedBorder(), "Per Cell"));
		add(total);
		add(perCell);
	}

	public static void main(String args[]) {
		RegionalDemandModel dm = new RegionalDemandModel();
		RegionalDemandDisplay display = dm.getDisplay();
		display.update();

		JFrame frame = new JFrame("Regional Demand Display");
		frame.getContentPane().add(display.getDisplay());

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(500, 600));
		frame.setVisible(true);
	}
}