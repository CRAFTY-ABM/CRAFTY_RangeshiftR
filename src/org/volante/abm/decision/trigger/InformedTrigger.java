/**
 * 
 */
package org.volante.abm.decision.trigger;


import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;


/**
 * Wrapper to store additional information when passed to {@link LaraBehaviouralComponent}, e.g., what condition
 * activated the trigger.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class InformedTrigger implements DecisionTrigger {

	DecisionTrigger wrappedTrigger = null;
	String information = "";

	public InformedTrigger(DecisionTrigger wrappedTrigger, String information) {
		this.wrappedTrigger = wrappedTrigger;
		this.information = information;
	}

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#getId()
	 */
	@Override
	public String getId() {
		return this.wrappedTrigger.getId();
	}

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#check(org.volante.abm.agent.Agent)
	 */
	@Override
	public boolean check(Agent agent) {
		return true;
	}

	public String toString() {
		return this.wrappedTrigger.getId() + "(" + this.information + ")";
	}
}
