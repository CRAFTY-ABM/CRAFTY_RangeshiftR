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
 * Created by Sascha Holzhauer on 13 Jul 2015
 */
package org.volante.abm.agent.assembler;


import org.apache.log4j.Logger;
import org.volante.abm.agent.DefaultLandUseAgent;
import org.volante.abm.agent.DefaultSocialLandUseAgent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.fr.LazyFR;
import org.volante.abm.data.Cell;
import org.volante.abm.example.GiveUpGiveInAllocationModel;
import org.volante.abm.models.AllocationModel;


/**
 * @author Sascha Holzhauer
 *
 */
public class DefaultSocialAgentAssembler extends DefaultAgentAssembler {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(DefaultSocialAgentAssembler.class);

	@Override
	public LandUseAgent assembleAgent(Cell homecell, int btIdInitial, int frIdInitial, String id) {
		if (region == null) {
			throw new IllegalStateException("Agent assembler has not been initialised!");
		}

		int btId;
		if (btIdInitial == Integer.MIN_VALUE) {
			btId = this.defaultBtId;
		} else {
			btId = btIdInitial;
		}

		int frId;
		if (frIdInitial == Integer.MIN_VALUE) {
			frId = this.defaultFrId;
		} else {
			frId = frIdInitial;
		}

		LandUseAgent agent =
				new DefaultSocialLandUseAgent((id != null ? id : "Agent_" + (homecell == null ? "" : homecell.toString())),
						mData);
		agent.setRegion(this.region);
		this.region.setAmbulant(agent);

		if (this.region.getFunctionalRoleMapBySerialId().containsKey(frId)) {
			this.region.getFunctionalRoleMapBySerialId().get(frId).assignNewFunctionalComp(agent);
		} else if (this.defaultFrId == Integer.MIN_VALUE) {
			LazyFR.getInstance().assignNewFunctionalComp(agent);
			logger.warn("Requested FunctionalRole (" + frId + ") not found. Using LazyFR!");
		} else {
			logger.error("Couldn't find FunctionalRole by id: " + frId);
		}

		if (this.region.getBehaviouralTypeMapBySerialId().containsKey(btId)) {
			this.region.getBehaviouralTypeMapBySerialId().get(btId).assignNewBehaviouralComp(agent);
		} else {
			logger.error("Couldn't find BehaviouralType by id: " + btId);
		}

		agent.setHomeCell(homecell);
		
		// set relative thresholding in case GuGi allocation
		AllocationModel allocation = this.region.getAllocationModel();

		if (allocation instanceof GiveUpGiveInAllocationModel) { 
			
			((DefaultLandUseAgent) agent).setRelativeThresholding(((GiveUpGiveInAllocationModel)allocation).relativeThresholding);
 
		}
			 
		
		return agent;
	}

}
