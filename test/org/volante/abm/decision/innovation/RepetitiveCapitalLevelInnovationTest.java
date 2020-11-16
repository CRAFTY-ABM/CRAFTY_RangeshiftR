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
 * Created by Sascha Holzhauer on 10 Dec 2014
 */
package org.volante.abm.decision.innovation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.RepeatingInnovativeInstitution;
import org.volante.abm.institutions.innovation.RepeatingCapitalLevelInnovation;
import org.volante.abm.institutions.recruit.InstitutionTargetRecruitment;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class RepetitiveCapitalLevelInnovationTest extends InnovationTestUtils {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(RepetitiveCapitalLevelInnovationTest.class);

	public final String INNOVATION_ID_CSV = "RepeatingCapitalLevelTestInnovationCSV";
	public final String REPEATING_CSV_INNOVATION_XML_FILE = "xml/RepeatingCapitalLevelInnovationInstitutionCsv.xml";
	public final String REPEATING_INNOVATION_CSV_FACTOR_FILE = "csv/CsvCapitalLevelInnovationRepCompTestfile.csv";

	public final int RENEWAL_INTERVAL = 2; // should also be tested with e.g. 3

	public final double[] INNOVATION_EFFECT_CSV_FACTORS = { 1.0, 1.2, 1.5, 2.0 };

	protected RepeatingInnovativeInstitution csvInstitution = null;
	protected RepeatingCapitalLevelInnovation innovation = null;

	public boolean indicator = false;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		// <- LOGGING
		logger.info("START UP RepetitiveCapitalLevelInnovationTest");
		// LOGGING ->

		// init institution
		persister = runInfo.getPersister();
		try {
			this.csvInstitution = persister.read(
					RepeatingInnovativeInstitution.class, persister
							.getFullPath(REPEATING_CSV_INNOVATION_XML_FILE,
									this.r1.getPersisterContextExtra()));
			this.csvInstitution.initialise(modelData, runInfo, r1);
			registerInstitution(this.csvInstitution, this.r1);

			// initialise innovation...
			BasicTestsUtils.runInfo.getSchedule().tick();

			this.innovation = (RepeatingCapitalLevelInnovation) r1
					.getInnovationRegistry().getInnovation(INNOVATION_ID_CSV);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		r1.getInnovationRegistry().reset();
	}

	/**
	 * 
	 */
	@Test
	public void testCsvFactor() {
		Capital capital = BasicTestsUtils.modelData.capitals
				.forName("NATURAL_CROPS");
		Cell[] cells = this.r1cells.toArray(new Cell[1]);
		cells[1].setBaseCapitals(cellCapitalsA);
		cells[2].setBaseCapitals(cellCapitalsA);
		cells[3].setBaseCapitals(cellCapitalsA);

		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "One");

		LandUseAgent two = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "Two");

		LandUseAgent three =
				this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "Three");

		this.r1.setOwnership(one, cells[1]);
		this.r1.setOwnership(two, cells[2]);
		this.r1.setOwnership(three, cells[3]);

		double initialCapital = BasicTestsUtils.cellCapitalsA.get(capital);

		// Tick 0 finished
		checkCapitalLevel(one, 0, capital, initialCapital);

		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 1 finished
		checkCapitalLevel(two, 1, capital, initialCapital);

		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 2 finished
		checkCapitalLevel(three, 2, capital, initialCapital);

		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 3 finished
		// second adoption based on first adoption:
		checkCapitalLevel(
				three,
				3,
				capital,
				initialCapital
						* 1.002
						* (RENEWAL_INTERVAL < 3 ? INNOVATION_EFFECT_CSV_FACTORS[RENEWAL_INTERVAL]
								: 1));
	}

	protected void checkCapitalLevel(final LandUseAgent agent, int ticks,
			Capital capital, double initialCapital) {
		
		double expectedValue = initialCapital;
		double effect = 1.002;

		for (int i = 0; i <= ticks; i++) {
			if (i % RENEWAL_INTERVAL == 0) {
				effect = effect * INNOVATION_EFFECT_CSV_FACTORS[i];
			}
		}

		checkCapitalChange(agent, expectedValue * effect,
				capital);
	}

	/**
	 * Checks that the agent's productivity for the given service is equals to
	 * the given expected one.
	 * 
	 * @param agent
	 * @param expectedCapital
	 * @param capital
	 */
	public void checkCapitalChange(LandUseAgent agent,
			double expectedCapital,
			Capital capital) {
		// need to adopt here in order to enable time-delayed adoptions
		((InnovativeBC) agent.getBC()).makeAware(this.csvInstitution
				.getCurrentInnovation());
		((InnovativeBC) agent.getBC()).makeTrial(this.csvInstitution
				.getCurrentInnovation());
		((InnovativeBC) agent.getBC()).makeAdopted(this.csvInstitution
				.getCurrentInnovation());

		double actualCapital;
		for (Cell c : agent.getCells()) {
			actualCapital = c.getEffectiveCapitals().getDouble(capital);

			// <- LOGGING
			logger.info("Check " + agent.getID() + "s productivity..."
					+ expectedCapital + " - " + actualCapital);
			// LOGGING ->

			assertEquals("Check " + agent.getID() + "s productivity...",
					expectedCapital, actualCapital, 0.0001);
		}
	}

	/**
	 */
	@Test
	public void testInnovationRenewal() {
		final Agent agent = new DefaultSocialLandUseAgent(
				innovativeFarming, "ID", modelData, r1,
 farmingProduction.copyWithNoise(modelData, null,
						null), 0.5, 0.5);
		this.innovationTestBT.assignNewBehaviouralComp(agent);

		this.csvInstitution
				.setInstitutionTargetRecruitment(new InstitutionTargetRecruitment() {

					@Override
					public Collection<InnovativeBC> getRecruitedAgents(
							Collection<? extends InnovativeBC> allAgents) {
						Collection<InnovativeBC> agents = new ArrayList<>();
						agents.add((InnovativeBC) agent.getBC());

						RepetitiveCapitalLevelInnovationTest.this.indicator = true;

						return agents;
					}

					@Override
					public void initialise(ModelData data, RunInfo info,
							Region extent) throws Exception {
					}
				});
		;

		for (int i = 1; i < RENEWAL_INTERVAL; i++) {
			BasicTestsUtils.runInfo.getSchedule().tick();
			assertFalse(indicator);
		}

		for (int i = 1; i <= ((RepeatingCapitalLevelInnovation) r1
				.getInnovationRegistry().getInnovation(INNOVATION_ID_CSV))
				.getRepetitionComp().getRenewalInterval(); i++) {
			BasicTestsUtils.runInfo.getSchedule().tick();
		}

		assertTrue(indicator);
	}
}
