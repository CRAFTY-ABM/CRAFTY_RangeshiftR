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
 * Created by Sascha Holzhauer on 7 Jan 2015
 */
package org.volante.abm.decision.innovation;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.CapitalDynamicsInstitution;
import org.volante.abm.institutions.InnovativeInstitution;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.institutions.innovation.VariableCapitalLevelInnovation;
import org.volante.abm.serialization.ABMPersister;

/**
 * @author Sascha Holzhauer
 *
 */
public class VariableCapitalLevelInnovationTest extends InnovationTestUtils {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(VariableCapitalLevelInnovationTest.class);

	public final String INNOVATION_ID = "VariableInnovation";
	public final String INSTITUTION_XML_FILE = "xml/VariableCapitalLevelInnovationInstitutionCsv.xml";

	private static final String CAPITAL_INSTITUTION_XML_FILENAME = "xml/Institutions_CapitalDynamics.xml";

	public final String CONSIDERED_CAPITAL = "NATURAL_CROPS";

	public final double[] INNOVATION_EFFECT_FACTORS = { 1.1, 1.21, 1.331,
			1.4641, 1.61051 };

	public final double[] CAPITAL_INSTITUTION_EFFECT_FACTORS_NATURAL_CROPS = {
			0.8, 0.8, 0.8 };

	VariableCapitalLevelInnovation innovation;
	InnovativeInstitution institution;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// <- LOGGING
		logger.info("START UP VariabelCapitalLevelInnovationTest");
		// LOGGING ->

		// init institution
		persister = runInfo.getPersister();
		try {
			this.institution = persister.read(InnovativeInstitution.class,
					persister.getFullPath(INSTITUTION_XML_FILE,
							this.r1.getPersisterContextExtra()));
			this.institution.initialise(modelData, runInfo, r1);

			registerInstitution(this.institution, this.r1);

			// initialise innovation...
			BasicTestsUtils.runInfo.getSchedule().tick();

			this.innovation = (VariableCapitalLevelInnovation) r1
					.getInnovationRegistry().getInnovation(INNOVATION_ID);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Test
	public void testVariableCapitalAdjustments() {
		Capital capital = BasicTestsUtils.modelData.capitals
				.forName(CONSIDERED_CAPITAL);
		double initialCapital = BasicTestsUtils.cellCapitalsA.get(capital);

		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "One");

		Cell[] cells = this.r1cells.toArray(new Cell[1]);
		cells[1].setBaseCapitals(cellCapitalsA);

		this.r1.setOwnership(one, cells[1]);

		((InnovativeBC) one.getBC()).makeAware(innovation);
		((InnovativeBC) one.getBC()).makeTrial(innovation);
		((InnovativeBC) one.getBC()).makeAdopted(innovation);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 1 finished

		checkCapitalLevel(one, 1, capital, initialCapital);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 2 finished

		checkCapitalLevel(one, 2, capital, initialCapital);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 3 finished

		checkCapitalLevel(one, 3, capital, initialCapital);
	}

	@Test
	public void testUnperformVariableCapitalAdjustments() {
		Capital capital = BasicTestsUtils.modelData.capitals
				.forName(CONSIDERED_CAPITAL);
		double initialCapital = BasicTestsUtils.cellCapitalsA.get(capital);

		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "One");

		Cell[] cells = this.r1cells.toArray(new Cell[1]);
		cells[1].setBaseCapitals(cellCapitalsA);
		this.r1.setOwnership(one, cells[1]);

		((InnovativeBC) one.getBC()).makeAware(innovation);
		((InnovativeBC) one.getBC()).makeTrial(innovation);
		((InnovativeBC) one.getBC()).makeAdopted(innovation);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 1 finished

		checkCapitalLevel(one, 1, capital, initialCapital);

		((InnovativeBC) one.getBC()).rejectInnovation(innovation);
		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 2 finished

