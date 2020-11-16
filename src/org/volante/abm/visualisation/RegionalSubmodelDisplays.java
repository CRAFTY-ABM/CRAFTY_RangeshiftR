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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.schedule.RunInfo;


/**
 * {@link Displayable} models (e.g. {@link DemandModel} can be added to the
 * {@link RegionalSubmodelDisplays} by calling {@link #setAllocationModel(AllocationModel)},
 * {@link #setCompetitivenessModel(CompetitivenessModel)}, or {@link #setDemandModel(DemandModel)}.
 * However, the standard way of assigning models is by {@link #cellChanged(Cell)} which invokes
 * above methods.
 * 
 * The agent panel is currently not implemented!
 * 
 * @author Dave Murray-Rust
 * @author Sascha Holzhauer
 * 
 */
public class RegionalSubmodelDisplays extends AbstractDisplay {

	/**
	 * Logger
	 */
	static private Logger			logger				= Logger.getLogger(RegionalSubmodelDisplays.class);

	private static final long		serialVersionUID	= -3289966236130005751L;

	JTabbedPane						tabbedPane			= null;

	JComponent						competitionPanel	= null;
	JComponent						allocationPanel		= null;
	JComponent						demandPanel			= null;
	JComponent						agentsPanel			= null;

	// submodels:
	// TODO not used
	Display							competitionDisplay	= null;
	CompetitivenessModel			competition			= null;

	// TODO not used
	Display							demandDisplay		= null;
	// TODO not used
	DemandModel						demand				= null;

	AllocationModel					allocation			= null;

	CellDisplay						map					= null;

	Map<Displayable, Display>		displays			= new HashMap<Displayable, Display>();
	Map<JComponent, Displayable>	currentSelection	= new HashMap<JComponent, Displayable>();
	JPanel							displaysPanel		= new JPanel();

	public RegionalSubmodelDisplays() {
		map = new CellDisplay()
		{
			private static final long	serialVersionUID	= -3240414837859608881L;

			@Override
			public int getColourForCell(Cell c) {
				return Color.gray.getRGB();
			}

			@Override
			public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
				super.initialise(data, info, region);
				log.info("Initialised: " + this.region.getExtent() + ", height: " + regionHeight
						+ ", width: " + regionWidth);
			}
		};

		displaysPanel.setLayout(new GridLayout(2, 2));
		// map.getMainPanel().setPreferredSize(new Dimension(200, 400));

		competitionPanel = modelPanel("Competition");
		allocationPanel = modelPanel("Allocation");
		demandPanel = modelPanel("Demand");
		agentsPanel = modelPanel("Agents");

		setLayout(new BorderLayout());
		add(displaysPanel, BorderLayout.CENTER);

		// out-commented since currently submodel displays do not consider cell selection
		// (they are meant to provide aggregate information
		// add(map, BorderLayout.WEST);
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Regions region) throws Exception {
		
		super.initialise(data, info, region);

		this.title = region.getID();
		map.initialise(data, info, region);
		map.update();
		if (region.getAllCells().iterator().hasNext()) {
			cellChanged(region.getAllCells().iterator().next());
		} else {
			logger.warn("Region " + region + " does not contain any cells!");
		}
	}

	JComponent modelPanel(String title) {
		JPanel comp = new JPanel();
		JScrollPane pane = new JScrollPane(comp);
		pane.setBorder(new TitledBorder(new EtchedBorder(), title));
		comp.setLayout(new BorderLayout());
		displaysPanel.add(pane);
		return comp;
	}

	@Override
	public void update() {
		for (Display display : displays.values()) {
			display.update();
		}

		if (competitionDisplay != null) {
			competitionDisplay.update();
		}
		if (demandDisplay != null) {
			demandDisplay.update();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void cellChanged(Cell c) {
		super.cellChanged(c);
		log.debug("Cell changed! " + c);
		setCompetitivenessModel(c.getRegion().getCompetitionModel());
		setAllocationModel(c.getRegion().getAllocationModel());
		setDemandModel(c.getRegion().getDemandModel());
		update();
	}

	public void setCompetitivenessModel(CompetitivenessModel c) {
		competition = c;
		if (competition != null) {
			addDisplay(competition, competitionPanel);
		}
	}

	public void setAllocationModel(AllocationModel c) {
		allocation = c;
		if (allocation != null) {
			addDisplay(allocation, allocationPanel);
		}
	}

	public void setDemandModel(DemandModel c) {
		demand = c;
		if (demand != null) {
			addDisplay(demand, demandPanel);
		}
	}

	/**
	 * The target component that needs to be passed is usually one of demandPanel, competitionPanel,
	 * allocationPanel, agentsPanel
	 * 
	 * @param submodel
	 * @param target
	 */
	public void addDisplay(Displayable submodel, JComponent target) {
		if (currentSelection.get(target) == submodel) {
			displays.get(submodel).update();
			return;
		}
		target.removeAll();
		try {
			if (!displays.containsKey(submodel)) {
				Display compDisp = submodel.getDisplay();
				compDisp.initialise(data, info, region);
				displays.put(submodel, compDisp);
			}
			Display com = displays.get(submodel);
			com.update();
			target.add(com.getDisplay(), BorderLayout.CENTER);
			target.invalidate();
			repaint();
			currentSelection.put(target, submodel);
		} catch (Exception e) {
			log.error("Couldn't set s display: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public JComponent getEastSidePanel() {
		// return map.getMainPanel();
		return null;
	}

	@Override
	public void setModelDisplays(ModelDisplays d) {
		super.setModelDisplays(d);
		d.registerDisplay(map);
	}
}
