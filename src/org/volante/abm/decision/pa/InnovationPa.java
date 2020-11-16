/**
 * 
 */
package org.volante.abm.decision.pa;


import java.util.Map;

import org.volante.abm.agent.bt.LaraBehaviouralComponent;

import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;

/**
 * 
 * @author Sascha Holzhauer
 * 
 */
public class InnovationPa extends
 CraftyPa<InnovationPa> {

	/**
	 * @param key
	 * @param comp
	 * @param preferenceUtilities
	 */
	public InnovationPa(String key, LaraBehaviouralComponent comp,
			Map<LaraPreference, Double> preferenceUtilities) {
		super(key, comp, preferenceUtilities);
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
	 *      java.util.Map)
	 */
	@Override
	public InnovationPa getModifiedBO(LaraBehaviouralComponent comp,
			Map<LaraPreference, Double> preferenceUtilities) {
		return new InnovationPa(this.getKey(), comp, preferenceUtilities);
	}

	@Override
	public Map<LaraPreference, Double> getSituationalUtilities(
			LaraDecisionConfiguration dBuilder) {
		return super.getModifiableUtilities();
	}
}
