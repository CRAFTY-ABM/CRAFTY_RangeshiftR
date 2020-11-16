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


import java.util.ArrayList;
import java.util.List;

import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.decision.pa.CraftyPa;

import com.moseph.modelutils.curve.Curve;

import de.cesr.lara.components.decision.LaraDecider;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.decision.LaraDecisionModes;

/**
 * @author Sascha Holzhauer
 *
 */
public abstract class AbstractImitatingFrDecider implements
		LaraDecider<CraftyPa<?>> {

	protected LaraBehaviouralComponent agent;
	protected LaraDecisionConfiguration dConfig;

	protected CraftyPa<?> selectedPo = null;

	protected Curve diffFunction;

	public AbstractImitatingFrDecider(LaraBehaviouralComponent agent,
			LaraDecisionConfiguration dConfiguration, Curve diffFunction) {
		this.agent = agent;
		this.dConfig = dConfiguration;
		this.diffFunction = diffFunction;
	}


	/**
	 * @see de.cesr.lara.components.decision.LaraDecider#getKSelectedBos(int)
	 */
	@Override
	public List<CraftyPa<?>> getSelectedBos() {
		List<CraftyPa<?>> list = new ArrayList<CraftyPa<?>>();
		list.add(selectedPo);
		return list;
	}

	/**
	 * @see de.cesr.lara.components.decision.LaraDecider#getNumSelectableBOs()
	 */
	@Override
	public int getNumSelectableBOs() {
		return selectedPo == null ? 0 : 1;
	}

	/**
	 * @see de.cesr.lara.components.decision.LaraDecider#getSelectedBo()
	 */
	@Override
	public CraftyPa<?> getSelectedBo() {
		return selectedPo;
	}

	/**
	 * @see de.cesr.lara.components.decision.LaraDecider#setSelectedBos(java.util.List)
	 */
	@Override
	public void setSelectedBos(List<CraftyPa<?>> selectedBos) {
		this.selectedPo = selectedBos.get(1);
	}

	/**
	 * @see de.cesr.lara.components.decision.LaraDecider#getDecisionMode()
	 */
	@Override
	public LaraDecisionModes getDecisionMode() {
		return LaraDecisionModes.IMITATION;
	}
}
