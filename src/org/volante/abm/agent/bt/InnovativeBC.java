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
 * Created by Sascha Holzhauer on 05.02.2014
 */
package org.volante.abm.agent.bt;


import java.util.Set;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.AgentAccessible;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.status.InnovationState;
import org.volante.abm.institutions.innovation.status.InnovationStates;

import de.cesr.more.basic.agent.MoreObservingNetworkAgent;

/**
 * @author Sascha Holzhauer
 *
 */
public interface InnovativeBC extends BehaviouralComponent, AgentAccessible,
		MoreObservingNetworkAgent<Agent> {

	/**
	 * Checks each registered innovation for its status in order to consider
	 * taking the next status. Calls
	 * {@link InnovativeBC#considerTrial(Innovation)},
	 * {@link InnovativeBC#considerAdoption(Innovation)}, or
	 * {@link InnovativeBC#considerRejection(Innovation)} as appropriate.
	 * This method is usually called by the schedule.
	 */
	public void considerInnovationsNextStep();

	/**
	 * Make this agent aware of the given innovation, i.e. set the innovation status to
	 * {@link InnovationStates#AWARE}.
	 * 
	 * @param innovation
	 */
	public void makeAware(Innovation innovation);


	/**
	 * Lets this agent decide whether to trial the given innovation.
	 * 
	 * @param innovation
	 */
	public void considerTrial(Innovation innovation);

	/**
	 * Gives the given innovation a trial. Sets the innovation's state to
	 * {@link InnovationStates#TRIAL}.
	 * 
	 * @param innovation
	 */
	public void makeTrial(Innovation innovation);

	/**
	 * Lets this agent decide whether to adopt the given innovation.
	 * 
	 * @param innovation
	 */
	public void considerAdoption(Innovation innovation);

	/**
	 * Adopts the given innovation. Sets the innovation's state to {@link InnovationStates#ADOPTED}.
	 * 
	 * @param innovation
	 */
	public void makeAdopted(Innovation innovation);


	/**
	 * Lets this agent decide whether to reject the given innovation.
	 * 
	 * @param innovation
	 */
	public void considerRejection(Innovation innovation);

	/**
	 * Rejects the given innovation. Sets the innovation's state to
	 * {@link InnovationStates#REJECTED}.
	 * 
	 * @param innovation
	 */
	public void rejectInnovation(Innovation innovation);

	public void removeInnovation(Innovation innvoation);

	public void perceiveSocialNetwork();

	/********************************
	 * GETTER and SETTER
	 *******************************/

	/**
	 * Provides a set of {@link Innovation}s this agent is aware of.
	 * 
	 * @return set of innovations
	 */
	public Set<Innovation> getInnovationsAwareOf();

	/**
	 * @param innovation
	 * @return the given innovation's current state
	 */
	public InnovationState getState(Innovation innovation);
}
