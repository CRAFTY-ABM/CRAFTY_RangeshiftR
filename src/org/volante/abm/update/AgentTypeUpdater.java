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
 * 
 */
package org.volante.abm.update;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.serialization.Initialisable;

import com.csvreader.CsvReader;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Updates the capitals on a cell using a function for each agent
 * 
 * @author dmrust
 * 
 */
public class AgentTypeUpdater extends AbstractUpdater {
	Multimap<FunctionalRole, CapitalUpdateFunction> functions = HashMultimap
			.create();

	@ElementMap(inline = true, required = false, attribute = true, key = "agent", entry = "agentUpdate", value = "function")
	Map<String, CapitalUpdateFunction> serialFunctions = new LinkedHashMap<String, AgentTypeUpdater.CapitalUpdateFunction>();

	/**
	 * Points to a csv file with capitals along the top and agents down the side
	 * First column should be the same as the "agentColumn" attribute, defaults
	 * to "Agent"
	 */
	@ElementList(required = false, inline = true, entry = "csvFile")
	ArrayList<String> csvFiles = new ArrayList<String>();

	@Attribute(required = false)
	String agentColumn = "Agent";

	// Used internally to get agents by name
	Map<String, FunctionalRole> fRoles = new LinkedHashMap<String, FunctionalRole>();

	@Override
	public void prePreTick() {
		for (Cell cell : region.getAllCells()) {
			for (CapitalUpdateFunction f : functions.get(cell.getOwner()
					.getFC().getFR())) {
				f.apply(cell);
			}
		}
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent)
			throws Exception {
		super.initialise(data, info, extent);

		fRoles = extent.getFunctionalRoleMapByLabel();

		// Load in the serialised stuff
		for (Entry<String, CapitalUpdateFunction> e : serialFunctions
				.entrySet()) {
			if (fRoles.containsKey(e.getKey())) {
				functions.put(fRoles.get(e.getKey()), e.getValue());
			}
		}

		// Read in csv files if we have any
		for (String file : csvFiles) {
			readCSVFile(file);
		}

		// And init the functions in case they need it
		for (CapitalUpdateFunction c : functions.values()) {
			c.initialise(data, info, extent);
		}
	}

	public void readCSVFile(String CSVFile) throws Exception {
		ABMPersister pers = info.getPersister();
		if (CSVFile != null
				&& pers.csvFileOK(getClass(), CSVFile,
						region.getPersisterContextExtra(), agentColumn)) {
			CsvReader reader = pers.getCSVReader(CSVFile,
					region.getPersisterContextExtra());
			while (reader.readRecord()) {
				String agent = reader.get(agentColumn);
				if (agent != null && agent != "" && fRoles.containsKey(agent)) {
					FunctionalRole ag = fRoles.get(agent);
					for (Capital c : data.capitals) {
						String val = reader.get(c.getName());
						if (val != null && val != "") {
							functions.put(ag,
									getCSVFunction(c, Double.parseDouble(val)));
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a new function for the value from a csv file. Defaults to
	 * proportional change functions. Override to use a different kind of
	 * function
	 * 
	 * @param c
	 * @param value
	 * @return
	 */
	public CapitalUpdateFunction getCSVFunction(Capital c, double value) {
		return new ProportionalChangeFunction(c, value);
	}

	/**
	 * A function which updates the level of capital in the given cell
	 * 
	 * @author dmrust
	 * 
	 */
	public static interface CapitalUpdateFunction extends Initialisable {
		public void apply(Cell c);
	}

}
