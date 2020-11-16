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
 */
package org.volante.abm.agent;


import org.volante.abm.agent.bt.BehaviouralComponent;
import org.volante.abm.agent.bt.PseudoBT;
import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.agent.fr.LazyFR;
import org.volante.abm.agent.property.DoublePropertyProvider;
import org.volante.abm.agent.property.PropertyProvider;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.models.ProductionModel;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

import de.cesr.more.basic.agent.MoreObservingNetworkAgent;



/**
 * An interface detailing all the methods an Agent has to provide.
 *
 * @author dmrust
 *
 */
public interface Agent extends DoublePropertyProvider, PropertyProvider<Object>,
		MoreObservingNetworkAgent<Agent> {


	/**
	 * @return the cell that is considered as the agent's base cell
	 */
	public Cell getHomeCell();

	/**
	 * Sets this agent's home cell
	 * 
	 * @param homecell
	 *            new home cell
	 */
	public void setHomeCell(Cell homecell);


	/**
	 * Called to remove the agent instance from the system.
	 */
	public void die();

	/**
	 * Returns the agent's ID/type
	 *
	 * @return ID
	 */
	public String getID();


	/**
	 *
	 * Returns useful descriptive information about this agent
	 * 
	 * @return info
	 */
	public String infoString();

	/**
	 * Called at the beginning of each tick to allow the agent to do any internal housekeeping
	 */
	public void tickStartUpdate();

	/**
	 * Called at the ending of each tick to allow the agent to do any internal
	 * housekeeping
	 */
	public void tickEndUpdate();

	public void setRegion(Region r);

	public Region getRegion();


	/**
	 * Access Methods
	 */

	/**
	 * Access to this agent's behavioural component
	 * 
	 * @return bc
	 */
	public BehaviouralComponent getBC();

	public void setBC(BehaviouralComponent bt);

	public FunctionalComponent getFC();

	public void setFC(FunctionalComponent fr);

	public static String NOT_MANAGED_AGENT_ID = "NOT_MANAGED";
	public static String NOT_MANAGED_FR_ID = "Lazy FR";
	public static double	NOT_MANAGED_COMPETITION	= -Double.MAX_VALUE;
	
	
	public static LandUseAgent NOT_MANAGED = new AbstractLandUseAgent(null) {

		{
			id = NOT_MANAGED_AGENT_ID;
			this.setProperty(AgentPropertyIds.COMPETITIVENESS,
					Agent.NOT_MANAGED_COMPETITION);
		}

		@Override
		public ProductionModel getProductionModel() {
			return null;
		}

		protected void initHook() {
			new PseudoBT().assignNewBehaviouralComp(this);
			LazyFR.getInstance().assignNewFunctionalComp(this);
		}

		@Override
		public void updateSupply() {

		}

		@Override
		public void considerGivingUp() {
			// nothing to do
		}

		@Override
		public UnmodifiableNumberMap<Service> supply(Cell c) {
			return null;
		}

		@Override
		public void die() {
			// nothing to do
		}

		@Override
		public void updateCompetitiveness() {
			// nothing to do
		}

		@Override
		public boolean canTakeOver(Cell c, double competitiveness) {
			return true;
		}

		@Override
		public String infoString() {
			return NOT_MANAGED_AGENT_ID;
		}

		/**
		 * @see org.volante.abm.agent.AbstractLandUseAgent#toString()
		 */
		public String toString() {
			return NOT_MANAGED_AGENT_ID;
		}

		@Override
		public void receiveNotification(
				de.cesr.more.basic.agent.MoreObservingNetworkAgent.NetworkObservation observation,
				Agent object) {
		}

		@Override
		public void setHomeCell(Cell homecell) {
		}
	};
}
