/**
 * 
 */
package org.volante.abm.decision.trigger;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.ElementList;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.example.measures.ConnectivityMeasure;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.model.impl.LModel;


/**
 * NOTE: There can be only one {@link ConnectivityTrigger} per institution (as multiple would set the same property
 * ConnectivityTriggerIds.LAST_RECORD_TICK and)!
 * 
 * @author Sascha Holzhauer
 */
public class ConnectivityTrigger extends AbstractDecisionTrigger {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(ConnectivityTrigger.class);

	public enum AgentProperty implements PropertyId {
		CONNECTIVITY_THRESHOLD;
	}

	enum ConnectivityTriggerIds implements PropertyId {
		LAST_RECORD_TICK;
	}

	@ElementList(inline = true, required = false, entry = "similarFrLabel", empty = false)
	public List<String> serialFrLabels = new ArrayList<>();

	protected Set<FunctionalRole> fRoles = null;

	/**
	 * @param agent
	 * @return connectivity
	 */
	protected double getCurrentConnectivity(Agent agent) {
		if (this.fRoles == null) {
			this.fRoles = new HashSet<>();
			for (String frLabel : this.serialFrLabels) {
				this.fRoles.add(agent.getRegion().getFunctionalRoleMapByLabel().get(frLabel));
			}
		}
		return ConnectivityMeasure.getScore(agent.getRegion(), this.fRoles);
	}

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#check(org.volante.abm.agent.Agent)
	 */
	@Override
	public boolean check(Agent agent) {
		double currentConnectivity = getCurrentConnectivity(agent);
		boolean triggered = false;

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(agent
			        + "> Checking Connectivity ("
			        + agent.getRegion()
			        + ")"
			        + (agent.isProvided(AgentProperty.CONNECTIVITY_THRESHOLD) ? "(current: "
			                + currentConnectivity + " - target: "
 + agent.getProperty(AgentProperty.CONNECTIVITY_THRESHOLD) + ") "
			                : "(not CONNECTIVITY_THRESHOLD available)") + "...");
		}
		// LOGGING ->

		if (agent.isProvided(AgentProperty.CONNECTIVITY_THRESHOLD)
		        && agent.isProvided(ConnectivityTriggerIds.LAST_RECORD_TICK)
		        && agent.getRegion().getRinfo().getSchedule().getCurrentTick() > agent
		                .getProperty(ConnectivityTriggerIds.LAST_RECORD_TICK)) {
			if (agent.getProperty(AgentProperty.CONNECTIVITY_THRESHOLD) > currentConnectivity) {
				LaraDecisionConfiguration dConfig =
				        LModel.getModel(agent.getRegion()).getDecisionConfigRegistry().get(this.dcId);

				((LaraBehaviouralComponent) agent.getBC()).subscribeOnce(
				        dConfig,
				        new InformedTrigger(this, currentConnectivity + "["
 + agent.getProperty(AgentProperty.CONNECTIVITY_THRESHOLD)
				                + "]"));
				triggered = true;

				// <- LOGGING
				logger.info(agent + "> Triggered regional institutional action (current connectivity: "
				        + currentConnectivity + " - target: "
 + agent.getProperty(AgentProperty.CONNECTIVITY_THRESHOLD)
				        + ")");
				// LOGGING ->
			}
		}
		agent.setProperty(ConnectivityTriggerIds.LAST_RECORD_TICK, (double) agent.getRegion().getRinfo().getSchedule()
		        .getCurrentTick());

		return triggered;
	}


	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ConnectivityDT";
	}
}
