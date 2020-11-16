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
package org.volante.abm.agent;


import org.apache.log4j.Logger;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.param.GeoPa;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import de.cesr.more.basic.agent.MAgentNetworkComp;
import de.cesr.more.basic.agent.MoreAgentNetworkComp;
import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.basic.network.MoreNetwork;
import de.cesr.more.measures.MMeasureDescription;
import de.cesr.more.measures.node.MNodeMeasures;
import de.cesr.more.measures.node.MoreNodeMeasureSupport;
import de.cesr.parma.core.PmParameterManager;


/**
 * @author Sascha Holzhauer
 * 
 */
public class DefaultSocialLandUseAgent extends DefaultLandUseAgent implements SocialAgent,
		GeoAgent {

	/**
	 * Logger
	 */
	static private Logger										logger			= Logger.getLogger(DefaultSocialLandUseAgent.class);

	static public int numberAgents = 0;

	MoreAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>>	netComp			= new MAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>>(
																						this);
	protected MNodeMeasures										measures		= new MNodeMeasures();




	/**
	 * @param id
	 *        agent id
	 * @param data
	 *        model data
	 */
	public DefaultSocialLandUseAgent(String id, ModelData data) {
		super(id, data);
		numberAgents++;
	}

	/**
	 * Mainly used for testing purposes
	 * 
	 * @param fRole
	 *            potential agent
	 * @param id
	 *            agent id
	 * @param data
	 *            model data
	 * @param r
	 *            region
	 * @param prod
	 *            production model
	 * @param givingUp
	 *            giving up threshold
	 * @param givingIn
	 *            giving in threshold
	 */
	public DefaultSocialLandUseAgent(FunctionalRole fRole, String id, ModelData data,
			Region r, ProductionModel prod, double givingUp, double givingIn) {
		super(fRole, id, data, r, prod, givingUp, givingIn);
		numberAgents++;
	}

	/**
	 * @see org.volante.abm.agent.DefaultLandUseAgent#receiveNotification(de.cesr.more.basic.agent.MoreObservingNetworkAgent.NetworkObservation,
	 *      org.volante.abm.agent.Agent)
	 */
	@Override
	public void receiveNotification(NetworkObservation observation, Agent object) {
		if (this.getBC() instanceof InnovativeBC)
			((InnovativeBC) this.getBC()).receiveNotification(observation,
					object);
	}


	/**
	 * Perceive social network regarding each innovation the agent is aware of.
	 * 
	 * @see org.volante.abm.agent.SocialAgent#perceiveSocialNetwork()
	 */
	@Override
	public void perceiveSocialNetwork() {
		if (this.getBC() instanceof InnovativeBC)
			((InnovativeBC) this.getBC()).perceiveSocialNetwork();
	}


	/********************************
	 * Basic agent methods
	 *******************************/

	/**
	 * Preliminary!
	 * 
	 * @see org.volante.abm.agent.GeoAgent#addToGeography()
	 */
	@Override
	public void addToGeography() {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("This agent is added to geography.");
		}
		// LOGGING ->

		Cell c = this.cells.iterator().next();
		Geometry geom = getRegion().getGeoFactory().createPoint(
				new Coordinate(c.getX()
						*
						((Double) PmParameterManager.getInstance(region).getParam(
								GeoPa.AGENT_COORD_FACTOR)).doubleValue(),
						c.getY()
								* ((Double) PmParameterManager.getInstance(region)
										.getParam(GeoPa.AGENT_COORD_FACTOR))
										.doubleValue()));
		this.getRegion().getGeography().move(this, geom);
	}


	/**
	 * @see org.volante.abm.agent.DefaultLandUseAgent#die()
	 */
	@Override
	public void die() {
		if (this.region.getNetworkService() != null && this.region.getNetwork() != null) {
			this.region.getNetworkService().removeNode(this.region.getNetwork(), this);
		}

		if (this.region.getGeography() != null
				&& this.region.getGeography().getGeometry(this) != null) {
			this.region.getGeography().move(this, null);
		}
	}

	/********************************
	 * GETTER and SETTER
	 *******************************/

	/**
	 * @see de.cesr.more.basic.agent.MoreNetworkAgent#setNetworkComp(de.cesr.more.basic.agent.MoreAgentNetworkComp)
	 */
	@Override
	public void setNetworkComp(MoreAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>> netComp) {
		this.netComp = netComp;
	}

	/**
	 * @see de.cesr.more.basic.agent.MoreNetworkAgent#getNetworkComp()
	 */
	@Override
	public MoreAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>> getNetworkComp() {
		return this.netComp;
	}

	/**
	 * @see de.cesr.more.measures.node.MoreNodeMeasureSupport#setNetworkMeasureObject(de.cesr.more.basic.network.MoreNetwork,
	 *      de.cesr.more.measures.MMeasureDescription, java.lang.Number)
	 */
	@Override
	public void setNetworkMeasureObject(
			MoreNetwork<? extends MoreNodeMeasureSupport, ?> network,
			MMeasureDescription key, Number value) {
		this.measures.setNetworkMeasureObject(network, key, value);
	}

	/**
	 * @see de.cesr.more.measures.node.MoreNodeMeasureSupport#getNetworkMeasureObject(de.cesr.more.basic.network.MoreNetwork,
	 *      de.cesr.more.measures.MMeasureDescription)
	 */
	@Override
	public Number getNetworkMeasureObject(
			MoreNetwork<? extends MoreNodeMeasureSupport, ?> network,
			MMeasureDescription key) {
		return this.measures.getNetworkMeasureObject(network, key);
	}

	/**
	 * @see de.cesr.more.rs.building.MoreMilieuAgent#getMilieuGroup()
	 */
	@Override
	public int getMilieuGroup() {
		return this.getFC().getFR().getSerialID();
	}

	/**
	 * @see de.cesr.more.rs.building.MoreMilieuAgent#getAgentId()
	 */
	@Override
	public String getAgentId() {
		return this.id;
	}
}
