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
package org.volante.abm.serialization;


import org.junit.Test;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Service;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.NamedIndexSet;


public class ScenarioLoadingDataTypesTest extends BasicTestsUtils {

	@Test
	public void test() throws Exception {
		ScenarioLoader loader = persister.readXML(ScenarioLoader.class,
				"xml/datatype-test-scenario.xml", null);
		loader.initialise(new RunInfo());
		NamedIndexSet<Capital> caps = loader.modelData.capitals;
		checkDataType(caps, "ECON", 3);
		checkDataType(caps, "SOC", 2);
		checkDataType(caps, "NAT", 1);
		checkDataType(caps, "INF", 0);

		NamedIndexSet<Service> services = loader.modelData.services;
		checkDataType(services, "FOOD", 3);
		checkDataType(services, "NAT", 2);
		checkDataType(services, "TIMBER", 1);
		checkDataType(services, "INF", 0);
	}
}
