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
 * Created by Sascha Holzhauer on 04.03.2014
 */
package org.volante.abm.serialization;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.ElementMapUnion;
import org.simpleframework.xml.Root;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultSocialLandUseAgent;
import org.volante.abm.agent.GeoAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.output.CraftyNodeMeasure;
import org.volante.abm.param.GeoPa;
import org.volante.abm.param.RandomPa;
import org.volante.abm.param.SocialNetworkPa;
import org.volante.abm.schedule.CraftyMoreSchedule;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;

import de.cesr.more.basic.MManager;
import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.building.edge.MoreEdgeFactory;
import de.cesr.more.building.network.AgentLabelFactory;
import de.cesr.more.building.network.MoreNetworkService;
import de.cesr.more.geo.building.edge.MDefaultGeoEdgeFactory;
import de.cesr.more.geo.building.edge.MGeoNotifyingNetworkEdgeModifier;
import de.cesr.more.geo.building.network.MGeoRestoreNetworkService;
import de.cesr.more.geo.building.network.MoreGeoNetworkService;
import de.cesr.more.param.MBasicPa;
import de.cesr.more.param.MNetBuildBhPa;
import de.cesr.more.param.MNetBuildHdffPa;
import de.cesr.more.param.MNetworkBuildingPa;
import de.cesr.more.param.MRandomPa;
import de.cesr.more.param.reader.MMilieuNetDataCsvReader;
import de.cesr.more.param.reader.MMilieuNetLinkDataCsvReader;
import de.cesr.more.util.io.MoreIoUtilities;
import de.cesr.parma.core.PmParameterDefinition;
import de.cesr.parma.core.PmParameterManager;
import edu.uci.ics.jung.io.GraphMLMetadata;


/**
 * @author Sascha Holzhauer
 *
 */
@Root(name = "socialNetwork")
public class SocialNetworkLoader {

	/**
	 * Logger
	 */
	static private Logger	logger					= Logger.getLogger(SocialNetworkLoader.class);

	@Attribute(name = "name")
	String					name					= "Unknown";

	/**
	 * Location of ABT-specific CSV parameter file for network composition
	 */
	@Element(required = false, name = "abtParams")
	String					abtNetworkParamFile		= "";

	/**
	 * Location of ABT-specific CSV parameter file for social network initialisation
	 */
	@Element(required = false, name = "abtLinkParams")
	String					abtNetworkLinkParamFile	= "";

	@Element(required = false, name = "networkGeneratorClass")
	String					networkGeneratorClass	= "de.cesr.more.building.network.MWattsBetaSwBuilder.class";

	@Element(required = false)
	protected boolean outputAgentCoords = false;

	@Element(required = false)
	protected boolean outputAgentFrId = false;

	@ElementMapUnion({
			@ElementMap(inline = true, entry = "Integer", attribute = true, required = false, key = "param", valueType = Integer.class),
			@ElementMap(inline = true, entry = "Double", attribute = true, required = false, key = "param", valueType = Double.class),
			@ElementMap(inline = true, entry = "Float", attribute = true, required = false, key = "param", valueType = Float.class),
			@ElementMap(inline = true, entry = "Long", attribute = true, required = false, key = "param", valueType = Long.class),
			@ElementMap(inline = true, entry = "Character", attribute = true, required = false, key = "param", valueType = Character.class),
			@ElementMap(inline = true, entry = "Boolean", attribute = true, required = false, key = "param", valueType = Boolean.class),
			@ElementMap(inline = true, entry = "String", attribute = true, required = false, key = "param", valueType = String.class) })
	Map<String, Object>		params					= new HashMap<String, Object>();

	@Element(required = false, name = "DYN_EDGE_WEIGHT_UPDATER")
	String					edgeWeightUpdaterClass	= "de.cesr.more.manipulate.agent.MPseudoEgoNetworkProcessor";

	@Element(required = false, name = "DYN_EDGE_MANAGER")
	String					edgeManagerClass		= "de.cesr.more.manipulate.agent.MPseudoEgoNetworkProcessor";

	/**
	 * The tick (year) the social network is going to be initialised. Especially for simulations
	 * that do not define AFTs for each cell to start with it may be a good idea to initialise the
	 * network after some setting phase to omit unnecessary running time for network initialisation
	 * of a population that is going to change anyway. Of course, if the network is relevant for AFT
	 * allocation this is different.
	 */
	@Element(required = false, name = "initTick")
	int						initTick				= Integer.MIN_VALUE;

	@ElementList(entry = "measure", required = false, name = "nodemeasures")
	List<CraftyNodeMeasure> nodeMeasures = new ArrayList<CraftyNodeMeasure>();

	Region					region;

	PmParameterManager		pm;

