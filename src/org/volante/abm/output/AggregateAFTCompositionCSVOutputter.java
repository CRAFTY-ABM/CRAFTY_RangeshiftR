/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 */
package org.volante.abm.output;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.fr.InstitutionalFR;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.example.measures.LandUseProportionMeasure;
import org.volante.abm.schedule.RunInfo;


/**
 * Creates one column per AFT-ID (across regions, too).
 * 
 * @author Sascha Holzhauer
 * 
 */
public class AggregateAFTCompositionCSVOutputter extends AggregateCSVOutputter {

	Map<Region, Map<String, Double>>	aftData		= new HashMap<Region, Map<String, Double>>();

	protected boolean					initialised	= false;

	/**
	 * Output absolute numbers instead of proportions.
	 */
	@Attribute(name = "outputSums", required = false)
	protected boolean					outputSums	= false;

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "AggregateAFTComposition";
	}

	public void initAftColumns(Regions regions) {
		Set<String> pAgentSet = new LinkedHashSet<String>();
		for (Region r : regions.getAllRegions()) {
			for (FunctionalRole fr : r.getFunctionalRoleMapByLabel().values()) {
				if (!(fr instanceof InstitutionalFR || pAgentSet.contains(fr.getLabel()))) {
					pAgentSet.add(fr.getLabel());
				}
			}
			HashMap<String, Double> pMap = new HashMap<String, Double>();
			aftData.put(r, pMap);
		}

		for (String id : pAgentSet) {
			addColumn(new PotentialAgentColumn(id));
		}
	}

	/**
	 * Calculates the compositions of AFTs for every region.
	 * 
	 * @see org.volante.abm.output.TableOutputter#doOutput(org.volante.abm.data.Regions)
	 */
	@Override
	public void doOutput(Regions regions) {
		if (!this.initialised) {
			initAftColumns(regions);
			this.initialised = true;
		}

		for (Region r : regions.getAllRegions()) {
			ArrayList<Region> rlist = new ArrayList<>();
			rlist.add(r);
			aftData.put(r, LandUseProportionMeasure.getScore(rlist, outputSums));
		}
		super.doOutput(regions);
	}

	public class PotentialAgentColumn implements TableColumn<Region> {
		String	id;

		/**
		 * @param id
		 */
		public PotentialAgentColumn(String id) {
			this.id = id;
		}

		/**
		 * @see org.volante.abm.output.TableColumn#getHeader()
		 */
		@Override
		public String getHeader() {
			return "AFT:" + id;
		}

		/**
		 * @see org.volante.abm.output.TableColumn#getValue(java.lang.Object,
		 *      org.volante.abm.data.ModelData, org.volante.abm.schedule.RunInfo,
		 *      org.volante.abm.data.Regions)
		 */
		@Override
		public String getValue(Region r, ModelData data, RunInfo info, Regions rs) {
			if (!aftData.get(r).containsKey(id)) {
				return doubleFmt.format(Double.NaN);
			} else {
				return doubleFmt.format(aftData.get(r).get(id).doubleValue());
			}
		}
	}
}