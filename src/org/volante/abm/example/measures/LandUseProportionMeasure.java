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
 * Created by Sascha Holzhauer on 19 Jul 2016
 */
package org.volante.abm.example.measures;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.fr.InstitutionalFR;
import org.volante.abm.data.Region;


/**
 * @author Sascha Holzhauer
 *
 */
public class LandUseProportionMeasure {

	/**
	 * TODO cache data
	 * 
	 * @param regions
	 * @param outputSums
	 * @return map
	 */
	public static Map<String, Double> getScore(Iterable<Region> regions, boolean outputSums) {

		Map<String, Double> aftData = new HashMap<>();

		for (Region r : regions) {

			int[] pagentNumbers = new int[r.getFunctionalRoles().size()];
			for (Agent a : r.getAgents()) {
				if (a.getFC().getFR().getSerialID() >= 0) {
					pagentNumbers[a.getFC().getFR().getSerialID()]++;
				}
			}

			int sum = 0;
			if (outputSums) {
				sum = 1;
			} else {
				for (int i = 0; i < pagentNumbers.length; i++) {
					sum += pagentNumbers[i];
				}
			}
			for (FunctionalRole fr : r.getFunctionalRoles()) {
				if (!(fr instanceof InstitutionalFR)) {
					aftData.put(fr.getLabel(), new Double((double) pagentNumbers[fr.getSerialID()] / sum));
				}
			}
		}
		return aftData;
	}

	/**
	 * TODO cache data
	 * 
	 * @param regions
	 * @param fRoles
	 * @param outputSums
	 * @return map
	 */
	public static double getScore(Iterable<Region> regions, Set<FunctionalRole> fRoles, boolean outputSums) {

		int pagentNumbers = 0;
		int sum = 0;

		for (Region r : regions) {
			for (Agent a : r.getAgents()) {
				sum++;
				if (fRoles.contains(a.getFC().getFR())) {
					pagentNumbers++;
				}
			}
		}
		return (double) pagentNumbers / sum;
	}
}
