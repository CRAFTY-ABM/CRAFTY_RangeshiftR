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
 * Created by Sascha Holzhauer on 9 Dec 2014
 */
package org.volante.abm.decision.innovation;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.data.Service;
import org.volante.abm.decision.trigger.PositiveDT;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.InnovativeInstitution;
import org.volante.abm.models.utils.ProductionWeightReporter;

/**
 * @author Sascha Holzhauer
 *
 */
public class InnovativeInstitutionTest extends InnovationTestUtils {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(InnovativeInstitutionTest.class);

	public final String INNOVATION_ID = "TestInnovation";
	public final String INSTITUTION_XML_FILE = "xml/InnovativeInstitution.xml";
	public final String INSTITUTION_AMBULANT_XML_FILE = "xml/InnovativeInstitutionAmbulant.xml";

	public final double EFFECT_ON_PRODUCTIVITY_FACTOR = 2.0;

	protected InnovativeInstitution institution;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// nothing to do
	}

	/**
	 * 
	 */
	protected void setUpInstitution(String institution_xml) {
		// <- LOGGING
		logger.info("START UP InnvativeInstitutionTest");
		// LOGGING ->

		// init institution
		persister = runInfo.getPersister();
		try {
			this.institution = persister.read(InnovativeInstitution.class,
					persister.getFullPath(institution_xml,
							this.r1.getPersisterContextExtra()));
			this.institution.initialise(modelData, runInfo, r1);
			registerInstitution(this.institution, this.r1);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Test
	public void test() throws Exception {
		setUpInstitution(INSTITUTION_XML_FILE);

		Service serviceFood = BasicTestsUtils.modelData.services
				.forName("FOOD");
		Service serviceTimber = BasicTestsUtils.modelData.services
				.forName("TIMBER");
		
		LandUseAgent forester = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeForestry.getLabel());
		LandUseAgent farmerA = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());
		LandUseAgent farmerB = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());

		r1.setOwnership(forester, c11);
		r1.setOwnership(farmerA, c12);
		r1.setOwnership(farmerB, c13);

		double initialProductivityForesterFood = ((ProductionWeightReporter) forester
				.getProductionModel()).getProductionWeights().getDouble(
				serviceFood);

		double initialProductivityForesterTimber = ((ProductionWeightReporter) forester
				.getProductionModel()).getProductionWeights().getDouble(
				serviceTimber);
		
		double initialProductivityFarmerFood = ((ProductionWeightReporter) farmerA
				.getProductionModel()).getProductionWeights().getDouble(
				serviceFood);

		double initialProductivityFarmerTimber = ((ProductionWeightReporter) farmerA
				.getProductionModel()).getProductionWeights().getDouble(
				serviceTimber);

		// trigger innovation decisions at agents:
		PositiveDT pdt = new PositiveDT("Innovation");
		pdt.initialise(modelData, runInfo);
		forester.getBC().getType().addDecisionTrigger(pdt);

		// initialise innovation and spread
		BasicTestsUtils.runInfo.getSchedule().tick();

		
		// check that farming agents trailed the innovation
		checkCapitalChange(forester,
				InnovationTestUtils.innovativeForestry,
				initialProductivityForesterFood, serviceFood);

		checkCapitalChange(forester,
				InnovationTestUtils.innovativeForestry,
				initialProductivityForesterTimber, serviceTimber);

		checkCapitalChange(farmerA, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerFood * EFFECT_ON_PRODUCTIVITY_FACTOR,
				serviceFood);
		
		checkCapitalChange(farmerA, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerTimber, serviceTimber);

		checkCapitalChange(farmerB, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerFood * EFFECT_ON_PRODUCTIVITY_FACTOR,
				serviceFood);
		
		checkCapitalChange(farmerB, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerTimber, serviceTimber);
	}

	@Test
	public void testAmbulant() throws Exception {
		setUpInstitution(INSTITUTION_AMBULANT_XML_FILE);

		Service serviceFood = BasicTestsUtils.modelData.services
				.forName("FOOD");
		Service serviceTimber = BasicTestsUtils.modelData.services
				.forName("TIMBER");

		LandUseAgent forester = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeForestry.getLabel());
		LandUseAgent farmerA = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());
		LandUseAgent farmerB = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());

		double initialProductivityForesterFood = ((ProductionWeightReporter) forester
				.getProductionModel()).getProductionWeights().getDouble(
				serviceFood);

		double initialProductivityForesterTimber = ((ProductionWeightReporter) forester
				.getProductionModel()).getProductionWeights().getDouble(
				serviceTimber);

		double initialProductivityFarmerFood = ((ProductionWeightReporter) farmerA
				.getProductionModel()).getProductionWeights().getDouble(
				serviceFood);

		double initialProductivityFarmerTimber = ((ProductionWeightReporter) farmerA
				.getProductionModel()).getProductionWeights().getDouble(
				serviceTimber);

		// trigger innovation decisions at agents:
		PositiveDT pdt = new PositiveDT("Innovation");
		pdt.initialise(modelData, runInfo);
		forester.getBC().getType().addDecisionTrigger(pdt);

		// initialise innovation and spread
		BasicTestsUtils.runInfo.getSchedule().tick();

		// check that farming agents trailed the innovation
		checkCapitalChange(forester, InnovationTestUtils.innovativeForestry,
				initialProductivityForesterFood, serviceFood);

		checkCapitalChange(forester, InnovationTestUtils.innovativeForestry,
				initialProductivityForesterTimber, serviceTimber);

		checkCapitalChange(farmerA, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerFood * EFFECT_ON_PRODUCTIVITY_FACTOR,
				serviceFood);

		checkCapitalChange(farmerA, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerTimber, serviceTimber);

		checkCapitalChange(farmerB, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerFood * EFFECT_ON_PRODUCTIVITY_FACTOR,
				serviceFood);

		checkCapitalChange(farmerB, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerTimber, serviceTimber);
	}

	@Test
	public void testAlteredAgentID() {
		setUpInstitution(INSTITUTION_XML_FILE);

		Service serviceFood = BasicTestsUtils.modelData.services.forName("FOOD");
		Service serviceTimber = BasicTestsUtils.modelData.services.forName("TIMBER");
		
		LandUseAgent forester =
				this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeForestry.getLabel(), "Forester");
		LandUseAgent farmerA =
				this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "FarmerA");
		LandUseAgent farmerB =
				this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel(), "FarmerB");

		r1.setOwnership(forester, c11);
		r1.setOwnership(farmerA, c12);
		r1.setOwnership(farmerB, c13);

		double initialProductivityForesterFood = ((ProductionWeightReporter) forester
				.getProductionModel()).getProductionWeights()
				.getDouble(serviceFood);
		
		double initialProductivityForesterTimber = ((ProductionWeightReporter) forester
				.getProductionModel()).getProductionWeights().getDouble(
				serviceTimber);
		
		double initialProductivityFarmerFood = ((ProductionWeightReporter) farmerA
				.getProductionModel()).getProductionWeights()
				.getDouble(serviceFood);

		double initialProductivityFarmerTimber = ((ProductionWeightReporter) farmerA
				.getProductionModel()).getProductionWeights().getDouble(
				serviceTimber);

		// initialise innovation and spread
		BasicTestsUtils.runInfo.getSchedule().tick();

		
		// check that farming agents trialed the innovation
		checkCapitalChange(forester,
				InnovationTestUtils.innovativeForestry,
				initialProductivityForesterFood, serviceFood);
		
		checkCapitalChange(forester,
				InnovationTestUtils.innovativeForestry,
				initialProductivityForesterTimber, serviceTimber);

		checkCapitalChange(farmerA, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerFood * EFFECT_ON_PRODUCTIVITY_FACTOR, serviceFood);
		
		checkCapitalChange(farmerA, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerTimber, serviceTimber);

		checkCapitalChange(farmerB, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerFood * EFFECT_ON_PRODUCTIVITY_FACTOR, serviceFood);
		
		checkCapitalChange(farmerB, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerTimber, serviceTimber);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testAll() {
		setUpInstitution(INSTITUTION_XML_FILE);

		this.institution.getCurrentInnovation().setAffectedAFTs("all");
		Service serviceFood = BasicTestsUtils.modelData.services
				.forName("FOOD");
		Service serviceTimber = BasicTestsUtils.modelData.services
				.forName("TIMBER");
		
		LandUseAgent forester = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeForestry.getLabel());
		LandUseAgent farmerA = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());
		LandUseAgent farmerB = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());

		r1.setOwnership(forester, c11);
		r1.setOwnership(farmerA, c12);
		r1.setOwnership(farmerB, c13);

		double initialProductivityForesterFood = ((ProductionWeightReporter) forester
				.getProductionModel()).getProductionWeights().getDouble(
				serviceFood);

		double initialProductivityForesterTimber = ((ProductionWeightReporter) forester
				.getProductionModel()).getProductionWeights().getDouble(
				serviceTimber);
		
		double initialProductivityFarmerFood = ((ProductionWeightReporter) farmerA
				.getProductionModel()).getProductionWeights().getDouble(
				serviceFood);

		double initialProductivityFarmerTimber = ((ProductionWeightReporter) farmerA
				.getProductionModel()).getProductionWeights().getDouble(
				serviceTimber);

		// initialise innovation and spread
		BasicTestsUtils.runInfo.getSchedule().tick();

		
		// check that farming agents trialed the innovation
		checkCapitalChange(forester,
				InnovationTestUtils.innovativeForestry,
				initialProductivityForesterFood * EFFECT_ON_PRODUCTIVITY_FACTOR,
				serviceFood);

		checkCapitalChange(forester,
				InnovationTestUtils.innovativeForestry,
				initialProductivityForesterTimber, serviceTimber);

		checkCapitalChange(farmerA, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerFood * EFFECT_ON_PRODUCTIVITY_FACTOR,
				serviceFood);
		
		checkCapitalChange(farmerA, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerTimber, serviceTimber);

		checkCapitalChange(farmerB, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerFood * EFFECT_ON_PRODUCTIVITY_FACTOR,
				serviceFood);
		
		checkCapitalChange(farmerB, InnovationTestUtils.innovativeFarming,
				initialProductivityFarmerTimber, serviceTimber);
	}

}
