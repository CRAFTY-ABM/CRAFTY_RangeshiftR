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
 * Created by Sascha Holzhauer on 24 Mar 2015
 */
package org.volante.abm.agent.bt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.institutions.innovation.AdoptionObservation;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.status.InnovationState;
import org.volante.abm.institutions.innovation.status.InnovationStates;
import org.volante.abm.institutions.innovation.status.InnovationStatus;
import org.volante.abm.institutions.innovation.status.SimpleInnovationStatus;

/**
 * @author Sascha Holzhauer
 *
 */
public class InnovativeCognitiveBC extends CognitiveBC implements InnovativeBC {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(InnovativeCognitiveBC.class);

	static public int numberAdoptions = 0;


	protected Map<Innovation, InnovationStatus> innovations = new LinkedHashMap<Innovation, InnovationStatus>();

	protected boolean initialAdoptionObservationPerformed = false;

	/**
	 * @param bType
	 * @param agent
	 */
	public InnovativeCognitiveBC(BehaviouralType bType, Agent agent) {
		super(bType, agent);
	}

	/********************************
	 * Innovation actions
	 *******************************/

	/**
	 * @see org.volante.abm.agent.bt.InnovativeBC#considerInnovationsNextStep()
	 */
	@Override
	public void considerInnovationsNextStep() {
		for (Map.Entry<Innovation, InnovationStatus> entry : this.innovations
				.entrySet()) {
			if (entry.getValue().getState().equals(InnovationStates.AWARE)) {
				this.considerTrial(entry.getKey());
			} else if (entry.getValue().getState()
					.equals(InnovationStates.TRIAL)) {
				this.considerAdoption(entry.getKey());
			} else if (entry.getValue().getState()
					.equals(InnovationStates.ADOPTED)) {
				this.considerRejection(entry.getKey());
			}
		}
	}

	/**
	 * @see org.volante.abm.agent.bt.InnovativeBC#makeAware(org.volante.abm.institutions.innovation.Innovation)
	 */
	@Override
	public void makeAware(Innovation innovation) {
		if (!this.innovations.containsKey(innovation)) {
			this.innovations.put(innovation, new SimpleInnovationStatus());
			this.innovations.get(innovation).aware();
		} else {
			this.innovations.get(innovation).setNetworkChanged(true);
		}
	}

	/**
	 * Checks whether the share of social network partners that currently apply
	 * the given innovation multiplied by the innocation's adoption factor is
	 * equal to or greater than a random number ]0,1[.
	 * 
	 * Checks whether this agent is in {@link InnovationStates#AWARE} mode and
	 * raises a warning otherwise.
	 * 
	 * @param innovation
	 */
	@Override
	public void considerTrial(Innovation innovation) {
		// TODO implement BOs
		if (innovations.get(innovation).getState() == InnovationStates.AWARE) {

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Probablilty to adopt: "
						+ innovations.get(innovation).getNeighbourShare()
						* innovation.getTrialThreshold(this)
						+ "(social network partner share: "
						+ innovations.get(innovation).getNeighbourShare() + ")");
			}
			// LOGGING ->

