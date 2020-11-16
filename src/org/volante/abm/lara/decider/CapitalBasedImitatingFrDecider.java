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
 * Created by Sascha Holzhauer on 26 May 2015
 */
package org.volante.abm.lara.decider;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.pa.FrSelectionPa;

import com.moseph.modelutils.curve.Curve;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.decision.impl.LDeliberativeDecider;

/**
 * Applies difference function to absolute difference between own and adjacent
 * cell's base capitals.
 * 
 * Note: This decider is designed as a simple heuristic which goes around PO
 * collection, POselection, and PO updating (it creates POs on demand). If more
 * control is needed, a {@link LDeliberativeDecider} should be considered, with
 * assigning similarities to PO utilities.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CapitalBasedImitatingFrDecider extends AbstractImitatingFrDecider {

	class CellValueComparator implements Comparator<Cell> {

		Map<Cell, Double> base;

		public CellValueComparator(Map<Cell, Double> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with
		// equals.
		public int compare(Cell a, Cell b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			} // returning 0 would merge keys
		}
	}

	/**
	 * @param agent
	 * @param dConfiguration
	 * @param diffFunction
	 */
	public CapitalBasedImitatingFrDecider(LaraBehaviouralComponent agent,
			LaraDecisionConfiguration dConfiguration, Curve diffFunction) {
		super(agent, dConfiguration, diffFunction);
	}

	/**
	 * @see de.cesr.lara.components.decision.LaraDecider#decide()
	 */
	@Override
	public void decide() {
		// get neighbouring cells (delegate to Cell)
		Set<Cell> adjacent = this.agent.getAgent().getRegion()
				.getAdjacentCells(this.agent.getAgent().getHomeCell());

		HashMap<Cell, Double> adjacentmap = new HashMap<Cell, Double>();
		CellValueComparator cvc = new CellValueComparator(adjacentmap);

		for (Cell c : adjacent) {
			// filter managed cells
			if (!(c.getOwner() == Agent.NOT_MANAGED)) {
				// apply difference function on differences in capital levels
				// for each
				// capital
				double difference = 0;
				for (Capital cap : this.agent.getAgent().getRegion()
						.getModelData().capitals) {
					difference += diffFunction.sample(Math.abs(c
							.getBaseCapitals().getDouble(cap)
							- this.agent.getAgent().getHomeCell()
									.getBaseCapitals().getDouble(cap)));
				}
				adjacentmap.put(c, difference);
			}
		}

		TreeMap<Cell, Double> sorted_map = new TreeMap<Cell, Double>(cvc);
		sorted_map.putAll(adjacentmap);

		// assign the cell's FR with least difference sum to selectedPo
		this.selectedPo = new FrSelectionPa(sorted_map.lastKey()
				.getOwnersFrLabel(), agent);
	}

	/**
	 * @see de.cesr.lara.components.decision.LaraDecider#getSelectableBos()
	 */
	@Override
	public Collection<CraftyPa<?>> getSelectableBos() {
		ArrayList<CraftyPa<?>> list = new ArrayList<>();
		list.add(this.selectedPo);
		return list;
	}
}
