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
import org.volante.abm.example.BasicTestsUtils;

import com.moseph.modelutils.fastdata.NamedIndexSet;


public class CSVCapitalLoaderTest extends BasicTestsUtils {
	@Test
	public void testBasicLoading() throws Exception {
		CSVCapitalLoader testCap = persister
				.readXML(CSVCapitalLoader.class, "xml/TestCapitals.xml", null);
		NamedIndexSet<Capital> caps = testCap.getDataTypes(persister);
		checkDataType(caps, "ECON", 3);
		checkDataType(caps, "SOC", 2);
		checkDataType(caps, "NAT", 1);
		checkDataType(caps, "INF", 0);
	}

}
