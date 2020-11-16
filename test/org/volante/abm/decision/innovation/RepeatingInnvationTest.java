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


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.Service;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.RepeatingInnovativeInstitution;
import org.volante.abm.institutions.innovation.RepeatingProductivityInnovation;
import org.volante.abm.models.utils.ProductionWeightReporter;

/**
 * @author Sascha Holzhauer
 *
 */
public class RepeatingInnvationTest extends InnovationTestUtils {

	public final String	INNOVATION_ID					= "RepeatingTestInnovation";
	public final String	REPEATING_INNOVATION_XML_FILE	= "xml/RepeatingInnovationInstitution.xml";
	
	public final double INNOVATION_EFFECT_ON_PRODUCTIVITY = 2.0;

	protected RepeatingInnovativeInstitution	institution						= null;

	protected RepeatingProductivityInnovation currentInnovation = null;

	public boolean indicator = false;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// init institution
		persister = runInfo.getPersister();
		try {
			this.institution = persister.read(
					RepeatingInnovativeInstitution.class,
 persister
							.getFullPath(REPEATING_INNOVATION_XML_FILE,
									this.r1.getPersisterContextExtra()));
			this.institution.initialise(modelData, runInfo, r1);
			registerInstitution(this.institution, this.r1);

			// to initialise innovation...
			BasicTestsUtils.runInfo.getSchedule().tick();
			
			this.currentInnovation = (RepeatingProductivityInnovation) r1.getInnovationRegistry()
					.getInnovation(INNOVATION_ID);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		r1.getInnovationRegistry().reset();
	}

	@Test
	public void testDiscountFactor() {
		Service service = BasicTestsUtils.modelData.services.forName("FOOD");

		LandUseAgent one = this.agentAssemblerR1.assembleAgent(null, "Innovator",
 innovativeFarming.getLabel());

		double initialProductivity = ((ProductionWeightReporter) one
				.getProductionModel()).getProductionWeights()
				.getDouble(service);
		// Tick 1
		adoptAndCheck(one, 1, service, initialProductivity);

		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 2
		LandUseAgent two = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());

		adoptAndCheck(two, 2, service, initialProductivity);
		BasicTestsUtils.runInfo.getSchedule().tick();
		BasicTestsUtils.runInfo.getSchedule().tick();

		// Tick 4
		LandUseAgent three = this.agentAssemblerR1.assembleAgent(null, "Innovator",
				innovativeFarming.getLabel());

		adoptAndCheck(three, 4, service, initialProductivity);
	}

	/**
	 * @param agent
	 */
	protected void adoptAndCheck(LandUseAgent agent, int ticks, Service service,
			double initialProductivity) {
		// need to adopt here in order to enable time-delayed adoptions
		((InnovativeBC) agent.getBC()).makeAware(this.currentInnovation);
		((InnovativeBC) agent.getBC()).makeTrial(this.currentInnovation);
		((InnovativeBC) agent.getBC()).makeAdopted(this.currentInnovation);

		checkCapitalChange(agent, InnovationTestUtils.innovativeFarming,
				INNOVATION_EFFECT_ON_PRODUCTIVITY
						* Math.pow(((RepeatingProductivityInnovation) r1.getInnovationRegistry()
								.getInnovation(INNOVATION_ID)).getEffectDiscountFactor(),
 ticks)
						* initialProductivity, service);
	}
}
