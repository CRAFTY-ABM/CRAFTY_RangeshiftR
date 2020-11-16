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
 * Created by Sascha Holzhauer on 04.03.2014
 */
package org.volante.abm.serialization;


import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.testutils.CraftyTestUtils;

import de.cesr.more.param.MMilieuNetworkParameterMap;
import de.cesr.more.param.MNetBuildWbSwPa;
import de.cesr.more.param.MNetworkBuildingPa;
import de.cesr.more.param.MRandomPa;
import de.cesr.parma.core.PmParameterManager;


/**
 * 
 * @author Sascha Holzhauer
 * 
 */
public class SocialNetworkLoaderTest extends BasicTestsUtils {

	public static final String	SOCIAL_NETWORK_XML_FILE	= "xml/SociaNetwork.xml";
	Region						region;
	SocialNetworkLoader			loader					= null;

	@Before
	public void setUp() throws Exception {
		CraftyTestUtils.initMoreTestEnvironment();
		RegionLoader rloader = ABMPersister.getInstance().readXML(RegionLoader.class,
 "xml/SmallWorldRegion1.xml", null);
		rloader.initialise(new RunInfo());
		region = rloader.region;
	}

	@After
	public void tearDown() throws Exception {
		region = null;
	}

	@Test
	public void testInitialisation() {
		try {
			loader = ABMPersister.getInstance().readXML(
					SocialNetworkLoaderList.class, SOCIAL_NETWORK_XML_FILE,
					region.getPersisterContextExtra()).loaders.get(0);
			loader.initialise(region.getModelData(), new RunInfo(), region);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		
		PmParameterManager pm = loader.getPm();
		MMilieuNetworkParameterMap pmap = (MMilieuNetworkParameterMap) pm
				.getParam(MNetworkBuildingPa.MILIEU_NETWORK_PARAMS);
		assertEquals(10, pmap.getMilieuParam(MNetBuildWbSwPa.K, 1));
		assertEquals(5, pmap.getMilieuParam(MNetBuildWbSwPa.K, 2));
		assertEquals(4, pmap.getMilieuParam(MNetBuildWbSwPa.K, 3));
		assertEquals(8, pmap.getMilieuParam(MNetBuildWbSwPa.K, 4));
		
		assertEquals(42, ((Integer) pm.getParam(MRandomPa.RANDOM_SEED_NETWORK_BUILDING)).intValue());

		assertEquals("de.cesr.more.building.network.MWattsBetaSwNetworkService",
				loader.getNetworkGeneratorClass());
	}

}
