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
package org.volante.abm.institutions;


import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.SocialAgent;

import de.cesr.more.basic.agent.MAgentNetworkComp;
import de.cesr.more.basic.agent.MoreAgentNetworkComp;
import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.basic.network.MoreNetwork;
import de.cesr.more.measures.MMeasureDescription;
import de.cesr.more.measures.node.MNodeMeasures;
import de.cesr.more.measures.node.MoreNodeMeasureSupport;

/**
 * @author Sascha Holzhauer
 *
 */
public abstract class AbstractSocialCobraInstitution extends AbstractCobraInstitution implements SocialAgent {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(AbstractSocialCobraInstitution.class);

	MoreAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>> netComp =
	        new MAgentNetworkComp<SocialAgent, MoreEdge<SocialAgent>>(this);
	protected MNodeMeasures measures = new MNodeMeasures();

	public AbstractSocialCobraInstitution(@Attribute(name = "id") String id) {
		super(id);
	}


		/**
	 * @see de.cesr.more.rs.building.MoreMilieuAgent#getAgentId()
	 */
	@Override
	public String getAgentId() {
		return this.id;
	}

	/**
	 * @see org.volante.abm.agent.SocialAgent#perceiveSocialNetwork()
	 */
	@Override
	public void perceiveSocialNetwork() {
		// nothing to do
	}


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
	public void setNetworkMeasureObject(MoreNetwork<? extends MoreNodeMeasureSupport, ?> network,
	        MMeasureDescription key, Number value) {
		this.measures.setNetworkMeasureObject(network, key, value);
	}

	/**
	 * @see de.cesr.more.measures.node.MoreNodeMeasureSupport#getNetworkMeasureObject(de.cesr.more.basic.network.MoreNetwork,
	 *      de.cesr.more.measures.MMeasureDescription)
	 */
	@Override
	public Number getNetworkMeasureObject(MoreNetwork<? extends MoreNodeMeasureSupport, ?> network,
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
}
