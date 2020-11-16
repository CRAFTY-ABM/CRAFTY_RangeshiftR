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
 * Created by Sascha Holzhauer on 10 Mar 2015
 */
package org.volante.abm.agent.bt;


import java.util.Set;

import org.volante.abm.agent.Agent;
import org.volante.abm.data.Region;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.serialization.Initialisable;

/**
 * @author Sascha Holzhauer
 *
 */
public interface BehaviouralType extends Initialisable {

	public static final int UNKNOWN_SERIAL = -1;

	public Agent assignNewBehaviouralComp(Agent agent);

	public String getLabel();

	public int getSerialID();

	public Region getRegion();

	public void addDecisionTrigger(DecisionTrigger trigger);

	public boolean removeDecisionTrigger(String id);

	public Set<DecisionTrigger> getDecisionTriggers();

	public boolean isInitialised();
}
