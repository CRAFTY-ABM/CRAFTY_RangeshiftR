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
 * Created by Sascha Holzhauer on 20 Mar 2015
 */
package org.volante.abm.agent.assembler;

import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.serialization.Initialisable;

/**
 * @author Sascha Holzhauer
 *
 */
public interface AgentAssembler extends Initialisable {

	/**
	 * Creates a new agent and initialises components for given ids
	 * 
	 * @param homecell
	 * @param btId
	 * @param frId
	 * @return new agent
	 */
	public LandUseAgent assembleAgent(Cell homecell, int btId, int frId);

	public LandUseAgent assembleAgent(Cell homecell, int btId, int frId,
			String agentLabel);

	public LandUseAgent assembleAgent(Cell homecell, String btLabel, String frLabel);

	public LandUseAgent assembleAgent(Cell homecell, String btLabel, String frLabel,
			String agentLabel);
}