		assertEquals(initialCapital,
				cells[1].getEffectiveCapitals().getDouble(capital), 0.0001);
	}

	@Test
	public void testUnchangingBaseCapitals() {
		Capital capital = BasicTestsUtils.modelData.capitals
				.forName(CONSIDERED_CAPITAL);
		double initialCapital = BasicTestsUtils.cellCapitalsA.get(capital);

		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "One");

		Cell[] cells = this.r1cells.toArray(new Cell[1]);
		cells[1].setBaseCapitals(cellCapitalsA);
		this.r1.setOwnership(one, cells[1]);

		((InnovativeBC) one.getBC()).makeAware(innovation);
		((InnovativeBC) one.getBC()).makeTrial(innovation);
		((InnovativeBC) one.getBC()).makeAdopted(innovation);

		assertEquals(initialCapital,
				cells[1].getBaseCapitals().getDouble(capital), 0.0001);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 1 finished
		assertEquals(initialCapital,
				cells[1].getBaseCapitals().getDouble(capital), 0.0001);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 2 finished
		assertEquals(initialCapital,
				cells[1].getBaseCapitals().getDouble(capital), 0.0001);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 3 finished
		assertEquals(initialCapital,
				cells[1].getBaseCapitals().getDouble(capital), 0.0001);
	}

	@Test
	public void testDifferenAdoptionDatesCapitalAdjustments() {
		Capital capital = BasicTestsUtils.modelData.capitals
				.forName(CONSIDERED_CAPITAL);
		Cell[] cells = this.r1cells.toArray(new Cell[1]);

		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "One");

		cells[1].setBaseCapitals(cellCapitalsA);
		this.r1.setOwnership(one, cells[1]);

		LandUseAgent two = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "Two");

		cells[2].setBaseCapitals(cellCapitalsA);
		this.r1.setOwnership(two, cells[2]);

		double initialCapital = BasicTestsUtils.cellCapitalsA.get(capital);

		((InnovativeBC) one.getBC()).makeAware(innovation);
		((InnovativeBC) one.getBC()).makeTrial(innovation);
		((InnovativeBC) one.getBC()).makeAdopted(innovation);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 1 finished
		checkCapitalLevel(one, 1, capital, initialCapital);
		assertEquals(initialCapital,
				cells[2].getEffectiveCapitals().getDouble(capital), 0.0001);

		((InnovativeBC) two.getBC()).makeAware(innovation);
		((InnovativeBC) two.getBC()).makeTrial(innovation);
		((InnovativeBC) two.getBC()).makeAdopted(innovation);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 2 finished
		checkCapitalLevel(one, 2, capital, initialCapital);
		checkCapitalLevel(two, 2, capital, initialCapital);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 3 finished
		checkCapitalLevel(one, 3, capital, initialCapital);
		checkCapitalLevel(two, 3, capital, initialCapital);
	}

	@Test
	public void testCapitalDynamicsInstitutionCombination() throws Exception {
		Capital capital = BasicTestsUtils.modelData.capitals
				.forName(CONSIDERED_CAPITAL);
		double initialCapital = BasicTestsUtils.cellCapitalsA.get(capital);

		CapitalDynamicsInstitution institution = ABMPersister.getInstance()
				.readXML(CapitalDynamicsInstitution.class,
						CAPITAL_INSTITUTION_XML_FILENAME,
						this.r1.getPersisterContextExtra());
		institution.initialise(modelData, runInfo, r1);

		Institutions institutions = r1.getInstitutions();
		institutions.addInstitution(institution);

		// setup adopted agent:
		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "One");

		Cell[] cells = this.r1cells.toArray(new Cell[1]);
		cells[1].setBaseCapitals(cellCapitalsA);
		this.r1.setOwnership(one, cells[1]);

		((InnovativeBC) one.getBC()).makeAware(innovation);
		((InnovativeBC) one.getBC()).makeTrial(innovation);
		((InnovativeBC) one.getBC()).makeAdopted(innovation);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 1 finished

		for (Cell c : one.getCells()) {
			double actualCapital = c.getEffectiveCapitals().getDouble(capital);

			assertEquals("Check " + one.getID() + "s cells' capital level...",
					initialCapital
							* CAPITAL_INSTITUTION_EFFECT_FACTORS_NATURAL_CROPS[2]
							* INNOVATION_EFFECT_FACTORS[1 - 1],
					actualCapital, 0.0001);
		}

		assertEquals(initialCapital,
				cells[1].getBaseCapitals().getDouble(capital), 0.0001);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 2 finished

		for (Cell c : one.getCells()) {
			double actualCapital = c.getEffectiveCapitals().getDouble(capital);

			assertEquals(
					"Check " + one.getID() + "s cells' capital level...",
					initialCapital
							* CAPITAL_INSTITUTION_EFFECT_FACTORS_NATURAL_CROPS[2 - 1]
							* INNOVATION_EFFECT_FACTORS[2 - 1], actualCapital,
					0.0001);
		}

		assertEquals(initialCapital,
				cells[1].getBaseCapitals().getDouble(capital), 0.0001);
	}

	protected void checkCapitalLevel(final LandUseAgent agent, int ticks,
			Capital capital, double initialCapitalValue) {

		double expectedCapitalValue = initialCapitalValue
				* INNOVATION_EFFECT_FACTORS[ticks - 1];

		for (Cell c : agent.getCells()) {
			double actualCapital = c.getEffectiveCapitals().getDouble(capital);

			assertEquals(
					"Check " + agent.getID() + "s cells' capital level...",
					expectedCapitalValue, actualCapital, 0.0001);
		}
	}
}
