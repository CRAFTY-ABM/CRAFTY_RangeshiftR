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
 * Created by Sascha Holzhauer on 26.03.2014
 */
package org.volante.abm.output;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;

import de.cesr.more.util.io.MoreIoUtilities;
import edu.uci.ics.jung.io.GraphMLMetadata;


/**
 * @author Sascha Holzhauer
 *
 */
public class GraphMlOutputter extends AbstractOutputter {

	@Attribute(required = false, name = "tickPattern")
	String	tickPattern	= "";

	@Attribute(required = false, name = "addCoordinates")
	boolean	addCoordinates	= false;

	@Attribute(required = false, name = "addAFTSerialID")
	boolean	addAFTSerialID	= false;

	/**
	 * @see org.volante.abm.output.Outputter#doOutput(org.volante.abm.data.Regions)
	 */
	@Override
	public void doOutput(Regions regions) {
		Map<String, GraphMLMetadata<SocialAgent>> vertexMetadata = new HashMap<String, GraphMLMetadata<SocialAgent>>();
		if (addCoordinates) {
			vertexMetadata.put("X", new GraphMLMetadata<SocialAgent>(
					"X coordinate", "NA",
					new Transformer<SocialAgent, String>() {
						@Override
						public String transform(SocialAgent agent) {
							return agent.getHomeCell() != null ? "" + agent.getHomeCell().getX()
									: "NA";
						}

					}));
			vertexMetadata.put("Y", new GraphMLMetadata<SocialAgent>(
					"Y coordinate", "NA",
					new Transformer<SocialAgent, String>() {
						@Override
						public String transform(SocialAgent agent) {
							return agent.getHomeCell() != null ? "" + agent.getHomeCell().getY()
									: "NA";
						}

					}));
		}

		if (addAFTSerialID) {
			vertexMetadata.put("AFT", new GraphMLMetadata<SocialAgent>(
					"Agent Functional Type", "-1",
					new Transformer<SocialAgent, String>() {
						@Override
						public String transform(SocialAgent agent) {
							return "" + agent.getFC().getFR().getSerialID();
						}

					}));
		}

		addVertexData(vertexMetadata);

		for (Region r : regions.getAllRegions()) {
			if (r.getNetwork() != null) {
				MoreIoUtilities.outputGraph(r.getNetwork(), new File(tickFilename(r)),
						vertexMetadata, null);
			} else {
				log.warn("There is an GraphML-Outputter defined for region " + r
						+ ", but no network is present!");
			}
		}
	}

	protected void addVertexData(
			Map<String, GraphMLMetadata<SocialAgent>> vertexMetadata) {
		// hook methods for subclasses
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "Social-Network";
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getExtension()
	 */
	@Override
	public String getExtension() {
		return "graphml";
	}

	@Override
	public String tickFilename(Regions r) {
		if (tickPattern.length() == 0) {
			tickPattern = outputs.tickPattern;
		}
		return outputs.getOutputFilename(getOutputName(), getExtension(), this.tickPattern, r);
	}
}
