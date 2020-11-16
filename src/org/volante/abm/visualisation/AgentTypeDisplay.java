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


import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.Agent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;

import de.cesr.uranus.core.URandomService;


public class AgentTypeDisplay extends CellDisplay {

	private static final long serialVersionUID = 722528466121585081L;

	@ElementMap(entry = "aftColor", key = "aft", attribute = true, inline = true, required = false)
	protected Map<String, Color> agentColours = new LinkedHashMap<String, Color>();

	@Attribute(required = false)
	String prefix = null;
	JPanel legend = new JPanel();

	public AgentTypeDisplay() {
		addAgent(Agent.NOT_MANAGED_AGENT_ID, Color.gray.brighter());
	}

	public void addAgent(String name, Color color) {
		agentColours.put(name, color);
	}

	public Color getColor(Cell c) {
		
		 
		Color col = agentColours.get(c.getOwnersFrLabel());
		if (col != null) {
			return col;
		}
		log.warn("No colour found for: " + c.getOwnersFrLabel() + " so making one up");
		Color nc = new Color(URandomService.getURandomService().getUniform().nextIntFromTo(0, 255),
		        URandomService.getURandomService().getUniform().nextIntFromTo(0, 255),
		        URandomService.getURandomService().getUniform().nextIntFromTo(0, 255));

		agentColours.put(c.getOwnersFrLabel(), nc);
		updateLegend();
		return nc;
	}

	@Override
	public int getColourForCell(Cell c) {
		return getColor(c).getRGB();
	}

	@Override
	public JComponent getLegend() {
		updateLegend();
		return legend;
	}

	public void updateLegend() {
		legend.setLayout(new FlowLayout());
		legend.removeAll();
		for (String name : agentColours.keySet()) {
			Box b = new Box(BoxLayout.Y_AXIS);
			JPanel p = new JPanel();
			p.setBackground(agentColours.get(name));
			p.setPreferredSize(new Dimension(30, 30));
			p.setAlignmentX(0.5f);
			b.add(p);
			if (prefix != null) {
				name = name.replace(prefix, "");
			}
			JLabel lab = new JLabel(name);
			lab.setAlignmentX(0.5f);
			b.add(lab);
			legend.add(b);
		}
		 
		revalidate();
	}

 
}
