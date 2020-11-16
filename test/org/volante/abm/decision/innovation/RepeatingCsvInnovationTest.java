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
 * Created by Sascha Holzhauer on 2 Dec 2014
 */
package org.volante.abm.decision.innovation;


import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultSocialLandUseAgent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.agent.bt.InnovativeCognitiveBC;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.RepeatingInnovativeInstitution;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.RepeatingProductivityInnovation;
import org.volante.abm.institutions.recruit.InstitutionTargetRecruitment;
import org.volante.abm.models.utils.ProductionWeightReporter;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class RepeatingCsvInnovationTest extends InnovationTestUtils {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(RepeatingCsvInnovationTest.class);

	public final String INNOVATION_ID_CSV = "RepeatingTestInnovationCSV";
	public final String REPEATING_CSV_INNOVATION_XML_FILE = "xml/RepeatingInnovationInstitutionCsv.xml";
	public final String REPEATING_INNOVATION_CSV_FACTOR_FILE = "csv/CsvProductivityInnovationRepCompTestfile.csv";

	public final double INNOVATION_EFFECT_ON_PRODUCTIVITY = 2.0;
	public final double[] INNOVATION_EFFECT_CSV_FACTORS = { 1.0, 1.2, 1.5, 2.0 };

	protected RepeatingInnovativeInstitution csvInstitution = null;

	protected RepeatingProductivityInnovation currentCsvInnovation = null;

	public boolean indicator = false;



	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		// <- LOGGING
		logger.info("START UP RepeatingCsvInnvationTest");
		// LOGGING ->

		// init institution
		persister = runInfo.getPersister();
		try {
			this.csvInstitution = persister.read(
					RepeatingInnovativeInstitution.class,
 persister
							.getFullPath(REPEATING_CSV_INNOVATION_XML_FILE,
									this.r1.getPersisterContextExtra()));
			this.csvInstitution.initialise(modelData, runInfo, r1);
			registerInstitution(this.csvInstitution, this.r1);

			// initialise innovation...
			BasicTestsUtils.runInfo.getSchedule().tick();

			this.currentCsvInnovation = (RepeatingProductivityInnovation) r1
					.getInnovationRegistry()
					.getInnovation(INNOVATION_ID_CSV);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		r1.getInnovationRegistry().reset();
	}


	@Test
	public void testCsvFactorBaseRelativeRenewal() {
		((CsvProductivityInnovationRepTestComp) this.currentCsvInnovation.getRepetitionComp())
				.setRelativeToPreviousTick(true);

		Service service = BasicTestsUtils.modelData.services.forName("FOOD");

		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
 innovativeFarming.getLabel());
		LandUseAgent two = this.agentAssemblerR1.assembleAgent(null, "Innovator",
 innovativeFarming.getLabel());
		LandUseAgent three = this.agentAssemblerR1.assembleAgent(null, "Innovator",
 innovativeFarming.getLabel());

		double initialProductivity = ((ProductionWeightReporter) one.getProductionModel()).
				getProductionWeights().getDouble(service);

		// Tick 1
		adoptAndCheckCsv(one, 0, true, service, initialProductivity);
		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 2
		adoptAndCheckCsv(two, 1, true, service, initialProductivity);
		BasicTestsUtils.runInfo.getSchedule().tick();
		
		// Tick 3
		adoptAndCheckCsv(three, 2, true, service, initialProductivity);
				
		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 4
		adoptAndCheckCsv(three, 3, true, service, initialProductivity);
	}

	@Test
	public void testCsvFactor() {
		((CsvProductivityInnovationRepTestComp) this.currentCsvInnovation.getRepetitionComp())
				.setRelativeToPreviousTick(false);

		Service service = BasicTestsUtils.modelData.services.forName("FOOD");

		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
 innovativeFarming.getLabel(), "One");

		LandUseAgent two = this.agentAssemblerR1.assembleAgent(null, "Innovator",
 innovativeFarming.getLabel(), "Two");

		LandUseAgent three =
				this.agentAssemblerR1.assembleAgent(null, "Innovator",
 innovativeFarming.getLabel(), "Three");


		double initialProductivity = ((ProductionWeightReporter) two
				.getProductionModel()).
				getProductionWeights().getDouble(service);


		// Tick 1
		adoptAndCheckCsv(one, 0, false, service, initialProductivity);

		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 2
		adoptAndCheckCsv(two, 1, false, service, initialProductivity);

		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 3
		adoptAndCheckCsv(three, 2, false, service, initialProductivity);

		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 4
		adoptAndCheckCsv(three, 3, false, service, initialProductivity);
	}

	protected void adoptAndCheckCsv(final LandUseAgent agent, int ticks,
			boolean relToPreviousTick,
			Service service,
			double initialProductivity) {

		// double expectedValue = initialProductivity;
		//
		//
		// for (int i = 1; i <= ticks; i++) {
		//
		// if (relToBase) {
		// expectedValue *= INNOVATION_EFFECT_ON_PRODUCTIVITY
		// * INNOVATION_EFFECT_CSV_FACTORS[ticks];
		// } else {
		// expectedValue *= INNOVATION_EFFECT_ON_PRODUCTIVITY
		// * INNOVATION_EFFECT_CSV_FACTORS[i];
		// }
		// }

		double expectedValue = initialProductivity;
		double effect = INNOVATION_EFFECT_ON_PRODUCTIVITY;

		if (relToPreviousTick) {
			for (int i = 1; i <= ticks; i++) {
				effect = effect * INNOVATION_EFFECT_CSV_FACTORS[i];
				expectedValue = expectedValue * effect;
			}

		} else {
			for (int i = 1; i <= ticks; i++) {
				effect = INNOVATION_EFFECT_ON_PRODUCTIVITY
						* INNOVATION_EFFECT_CSV_FACTORS[i];
				expectedValue = expectedValue * effect;
			}
		}

		checkCapitalChange(agent, InnovationTestUtils.innovativeFarming,
				expectedValue, service);
	}

	@Test
	public void testInnovationRenewal() {
		final Agent agent = new DefaultSocialLandUseAgent(innovativeFarming, "ID",
				modelData, r1, farmingProduction.copyWithNoise(modelData, null,
						null), 0.5, 0.5) {
		};

		InnovativeBC ibc = new InnovativeCognitiveBC(innovationTestBT, agent) {
			public void makeAware(Innovation innovation) {
				super.makeAware(innovation);
				indicator = true;
			}
		};
		agent.setBC(ibc);

		this.csvInstitution
				.setInstitutionTargetRecruitment(new InstitutionTargetRecruitment() {

					@Override
					public Collection<InnovativeBC> getRecruitedAgents(
							Collection<? extends InnovativeBC> allAgents) {
						Collection<InnovativeBC> agents = new ArrayList<InnovativeBC>();
						agents.add((InnovativeBC) agent.getBC());
						return agents;
					}

					@Override
					public void initialise(ModelData data, RunInfo info,
							Region extent) throws Exception {
					}
				});
		;
		BasicTestsUtils.runInfo.getSchedule().tick();
		assertTrue(indicator);

		this.indicator = false;

		for (int i = 0; i < ((RepeatingProductivityInnovation) r1
				.getInnovationRegistry().getInnovation(INNOVATION_ID_CSV))
				.getRepetitionComp().getRenewalInterval(); i++) {
			BasicTestsUtils.runInfo.getSchedule().tick();
		}

		assertTrue(indicator);
	}
}
