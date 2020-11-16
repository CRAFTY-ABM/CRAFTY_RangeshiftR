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
 * Created by Sascha Holzhauer on 10 Jun 2016
 */
package org.volante.abm.output;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.example.GlobalBtRepository;
import org.volante.abm.schedule.PrePreTickAction;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;


/**
 * @author Sascha Holzhauer
 *
 */
public class GenericTableOutputter extends TableOutputter<Map<String, Object>> implements GloballyInitialisable,
        PrePreTickAction {

	protected static Map<String, GenericTableOutputter> outputters = new HashMap<>();

	public static boolean hasGenericTableOutputter(String id) {
		return outputters.containsKey(id);
	}
	public static GenericTableOutputter getGenericTabelOutputter(String id) {
		return outputters.get(id);
	}

	/**
	 * Adds actions of global institutions to regional files.
	 */
	@Attribute(required = false)
	boolean addGlobalActionsToRegions = true;

	/**
	 * Outputs actions of global institutions to a separate file. If both addGlobalActionsToRegions and
	 * outputGlobalActionsSeparately are true, global actions will be output both into region files and in a separate
	 * file.
	 */
	@Attribute(required = false)
	boolean outputGlobalActionsSeparately = false;

	@Attribute(required = true)
	protected String id = null;

	@ElementList(required = true, entry = "column", inline = true)
	protected Set<String> columns;

	@Attribute(required = false)
	boolean addTick = true;

	/**
	 * Only considered when <code>perRegion==false</code> (otherwise, regions are considered in column headers).
	 */
	@Attribute(required = false)
	boolean addRegion = true;

	protected Map<Region, Set<Map<String, Object>>> dataSet = new HashMap<>();

	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		info.getSchedule().register(this);
		outputters.put(this.id, this);
	}

	public void setOutputManager(Outputs outputs) {
		super.setOutputManager(outputs);

		if (addTick) {
			addColumn(new TickColumn<Map<String, Object>>());
		}

		if (addRegion && perRegion) {
			addColumn(new RegionsColumn<Map<String, Object>>());
		}

		for (String column : this.columns) {
			addColumn(new GenericColumn(column));
		}
	}

	public class GenericColumn implements TableColumn<Map<String, Object>> {

		String columnname = "";

		public GenericColumn(String columnname) {
			this.columnname = columnname;
		}

		@Override
		public String getHeader() {
			return this.columnname;
		}

		@Override
		public String getValue(Map<String, Object> map, ModelData data, RunInfo info, Regions rs) {
			if (!map.containsKey(this.columnname)) {
				log.warn("The map does not contain an entry for key '" + this.columnname + "'!");
			}
			return map.get(this.columnname).toString();
		}
	}

	public Iterable<Map<String, Object>> getData(Regions r) {
		Set<Map<String, Object>> tes = new HashSet<>();
		for (Region region : r.getAllRegions()) {
			if (this.dataSet.containsKey(region)) {
				tes.addAll(this.dataSet.get(region));
			}
		}
		if (addGlobalActionsToRegions) {
			if (this.dataSet.containsKey(GlobalBtRepository.getInstance().getPseudoRegion())) {
				tes.addAll(this.dataSet.get(GlobalBtRepository.getInstance().getPseudoRegion()));
			}
		}
		return tes;
	}

	public void setData(Map<String, Object> datamap, Region region) {
		if (!this.dataSet.containsKey(region)) {
			this.dataSet.put(region, new HashSet<Map<String, Object>>());
		}
		this.dataSet.get(region).add(datamap);
	}

	@Override
	public void doOutput(Regions regions) {
		if (perRegion) {
			for (Region r : regions.getAllRegions()) {
				writeFile(r);
			}
			if (this.outputGlobalActionsSeparately) {
				writeFile(GlobalBtRepository.getInstance().getPseudoRegion());
			}
		} else {
			writeFile(regions);
		}
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "GenericTableOutputter-" + this.id;
	}

	/**
	 * @see org.volante.abm.schedule.PrePreTickAction#prePreTick()
	 */
	@Override
	public void prePreTick() {
		for (Region region : this.dataSet.keySet()) {
			this.dataSet.get(region).clear();
		}
	}
}
