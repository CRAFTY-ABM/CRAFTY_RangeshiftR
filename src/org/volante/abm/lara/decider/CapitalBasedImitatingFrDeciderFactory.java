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
 * Created by Sascha Holzhauer on 13 Mar 2015
 */
package org.volante.abm.lara.decider;

import org.simpleframework.xml.Element;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.decision.pa.CraftyPa;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.curve.Identity;

import de.cesr.lara.components.decision.LaraDecider;
import de.cesr.lara.components.decision.LaraDeciderFactory;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;

/**
 * @author Sascha Holzhauer
 *
 */
public class CapitalBasedImitatingFrDeciderFactory implements
		LaraDeciderFactory<LaraBehaviouralComponent, CraftyPa<?>> {


	@Element(name = "diffFunction", required = false)
	protected Curve diffFunction = new Identity();

	public CapitalBasedImitatingFrDeciderFactory() {
	}


	/**
	 * @see de.cesr.lara.components.decision.LaraDeciderFactory#getDecider(de.cesr.lara.components.agents.LaraAgent,
	 *      de.cesr.lara.components.decision.LaraDecisionConfiguration)
	 */
	@Override
	public LaraDecider<CraftyPa<?>> getDecider(LaraBehaviouralComponent agent,
			LaraDecisionConfiguration dConfiguration) {
		return new CapitalBasedImitatingFrDecider(agent, dConfiguration,
				diffFunction);
	}
}
