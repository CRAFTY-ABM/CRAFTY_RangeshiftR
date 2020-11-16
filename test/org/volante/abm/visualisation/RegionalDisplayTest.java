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
 * Created by Sascha Holzhauer on 9 Dec 2014
 */
package org.volante.abm.visualisation;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.RegionalDemandModel;

/**
 * @author Sascha Holzhauer
 *
 */
public class RegionalDisplayTest {
	public static void main(String[] args) throws Exception {
		BasicTestsUtils bt = new BasicTestsUtils();
		Region r = bt.r1;
		RegionalDemandModel dem = new RegionalDemandModel();
		r.setDemandModel(dem);
		r.initialise(BasicTestsUtils.modelData, BasicTestsUtils.runInfo, r);
		RegionalDisplay rd = new RegionalDisplay();
		rd.initialise(BasicTestsUtils.modelData, BasicTestsUtils.runInfo, r);
		rd.setRegion(r);

		JFrame frame = new JFrame("Regional Display Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(rd.getDisplay());
		frame.setSize(new Dimension(600, 1000));
		frame.setVisible(true);
	}
}
