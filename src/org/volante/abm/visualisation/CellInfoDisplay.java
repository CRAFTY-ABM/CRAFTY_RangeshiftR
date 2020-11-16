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
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;


public class CellInfoDisplay extends JPanel {
	private static final long	serialVersionUID					= -4128584661818932514L;

	DoubleMapDisplay			capitalDisplay						= new DoubleMapTextDisplay();
	DoubleMapDisplay			baseCapitalDisplay					= new DoubleMapTextDisplay();
	DoubleMapDisplay			productionDisplay					= new DoubleMapTextDisplay();
	DoubleMapDisplay			competitivenessDisplay				= new DoubleMapTextDisplay();
	DoubleMapDisplay			unadjustedCompetitivenessDisplay	= new DoubleMapTextDisplay();
	JTextArea					owner								= new JTextArea("Unknown", 5, 0);
	JLabel						xLoc								= new JLabel("X=?");
	JLabel						yLoc								= new JLabel("Y=?");
	JLabel						cellRegion							= new JLabel("Region=?");

	public CellInfoDisplay() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		owner.setLineWrap(true);
		JScrollPane ownerScroll = new JScrollPane(owner);
		ownerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		ownerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		Box location = new Box(BoxLayout.Y_AXIS);
		location.add(xLoc);
		location.add(yLoc);
		location.add(cellRegion);
		addPanel(location, "Location");
		addPanel(ownerScroll, "Owner");
		addPanel(capitalDisplay.getDisplay(), "Capitals");
		addPanel(baseCapitalDisplay.getDisplay(), "Base Capitals");
		addPanel(productionDisplay.getDisplay(), "Productivity");
		addPanel(competitivenessDisplay.getDisplay(), "Competitiveness");
		addPanel(unadjustedCompetitivenessDisplay.getDisplay(), "Unadjusted Competitiveness");
		setPreferredSize(new Dimension(250, 700));
		clearCell();

	}

	public void addPanel(JComponent cDisp, String title) {
		cDisp.setBorder(new TitledBorder(new EtchedBorder(), title));
		// TODO assign the height that is required for contained text (seems challenging...)
		// int height = cDisp.getHeight();
		cDisp.setPreferredSize(new Dimension(250, 170));
		cDisp.setMaximumSize(new Dimension(250, 170));
		cDisp.setAlignmentX(0.5f);
		add(cDisp);
	}

	public void setCell(Cell c) {
		if (c == null) {
			clearCell();
			return;
		}
		capitalDisplay.setMap(c.getEffectiveCapitals().toMap());
		baseCapitalDisplay.setMap(c.getBaseCapitals().toMap());
		productionDisplay.setMap(c.getSupply().toMap());
		competitivenessDisplay.setMap(getCompetitivenessMap(c));
		unadjustedCompetitivenessDisplay.setMap(getUnadjustedCompetitivenessMap(c));
		owner.setText(c.getOwnersFrLabel() + "\n"
				+ (c.getOwner() != null ? c.getOwner().infoString() : ""));
		setCellXY(c.getX(), c.getY());
		setCellRegion(c.getRegion());
		revalidate();
		repaint();
	}

	public void setCellXY(int x, int y) {
		xLoc.setText("X=" + (x != Integer.MIN_VALUE ? x + "" : "?"));
		yLoc.setText("Y=" + (y != Integer.MIN_VALUE ? y + "" : "?"));
	}

	public void setCellRegion(Region region) {
		cellRegion.setText(region == null ? "NN" : "Region: " + region.getID());
	}

	public void clearCell() {
		capitalDisplay.clear();
		baseCapitalDisplay.clear();
		productionDisplay.clear();
		competitivenessDisplay.clear();
		unadjustedCompetitivenessDisplay.clear();
		owner.setText("NONE SELECTED");
		setCellXY(Integer.MIN_VALUE, Integer.MIN_VALUE);
		setCellRegion(null);
		revalidate();
		repaint();
	}

	public Map<String, Double> getCompetitivenessMap(Cell c) {
		Map<String, Double> map = new HashMap<String, Double>();
		Region r = c.getRegion();
		if (r != null) {
			for (FunctionalRole fr : r.getFunctionalRoleMapByLabel().values()) {
				map.put(fr.getLabel(), r.getCompetitiveness(fr, c));
			}
		}
		return map;
	}

	public Map<String, Double> getUnadjustedCompetitivenessMap(Cell c) {
		Map<String, Double> map = new HashMap<String, Double>();
		Region r = c.getRegion();
		if (r != null) {
			for (FunctionalRole fr : r.getFunctionalRoleMapByLabel().values()) {
				map.put(fr.getLabel(),
 r.getUnadjustedCompetitiveness(fr, c));
			}
		}
		return map;
	}

	public static void main(String[] args) throws Exception {
		ModelData data = new ModelData();
		Region r = new Region();
		Cell c = new Cell(20, 40);
		r.addCell(c);
		c.initialise(data, new RunInfo(), null);

		CellInfoDisplay display = new CellInfoDisplay();

		JFrame frame = new JFrame("Test Cell Display");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(new Dimension(500, 500));

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(display, BorderLayout.CENTER);
		p.add(new JPanel(), BorderLayout.WEST);
		frame.add(p);
		frame.pack();
		frame.setVisible(true);
		display.setCell(c);
	}
}
