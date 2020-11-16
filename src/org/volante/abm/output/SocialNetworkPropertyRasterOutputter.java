/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 20 Jan 2015
 */
package org.volante.abm.output;

import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.Cell;

import de.cesr.more.measures.MMeasureDescription;
import de.cesr.more.measures.node.MNodeMeasureManager;

/**
 * @author Sascha Holzhauer
 *
 */
public class SocialNetworkPropertyRasterOutputter extends RasterOutputter {

	@Attribute(required = true)
	String measure;

	@ElementMap(inline = true, required = false, entry = "param", attribute = true, key = "name")
	Map<String, Object> parameters = new HashMap<String, Object>();

	/**
	 * @see org.volante.abm.serialization.CellToDouble#apply(org.volante.abm.data.Cell)
	 */
	@Override
	public double apply(Cell c) {
		if (c.getRegion().getNetwork() == null) {
			throw new IllegalStateException(
					"Network has not been set at region " + c.getRegion() + "!");
		} else if (!MNodeMeasureManager.getInstance().hasMeasureCalculation(
				c.getRegion().getNetwork(), measure)) {
			throw new IllegalStateException("Measure (" + measure
					+ ") has not been set at region " + c.getRegion() + "!");
			
		} else {
			Agent agent = c.getOwner();

			if (c != agent.getHomeCell() || !(agent instanceof SocialAgent)) {
				return Double.NaN;
			} else {
				SocialAgent sagent = ((SocialAgent) agent);
				return sagent.getNetworkMeasureObject(
						c.getRegion().getNetwork(),
						new MMeasureDescription(measure)).doubleValue();
			}
		}
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "SNMeasure-" + measure;
	}
}
