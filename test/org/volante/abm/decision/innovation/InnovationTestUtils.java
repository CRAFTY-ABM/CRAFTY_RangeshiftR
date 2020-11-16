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
 * Created by Sascha Holzhauer on 3 Dec 2014
 */
package org.volante.abm.decision.innovation;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.agent.bt.InnovativeCognitiveBC;
import org.volante.abm.agent.bt.InnovativeCognitiveBT;
import org.volante.abm.agent.fr.DefaultFR;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.fr.IndividualProductionFR;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.Institution;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.repeat.CsvProductivityInnovationRepComp;
import org.volante.abm.institutions.innovation.status.InnovationState;
import org.volante.abm.models.utils.ProductionWeightReporter;


/**
 * @author Sascha Holzhauer
 *
 */
public class InnovationTestUtils extends BasicTestsUtils {

	public BehaviouralType innovationTestBT = new InnovativeCognitiveTestBT();

	public InnovationTestUtils() {
	}


	/**
	 * @see org.volante.abm.example.BasicTestsUtils#setupBasicTestEnvironment()
	 */
	@Before
	public void setupBasicTestEnvironment() {
		super.setupBasicTestEnvironment();

		innovativeForestry =
				new DefaultFR("InnoForestry", 111,
				BasicTestsUtils.forestryProduction.copyWithNoise(modelData,
						null, null), BasicTestsUtils.forestryGivingUp,
				BasicTestsUtils.forestryGivingIn);


		innovativeFarming =
				new IndividualProductionFR("InnoFarming", 112,
				BasicTestsUtils.farmingProduction.copyWithNoise(modelData,
						null, null), BasicTestsUtils.farmingGivingUp,
				BasicTestsUtils.farmingGivingIn);

		potentialAgents = new HashSet<FunctionalRole>(
				Arrays.asList(new FunctionalRole[] { innovativeForestry,
						innovativeFarming }));
		// this.r1.clearFunctionalRoles();
		this.r1.addfunctionalRoles(potentialAgents);

		innoFarmingA = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());
		innoForesterA = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeForestry.getLabel());

		ArrayList<BehaviouralType> collection = new ArrayList<BehaviouralType>();
		collection.add(innovationTestBT);
		this.r1.addBehaviouralTypes(collection);
	}

	/**
	 * Enables setting relative to base during runtime.
	 * 
	 * @author Sascha Holzhauer
	 *
	 */
	public static class CsvProductivityInnovationRepTestComp extends
			CsvProductivityInnovationRepComp {

		public void setRelativeToPreviousTick(boolean relative) {
			this.considerFactorsRelativeToPreviousTick = relative;
		}
	}

	public static class InnovativeCognitiveTestBT extends InnovativeCognitiveBT {

		public boolean indicator = false;

		public InnovativeCognitiveTestBT() {
			this.label = "TestInnovator";
			this.serialID = 77;
		}

		/**
		 * @see org.volante.abm.agent.bt.InnovativeCognitiveBT#assignNewBehaviouralComp(org.volante.abm.agent.Agent)
		 */
		@Override
		public final Agent assignNewBehaviouralComp(Agent agent) {
			agent.setBC(new InnovativeCognitiveBC(this, agent) {
				public void makeAware(Innovation innovation) {
					super.makeAware(innovation);
					indicator = true;
				}
			});
			return agent;
		}
	}

	public static FunctionalRole innovativeForestry;

	public static FunctionalRole innovativeFarming;

	public static Set<FunctionalRole> potentialAgents;

	public Agent innoFarmingA;

	public Agent innoForesterA;

	protected static void checkInnovationState(Innovation innovation,
			Collection<InnovativeBC> agents,
			InnovationState status) {
		for (InnovativeBC ibc : agents) {
				assertEquals("Check innovation status", status,
					(ibc).getState(innovation));
		}
	}

	protected void addInnovationAgentsToRegion1(int numberOfAgents,
			FunctionalRole fRole) {
		Cell[] cells = this.r1cells.toArray(new Cell[1]);

		if (numberOfAgents > cells.length) {
			throw new IllegalStateException("Only " + cells.length + " cells available, but " +
					numberOfAgents + " requested!");
		}
		for (int i = 0; i < numberOfAgents; i++) {
			this.r1.setOwnership(this.agentAssemblerR1.assembleAgent(cells[i],
					"Innovator", fRole.getLabel()), cells[i]);
		}
	}

	/**
	 * Register given institution at given region and create
	 * {@link Institutions} if not present at region.
	 * 
	 * @param institution
	 * @param region
	 */
	protected void registerInstitution(Institution institution, Region region) {
		Institutions institutions = region.getInstitutions();
		institutions.addInstitution(institution);
	}

	/**
	 * Checks that the agent's productivity for the given service is equals to
	 * the given expected one.
	 * 
	 * @param agent
	 * @param expectedProductivity
	 * @param service
	 */
	public void checkCapital(LandUseAgent agent, double expectedProductivity,
			Service service) {
		double actualProductivity;
		if (agent.getProductionModel() instanceof ProductionWeightReporter) {
			actualProductivity = ((ProductionWeightReporter) agent
					.getProductionModel()).getProductionWeights().getDouble(
					service);

			assertEquals("Check " + agent.getID() + "s productivity...",
					expectedProductivity, actualProductivity, 0.0001);
		} else {
			fail("Could not test productivity because agent's production model is not a ProductionWeightReporter!");
		}
	}

	public void checkCapitalChange(LandUseAgent agent, FunctionalRole fRole,
			double expectedProductivity, Service service) {
		if (fRole.getProduction() instanceof ProductionWeightReporter) {
			checkCapital(agent, expectedProductivity, service);
		} else {
			fail("Could not test productivity because potential agent's production model is not a ProductionWeightReporter!");
		}

	}
}