	/**
	 * @return the pm
	 */
	public PmParameterManager getPm() {
		return pm;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the networkGeneratorClass
	 */
	public String getNetworkGeneratorClass() {
		return networkGeneratorClass;
	}

	/**
	 * Initialises parameters and creates the social network.
	 *
	 * @param data
	 * @param info
	 * @param extent
	 * @throws Exception
	 */
	public void initialise(final ModelData data, final RunInfo info,
			Region extent) throws Exception {
		this.region = extent;

		// read parameter
		this.pm = PmParameterManager.getInstance(region);

		this.pm.copyParamValue(RandomPa.RANDOM_SEED_INIT_NETWORK, MRandomPa.RANDOM_SEED);
		
		this.pm.setParam(MNetBuildHdffPa.DISTANCE_FACTOR_FOR_DISTRIBUTION, new Double(1.0));
		
		// otherwise, non existing land manger types could be searched for forever
		this.pm.setParam(MNetBuildBhPa.DISTANT_FORCE_MILIEU, Boolean.FALSE);

		for (Map.Entry<String, Object> param : params.entrySet()) {
			PmParameterDefinition p = PmParameterManager.parse(param.getKey());
			
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Read param: " + p + " / " + param.getValue());
			}
			// LOGGING ->

			if (Class.class.isAssignableFrom(p.getType())
					&& param.getValue() instanceof String) {
				pm.setParam(p, Class.forName(((String) param.getValue()).trim()));
			} else {
				pm.setParam(p, param.getValue());
			}
		}
		
		// <- LOGGING
		pm.logParamValues(MBasicPa.values(), MNetworkBuildingPa.values(), GeoPa.values());
		// LOGGING ->
		
		if (abtNetworkParamFile != "") {
			pm.setParam(MNetworkBuildingPa.MILIEU_NETWORK_CSV_MILIEUS,
					info.getPersister().getFullPath(abtNetworkParamFile,
							this.region.getPersisterContextExtra()));
			new MMilieuNetDataCsvReader(pm).initParameters();
		}

		if (abtNetworkLinkParamFile != "") {
			pm.setParam(MNetworkBuildingPa.MILIEU_NETWORK_CSV_MILIEULINKS, info.getPersister()
.getFullPath(abtNetworkLinkParamFile,
							this.region.getPersisterContextExtra()));
			new MMilieuNetLinkDataCsvReader(pm).initParameters();
		}

		final MoreNetworkService<SocialAgent, MoreEdge<SocialAgent>> networkService = initNetworkInitialiser();

		logger.info("Init social network inititialiser for " + this.region.getAgents().size()
				+ " agents in region " + this + " using " + networkService);

		this.region.setNetworkService(networkService);


		// allow scheduling later
		final int tick = (this.initTick == Integer.MIN_VALUE) ? info.getSchedule().getCurrentTick() + 1
				: this.initTick;

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Register Pretick action for tick " + tick + " at schedule " + info.getSchedule());
		}
		// LOGGING ->

		info.getSchedule().register(getNetworkInitPreTickAction(networkService, data, info, tick));
	}

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

					// <- LOGGING
					logger.info("Add " + region.getAgents().size() + " agents to geography...");
					// LOGGING ->
					
					for (Agent a : region.getAgents()) {
						if (a instanceof GeoAgent
								&& region.getNetworkService() instanceof MoreGeoNetworkService) {
							((DefaultSocialLandUseAgent) a).addToGeography();
						}
					}
					
					// <- LOGGING
					logger.info("Build social network...");
					// LOGGING ->

					region.setNetwork(networkService
							.buildNetwork(socialAgentSet));

					// output network
					if ((Boolean) pm.getParam(SocialNetworkPa.OUTPUT_NETWORK_AFTER_CREATION)) {

						Map<String, GraphMLMetadata<SocialAgent>> metaData = setVertexMetaData();

						MoreIoUtilities.outputGraph(
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
						MManager.setSchedule(new CraftyMoreSchedule(info
								.getSchedule()));

						for (CraftyNodeMeasure nmeasure : nodeMeasures) {
							try {
								nmeasure.initialise(data, info, region);
							} catch (Exception exception) {
								logger.error("Error during initialisation of "
										+ nmeasure);
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


				if (SocialNetworkLoader.this.outputAgentFrId) {
					metaData.put("FR", new GraphMLMetadata<SocialAgent>("FR[int]", "0",
					        new Transformer<SocialAgent, String>() {
						        @Override
						        public String transform(SocialAgent agent) {
							        return agent.getFC().getFR().getSerialID() + "";
						        }
					        }));
				}

				if (SocialNetworkLoader.this.outputAgentCoords) {
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

	/**
	 * @return the network service
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected MoreNetworkService<SocialAgent, MoreEdge<SocialAgent>> initNetworkInitialiser() {
		MoreNetworkService<SocialAgent, MoreEdge<SocialAgent>> networkInitializer = null;
		try {
			networkInitializer = (MoreNetworkService<SocialAgent, MoreEdge<SocialAgent>>)
					Class.forName(networkGeneratorClass).getConstructor(
							MoreEdgeFactory.class, String.class, PmParameterManager.class)
							.newInstance(
									new MDefaultGeoEdgeFactory<Agent>(),
									this.name, this.pm);
			if (networkInitializer instanceof MoreGeoNetworkService) {
				((MoreGeoNetworkService<SocialAgent, MoreEdge<SocialAgent>>) networkInitializer)
						.
						setGeography(region.getGeography());
				((MoreGeoNetworkService<SocialAgent, MoreEdge<SocialAgent>>) networkInitializer)
						.setGeoRequestClass(SocialAgent.class);
				if (networkInitializer instanceof MGeoRestoreNetworkService) {
					((MGeoRestoreNetworkService) networkInitializer)
					        .setAgentLabelFactory(new AgentLabelFactory<SocialAgent>() {
						        @Override
						        public String getLabel(SocialAgent agent) {
							        return agent.getID();
						        }
					        });
				}
			}
			networkInitializer.setEdgeModifier(new MGeoNotifyingNetworkEdgeModifier());

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			logger.error("Error while instanciating " + networkGeneratorClass);
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException exception) {
			exception.printStackTrace();
		}
		
		return networkInitializer;
	}
}
