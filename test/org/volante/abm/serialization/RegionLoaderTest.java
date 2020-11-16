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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.volante.abm.agent.fr.DefaultFR;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.DefaultFrTest;
import org.volante.abm.schedule.RunInfo;

public class RegionLoaderTest extends BasicTestsUtils
{

	@SuppressWarnings("deprecation")
	@Test
	public void testReadingCSV() throws Exception
	{
		RegionLoader loader = ABMPersister.getInstance().readXML(RegionLoader.class,
				"xml/SmallWorldRegion1.xml", null);
		loader.initialise( new RunInfo() );
		Region region = loader.region;
		region.initialise(modelData, new RunInfo(), region);
		
		assertEquals( 10, loader.cellTable.size() );
		assertNotNull( loader.demand );
		assertNotNull( loader.allocation );
		assertNotNull( loader.competition );
		assertNotNull( region.getDemandModel() );
		assertNotNull( region.getCompetitionModel() );
		assertNotNull( region.getAllocationModel() );
		
		assertEqualMaps( loader.cellTable.get( 1, 1 ).getEffectiveCapitals(), capitals( 1, 0, 0.5, 0.5, 0, 1, 0.1 ) );
		assertEqualMaps( loader.cellTable.get( 1, 2 ).getEffectiveCapitals(), capitals( 1, 0, 0.5, 0.5, 0, 1, 0.2 ) );
		assertEqualMaps( loader.cellTable.get( 1, 3 ).getEffectiveCapitals(), capitals( 1, 0, 0.5, 0.5, 0, 1, 0.3 ) );
		assertEqualMaps( loader.cellTable.get( 2, 1 ).getEffectiveCapitals(), capitals( 1, 0, 0.5, 0.5, 0, 1, 0.4 ) );
		assertEqualMaps( loader.cellTable.get( 2, 2 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 0, 1, 0.5 ) );
		assertEqualMaps( loader.cellTable.get( 2, 3 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 0, 1, 0.6 ) );
		assertEqualMaps( loader.cellTable.get( 3, 1 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 0, 1, 0.7 ) );
		assertEqualMaps( loader.cellTable.get( 3, 2 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 1, 0, 0.8 ) );
		assertEqualMaps( loader.cellTable.get( 3, 3 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 1, 0, 0.9 ) );
		assertEqualMaps( loader.cellTable.get( 4, 2 ).getEffectiveCapitals(), capitals( 1, 0, 1, 0.5, 1, 0, 1 ) );
		
		FunctionalRole ag = loader.getRegion().getFunctionalRoleMapByLabel()
				.get("LowIntensityArable");
		assertNotNull( ag );
		DefaultFrTest.testLowIntensityArableAgent((DefaultFR) loader
				.getRegion().getFunctionalRoleMapByLabel()
				.get("LowIntensityArable"));
		DefaultFrTest
				.testHighIntensityArableAgent((DefaultFR) loader.getRegion()
						.getFunctionalRoleMapByLabel()
						.get("HighIntensityArable"));
		DefaultFrTest.testCommercialForestryAgent((DefaultFR) loader
				.getRegion().getFunctionalRoleMapByLabel()
				.get("CommercialForestry"));
	}
}
