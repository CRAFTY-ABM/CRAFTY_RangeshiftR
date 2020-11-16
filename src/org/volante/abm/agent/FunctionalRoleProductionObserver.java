package org.volante.abm.agent;


import org.volante.abm.agent.fr.FunctionalRole;


public interface FunctionalRoleProductionObserver {
	
	/**
	 * To inform observers about changes in production.
	 * 
	 * @param fr
	 */
	public void functionalRoleProductionChanged(FunctionalRole fr);

}
