/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2016 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 7 Dec 2016
 */
package org.volante.abm.serialization;


import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultSocialLandUseAgent;
import org.volante.abm.agent.GeoAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.output.CraftyNodeMeasure;
import org.volante.abm.param.SocialNetworkPa;
import org.volante.abm.schedule.CraftyMoreSchedule;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;

import de.cesr.more.basic.MManager;
import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.building.network.MoreNetworkService;
import de.cesr.more.geo.building.network.MoreGeoNetworkService;
import de.cesr.more.util.io.MoreIoUtilities;
import edu.uci.ics.jung.io.GraphMLMetadata;


/**
 * @author Sascha Holzhauer
 *
 */
public class InstitutionalSocialNetworkLoader extends SocialNetworkLoader {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(InstitutionalSocialNetworkLoader.class);

	protected PreTickAction getNetworkInitPreTickAction(
	        final MoreNetworkService<SocialAgent, MoreEdge<SocialAgent>> networkService, final ModelData data,
	        final RunInfo info, final int tick) {

		return new PreTickAction() {

			@Override
			public void preTick() {
				if (info.getSchedule().getCurrentTick() == tick) {
					Collection<SocialAgent> socialAgentSet = new LinkedHashSet<SocialAgent>();
					for (Agent agent : region.getAgents()) {
						if (agent instanceof SocialAgent) {
							socialAgentSet.add((SocialAgent) agent);
						}
					}

					// TODO add institutions

					// <- LOGGING
					logger.info("Add " + region.getAgents().size() + " agents to geography...");
					// LOGGING ->

					for (Agent a : region.getAgents()) {
						if (a instanceof GeoAgent && region.getNetworkService() instanceof MoreGeoNetworkService) {
							((DefaultSocialLandUseAgent) a).addToGeography();
						}
					}

					// <- LOGGING
					logger.info("Build social network...");
					// LOGGING ->

					region.setNetwork(networkService.buildNetwork(socialAgentSet));

					// output network
					if ((Boolean) pm.getParam(SocialNetworkPa.OUTPUT_NETWORK_AFTER_CREATION)) {

						Map<String, GraphMLMetadata<SocialAgent>> metaData = setVertexMetaData();

						MoreIoUtilities
						        .outputGraph(
						                region.getNetwork(),
						                new File(
						                        info.getOutputs()
						                                .getOutputFilename(
						                                        "Social-Network",
						                                        "graphml",
						                                        (String) pm
						                                                .getParam(SocialNetworkPa.OUTPUT_NETWORK_AFTER_CREATION_TICKPATTERN),
						                                        region)), metaData, null,
						                new Transformer<SocialAgent, String>() {
							                @Override
							                public String transform(SocialAgent input) {
								                return input.getID();
							                }
						                });
					}

					if (nodeMeasures.size() > 0) {
						MManager.setSchedule(new CraftyMoreSchedule(info.getSchedule()));

						for (CraftyNodeMeasure nmeasure : nodeMeasures) {
							try {
								nmeasure.initialise(data, info, region);
							} catch (Exception exception) {
								logger.error("Error during initialisation of " + nmeasure);
								exception.printStackTrace();
							}
						}
					}
				}
			}

			/**
			 * @return
			 */
			protected Map<String, GraphMLMetadata<SocialAgent>> setVertexMetaData() {
				Map<String, GraphMLMetadata<SocialAgent>> metaData =
				        new HashMap<String, GraphMLMetadata<SocialAgent>>();

				if (InstitutionalSocialNetworkLoader.this.outputAgentFrId) {
					metaData.put("FR", new GraphMLMetadata<SocialAgent>("FR[int]", "0",
					        new Transformer<SocialAgent, String>() {
						        @Override
						        public String transform(SocialAgent agent) {
							        return agent.getFC().getFR().getSerialID() + "";
						        }
					        }));
				}

				if (InstitutionalSocialNetworkLoader.this.outputAgentCoords) {
					metaData.put("X", new GraphMLMetadata<SocialAgent>("X[int]", "0",
					        new Transformer<SocialAgent, String>() {
						        @Override
						        public String transform(SocialAgent agent) {
							        return Math.round(agent.getHomeCell().getX()) + "";
						        }
					        }));

					metaData.put("Y", new GraphMLMetadata<SocialAgent>("Y[int]", "0",
					        new Transformer<SocialAgent, String>() {
						        @Override
						        public String transform(SocialAgent agent) {
							        return Math.round(agent.getHomeCell().getY()) + "";
						        }
					        }));
				}
				return metaData;
			}
		};
	}
}
