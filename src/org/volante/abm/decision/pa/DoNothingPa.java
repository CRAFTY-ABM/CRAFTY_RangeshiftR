/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2016 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 2 Sep 2016
 */
package org.volante.abm.decision.pa;


import java.util.Map;

import org.simpleframework.xml.Element;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.ModelData;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;

import de.cesr.lara.components.LaraBehaviouralOption;
import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.agents.LaraAgent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.toolbox.config.xml.LBoFactory;


/**
 * Used to out-perform another PA in case their situational utility is below a defined threshold. Requires the agent to
 * weight the preference THRESHOLD with 1.0.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class DoNothingPa extends CraftyPa<DoNothingPa> {

	static final String PREFNAME_PBC = "THRESHOLD";

	public static class DoNothingPaFactory extends LBoFactory implements GloballyInitialisable {

		@Element(required=true)
		protected double threshold;

		/**
		 * @see org.volante.abm.serialization.GloballyInitialisable#initialise(org.volante.abm.data.ModelData,
		 *      org.volante.abm.schedule.RunInfo)
		 */
		@Override
		public void initialise(ModelData data, RunInfo info) throws Exception {
		}

		public LaraBehaviouralOption<?, ?> assembleBo(LaraAgent<?, ?> lbc, Object modelId) {
			return new DoNothingPa(this.key, (LaraBehaviouralComponent) lbc, this.threshold);
		}
	}

	protected double threshold;

	/**
	 * @param key
	 * @param agent
	 * @param threshold
	 */
	public DoNothingPa(String key, LaraBehaviouralComponent agent, double threshold) {
		super(key, agent);
		this.threshold = threshold;
	}

	public DoNothingPa(String key, LaraBehaviouralComponent agent, Map<LaraPreference, Double> preferenceUtilities,
	        double threshold) {
		super(key, agent, preferenceUtilities);
		this.threshold = threshold;
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
	 *      java.util.Map)
	 */
	@Override
	public CraftyPa<DoNothingPa> getModifiedBO(LaraBehaviouralComponent agent,
	        Map<LaraPreference, Double> preferenceUtilities) {
		return new DoNothingPa(this.getKey(), this.getAgent(), preferenceUtilities, this.threshold);
	}

	// UNDO
	@Override
	public Map<LaraPreference, Double> getValue() {
		return super.getValue();
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getSituationalUtilities(de.cesr.lara.components.decision.LaraDecisionConfiguration)
	 */
	@Override
	public Map<LaraPreference, Double> getSituationalUtilities(LaraDecisionConfiguration dConfig) {
		Map<LaraPreference, Double> utilities = this.getModifiableUtilities();
		utilities.put(
this.getAgent().getLaraComp().getLaraModel().getPrefRegistry().get(PREFNAME_PBC), this.threshold);
		return utilities;
	}
}
