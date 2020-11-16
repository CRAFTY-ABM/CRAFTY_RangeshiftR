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
 * Created by Sascha Holzhauer on 29 Jul 2015
 */
package org.volante.abm.institution;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.institutions.global.GlobalInstitution;
import org.volante.abm.institutions.global.GlobalInstitutionsRegistry;
import org.volante.abm.institutions.global.GlobalSubsidisingInstitution;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.serialization.PotentialActionInitialiser;
import org.volante.abm.serialization.ScenarioLoader;

/**
 * @author Sascha Holzhauer
 *
 */
public class GlobalSubsidyInstitutionTest {

	protected static final String SCENARIO_LOADER_XML_FILE = "xml/globalinstitutions/Scenario.xml";
	protected static final String FILENAME_PA_B = "./pas/GlobalSubsidisingInstPasB.xml";
	protected static final int NUM_DEFINED_INSTITUTIONS = 1;
	protected static final double SUBSIDIES_FACTOR = 2.0;

	public static RunInfo runInfo = new RunInfo();
	public static ModelData modelData = new ModelData();

	public ABMPersister persister = ABMPersister.getInstance();

	protected ScenarioLoader loader;
	protected GlobalSubsidisingInstitution globalInstitution;


	@Before
	public void setupBasicTestEnvironment() {
		persister.setBaseDir("test-data");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		runInfo = new RunInfo();
		modelData = new ModelData();

		GlobalInstitutionsRegistry.reset();

		loader = persister.readXML(ScenarioLoader.class, SCENARIO_LOADER_XML_FILE, null);
		loader.initialise(runInfo);
		runInfo.getSchedule().setRegions(loader.getRegions());

		for (GlobalInstitution institution : GlobalInstitutionsRegistry.getInstance().getGlobalInstitutions()) {
			if (institution instanceof GlobalSubsidisingInstitution) {
				this.globalInstitution = (GlobalSubsidisingInstitution) institution;
			}
		}
		this.globalInstitution
		        .initialise(loader.getRegions().getAllRegions().iterator().next().getModelData(), runInfo);
		runInfo.getSchedule().tick();
		runInfo.getSchedule().tick();
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.institutions.global.AbstractGlobalInstitution#initialise(org.volante.abm.schedule.RunInfo, org.volante.abm.data.ModelData, ScenarioLoader)}
	 * .
	 */
	@Test
	public void testInitialiseRunInfo() {
		assertEquals(NUM_DEFINED_INSTITUTIONS, GlobalInstitutionsRegistry.getInstance().getGlobalInstitutions().size());

		for (GlobalInstitution institution : GlobalInstitutionsRegistry.getInstance().getGlobalInstitutions()) {
			assertTrue(institution instanceof GlobalSubsidisingInstitution);

			for (Region region : loader.getRegions().getAllRegions()) {
				assertTrue(region.getInstitutions().hasInstitution(institution));
			}
		}
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.institutions.global.GlobalSubsidisingInstitution#adjustCompetitiveness(org.volante.abm.agent.fr.FunctionalRole, org.volante.abm.data.Cell, com.moseph.modelutils.fastdata.UnmodifiableNumberMap, double)}
	 * . TODO
	 */
	@Test
	@Ignore
	public void testAdjustCompetitiveness() {
		// check competition without subsidies (DO_NOTHING)
		for (Region region : loader.getRegions().getAllRegions()) {
			for (Cell cell : region.getCells()) {
				// supply * residual
				assertEquals(20.0 * 50.0, region.getCompetitiveness(cell), 0.0001);
			}
		}
		// add PA to subsidy:
		try {
			PotentialActionInitialiser.addPa((LaraBehaviouralComponent) this.globalInstitution.getBC(), FILENAME_PA_B,
			        loader.getRegions().getAllRegions().iterator().next().getModelData(), runInfo);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		runInfo.getSchedule().tick();

		// check competition with subsidies
		for (Region region : loader.getRegions().getAllRegions()) {
			for (Cell cell : region.getCells()) {
				// supply * residual * overallEffect * subsidies
				assertEquals(20.0 * 50.0 * 1 * SUBSIDIES_FACTOR, region.getCompetitiveness(cell), 0.0001);
			}
		}
	}
}
