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
 * Created by Sascha Holzhauer on 27 May 2015
 */
package org.volante.abm.agent.bt;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.AgentAccessible;

/**
 * @author Sascha Holzhauer
 *
 */
public class AbstractIndividualBC extends AbstractBC implements AgentAccessible {

	/**
	 * @param bType
	 * @param agent
	 */
	public AbstractIndividualBC(BehaviouralType bType, Agent agent) {
		super(bType);
		this.agent = agent;
	}

	protected Agent agent = null;

	/**
	 * @see org.volante.abm.agent.AgentAccessible#getAgent()
	 */
	@Override
	public Agent getAgent() {
		return this.agent;
	}
}