			if (innovations.get(innovation).getNeighbourShare()
					+ innovation.getTrialNoise() >= innovation
						.getTrialThreshold(this)) {

				this.makeTrial(innovation);
			}
		} else {
			// <- LOGGING
			logger.warn(this + "> considered trial, but the innovation >"
					+ innovation + "< is not in State AWARE!");
			// LOGGING ->
		}
	}

	/**
	 * Sets {@link InnovationStates#TRIAL}, performs the innovation and makes
	 * social network partners aware.
	 * 
	 * @param innovation
	 */
	@Override
	public void makeTrial(Innovation innovation) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this + "> trials " + innovation);
		}
		// LOGGING ->

		this.innovations.get(innovation).trial();
		innovation.perform(this);

		if (this.agent.getRegion().getNetwork() != null) {
			for (Agent n : this.agent.getRegion().getNetwork()
					.getSuccessors((SocialAgent) this.agent)) {
				if (n instanceof InnovativeBC
						&& innovation.getAffectedAFTs().contains(
								this.agent.getFC().getFR().getLabel())) {
					((InnovativeBC) n).makeAware(innovation);
				}
			}
		}
	}

	/**
	 * Checks whether this agent is in {@link InnovationStates#AWARE} or mode
	 * {@link InnovationStates#TRIAL} and raises a warning otherwise.
	 * 
	 * Adoption is steered by probability (applying
	 * {@link Innovation#getAdoptionThreshold(InnovativeBC)}.
	 * 
	 * 
	 * @see org.volante.abm.agent.bt.InnovativeBC#considerAdoption(org.volante.abm.institutions.innovation.Innovation)
	 */
	@Override
	public void considerAdoption(Innovation innovation) {
		if (innovations.get(innovation).getState() == InnovationStates.AWARE
				|| innovations.get(innovation).getState() == InnovationStates.TRIAL) {

			if (innovations.get(innovation).getNeighbourShare()
					+ innovation.getAdoptionNoise() >= innovation
						.getAdoptionThreshold(this)) {
				this.makeAdopted(innovation);
			}
		} else {
			// <- LOGGING
			logger.warn(this + "> considered adoption, but the innovation >"
					+ innovation + "< is not in State AWARE or TRIAL!");
			// LOGGING ->
		}
	}

	/**
	 * Sets {@link InnovationStates#ADOPTED} and increases adoption counter.
	 * 
	 * @param innovation
	 */
	@Override
	public void makeAdopted(Innovation innovation) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this + "> adopts " + innovation);
		}
		// LOGGING ->

		this.innovations.get(innovation).adopt();
		numberAdoptions++;
	}

	/**
	 * Does nothing
	 * 
	 * @param innovation
	 */
	@Override
	public void considerRejection(Innovation innovation) {
	}

	/**
	 * @param innovation
	 */
	@Override
	public void rejectInnovation(Innovation innovation) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this + "> rejects " + innovation);
		}
		// LOGGING ->

		this.innovations.get(innovation).reject();
		innovation.unperform(this);
	}

	/**
	 * @see org.volante.abm.agent.bt.InnovativeBC#removeInnovation(org.volante.abm.institutions.innovation.Innovation)
	 */
	@Override
	public void removeInnovation(Innovation innvoation) {
		this.innovations.remove(innvoation);
	}

	/**
	 * Unmodifiable set of innovations this agent is aware of.
	 * 
	 * @see org.volante.abm.agent.bt.InnovativeBC#getInnovationsAwareOf()
	 */
	@Override
	public Set<Innovation> getInnovationsAwareOf() {
		return Collections.unmodifiableSet(this.innovations.keySet());
	}

	/**
	 * @see org.volante.abm.agent.bt.InnovativeBC#getState(org.volante.abm.institutions.innovation.Innovation)
	 */
	@Override
	public InnovationState getState(Innovation innovation) {
		if (this.innovations.containsKey(innovation)) {
			return this.innovations.get(innovation).getState();
		} else {
			return InnovationStates.UNAWARE;
		}
	}

	/**
	 * @see org.volante.abm.agent.DefaultLandUseAgent#receiveNotification(de.cesr.more.basic.agent.MoreObservingNetworkAgent.NetworkObservation,
	 *      org.volante.abm.agent.Agent)
	 */
	@Override
	public void receiveNotification(NetworkObservation observation, Agent object) {
		if (observation instanceof AdoptionObservation) {
			Innovation innovation = ((AdoptionObservation) observation)
					.getInnovation();
			if (innovation.getAffectedAFTs().contains(
					this.agent.getFC().getFR().getLabel())) {
				this.makeAware(innovation);
			}
		} else {
			for (InnovationStatus istate : innovations.values()) {
				istate.setNetworkChanged(true);
			}
		}
	}

	/**
	 * Perceive social network regarding each innovation the agent is aware of.
	 * 
	 * @see org.volante.abm.agent.SocialAgent#perceiveSocialNetwork()
	 */
	@Override
	public void perceiveSocialNetwork() {
		if (!initialAdoptionObservationPerformed) {
			this.initialAdoptionObservation();
			this.initialAdoptionObservationPerformed = true;
		}
		for (Map.Entry<Innovation, InnovationStatus> entry : innovations
				.entrySet()) {
			if (entry.getValue().hasNetworkChanged()) {
				perceiveSocialNetwork(entry.getKey());
				entry.getValue().setNetworkChanged(false);
			}
		}
	}
	
	/**
	 * 
	 */
	protected void initialAdoptionObservation() {
		for (SocialAgent neighbour : this.agent.getRegion().getNetwork()
				.getPredecessors((SocialAgent) this.agent)) {
			if (neighbour.getBC() instanceof InnovativeBC) {
				for (Innovation i : ((InnovativeBC) neighbour.getBC())
						.getInnovationsAwareOf()) {
					if (i.getAffectedAFTs().contains(
							this.agent.getFC().getFR().getSerialID())) {
						this.makeAware(i);
					}
				}
			}
		}
	}

	/**
	 * Observe and set the share of social network partners that adopted the
	 * given {@link Innovation}. Considers only incoming relations.
	 * 
	 * @param i
	 *            innovation to consider
	 */
	protected void perceiveSocialNetwork(Innovation i) {
		double shareAdopters = 0.0;
		for (SocialAgent predecessor : this.agent.getRegion().getNetwork().getPredecessors((SocialAgent)this.agent)) {
			if (predecessor.getBC() instanceof InnovativeBC) {
				if (((InnovativeBC) predecessor.getBC()).getState(i) == InnovationStates.ADOPTED) {
					shareAdopters += 1.0;
				}
			}
		}
		innovations.get(i).setNeighbourShare(
				this.agent.getRegion().getNetwork().getInDegree((SocialAgent)this.agent) == 0 ? 0 : shareAdopters
								/ this.agent.getRegion().getNetwork()
										.getInDegree((SocialAgent) this.agent));
	}
}
