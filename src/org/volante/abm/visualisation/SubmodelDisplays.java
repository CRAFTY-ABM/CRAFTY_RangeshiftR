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
 * Created by Sascha Holzhauer on 30 Oct 2014
 */
package org.volante.abm.visualisation;


import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class SubmodelDisplays extends JPanel implements Display {

	/**
	 * 
	 */
	private static final long				serialVersionUID	= 2402532037598915646L;

	@Attribute
	String									title				= "Unknown";

	/**
	 * Logger
	 */
	static private Logger			logger				= Logger.getLogger(SubmodelDisplays.class);

	Map<Region, RegionalSubmodelDisplays>	submodelDisplays	= new HashMap<Region, RegionalSubmodelDisplays>();
	JTabbedPane						tabbedPane			= null;

	protected ModelDisplays			modelDisplays;

	public SubmodelDisplays() {
		this.tabbedPane = new JTabbedPane();
		this.add(this.tabbedPane);
		// Sthis.setSize(new Dimension(800, 1200));
	}

	@Override
	public void postTick() {
		update();
	}

	@Override
	public void update() {
		for (RegionalSubmodelDisplays sDisplay : submodelDisplays.values()) {
			sDisplay.update();
		}
	}

	@Override
	public String getTitle() {
		return "Submodels";
	}

	@Override
	public JComponent getDisplay() {
		return this;
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Regions extent) throws Exception {
		logger.info("Initialising submodel displays...");

		for (Region region : extent.getAllRegions()) {
			submodelDisplays.put(region, new RegionalSubmodelDisplays());
		}

		for (Map.Entry<Region, RegionalSubmodelDisplays> entry : submodelDisplays.entrySet()) {
			entry.getValue().initialise(data, info, entry.getKey());
			info.getSchedule().register(entry.getValue());
			this.tabbedPane.addTab(entry.getValue().getTitle(), entry.getValue().getDisplay());
		}
		this.setVisible(true);
	}

	@Override
	public void setModelDisplays(ModelDisplays d) {
		this.modelDisplays = d;
		for (Display sDisplays : submodelDisplays.values()) {
			sDisplays.setModelDisplays(d);
		}
	}

	@Override
	public void addCellListener(Display d) {
		for (Display sDisplays : submodelDisplays.values()) {
			sDisplays.addCellListener(d);
		}
	}

	@Override
	public void cellChanged(Cell c) {
		for (Display sDisplays : submodelDisplays.values()) {
			sDisplays.cellChanged(c);
		}
	}

	@Override
	public void fireCellChanged(Cell c) {
		for (Display sDisplays : submodelDisplays.values()) {
			sDisplays.fireCellChanged(c);
		}
	}
}
