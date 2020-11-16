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
 * Created by Sascha Holzhauer on 19 Mar 2015
 */
package org.volante.abm.agent.bt;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DecisionTriggerPrecheckingAgent;
import org.volante.abm.agent.property.DoublePropertyProvider;
import org.volante.abm.agent.property.DoublePropertyProviderComp;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.decision.trigger.DecisionTrigger;

/**
 * @author Sascha Holzhauer
 *
 */
public abstract class AbstractBC implements BehaviouralComponent {

	protected BehaviouralType bType;

	protected DoublePropertyProvider propertyProvider;

	public AbstractBC(BehaviouralType bType) {
		this.bType = bType;
		this.propertyProvider = new DoublePropertyProviderComp();
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#isProvided(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public boolean isProvided(PropertyId property) {
		return this.propertyProvider.isProvided(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#getProperty(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public Double getProperty(PropertyId property) {
		return this.propertyProvider.getProperty(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#setProperty(org.volante.abm.agent.property.PropertyId,
	 *      java.lang.Double)
	 */
	@Override
	public void setProperty(PropertyId propertyId, Double value) {
		this.propertyProvider.setProperty(propertyId, value);
	}

	/**
	 * @see org.volante.abm.agent.bt.BehaviouralComponent#getType()
	 */
	@Override
	public BehaviouralType getType() {
		return this.bType;
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#getProperty(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public Object getObjectProperty(PropertyId property) {
		return this.propertyProvider.getObjectProperty(property);
	}


		/**
	 * @see org.volante.abm.agent.property.PropertyProvider#setObjectProperty(org.volante.abm.agent.property.PropertyId,
	 *      java.lang.Object)
	 */
	@Override
	public void setObjectProperty(PropertyId propertyId, Object value) {
		this.propertyProvider.setObjectProperty(propertyId, value);
	}

	/**
	 * @see org.volante.abm.agent.bt.BehaviouralComponent#triggerDecisions(Agent)
	 */
	@Override
	public void triggerDecisions(Agent agent) {
		if (agent instanceof DecisionTriggerPrecheckingAgent) {
			for (DecisionTrigger trigger : ((DecisionTriggerPrecheckingAgent) agent).preCheckDecisionTriggers(this
			        .getType().getDecisionTriggers())) {
				trigger.check(agent);
			}
		} else {
			for (DecisionTrigger trigger : this.getType().getDecisionTriggers()) {
				trigger.check(agent);
			}			
		}
	}
}
