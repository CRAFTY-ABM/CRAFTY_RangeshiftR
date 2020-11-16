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
 * Created by Sascha Holzhauer on 3 Jun 2015
 */
package org.volante.abm.decision.trigger;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.eventbus.impl.LEventbus;

/**
 * @author Sascha Holzhauer
 *
 */
public abstract class AbstractDecisionTrigger implements DecisionTrigger {

	/**
	 * Defines the {@link LaraDecisionConfiguration} this trigger may trigger
	 * (needed to register the {@link LaraBehaviouralComponent} at the
	 * {@link LEventbus}).
	 */
	@Element(required = true)
	protected String dcId = null;

	@Attribute(required = true)
	protected String id = null;

	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#getId()
	 */
	@Override
	public String getId() {
		return this.id;
	}
}
