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
 * Created by Sascha Holzhauer on 8 Dec 2014
 */
package org.volante.abm.institution.recruit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.Region;
import org.volante.abm.decision.innovation.InnovationTestUtils;
import org.volante.abm.institutions.InnovativeInstitution;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.status.InnovationState;
import org.volante.abm.institutions.innovation.status.InnovationStates;

/**
 * @author Sascha Holzhauer
 * 
 */
public class ProbabilityRandomRecruitmentTest extends InnovationTestUtils {

	public static final String PERCENTAL_RANDOM_RECRUITMENT_XML_FILE = "xml/ProbabilityRandomRecruitmentInnovation.xml";

	protected InnovativeInstitution institution = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		setupPseudoRandomEngine();

		// add 8 agents to region 1:
		addInnovationAgentsToRegion1(8, innovativeFarming);

		persister = runInfo.getPersister();
		try {
			this.institution = persister.read(InnovativeInstitution.class,
					persister.getFullPath(
							PERCENTAL_RANDOM_RECRUITMENT_XML_FILE,
							this.r1.getPersisterContextExtra()));
			this.institution.initialise(modelData, runInfo, r1);

			// Need to update manually since institution is not registered at
			// Institutions...
			this.institution.update();

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Test
	public void test() {
		// Tick 0
		// Initial recruitment
		checkNumberOfAgentsWithInnovationState(r1, 4, InnovationStates.TRIAL,
				this.institution.getCurrentInnovation());
	}

	protected void checkNumberOfAgentsWithInnovationState(Region region,
			int number, InnovationState state, Innovation innovation) {
		int counter = 0;
		for (Agent agent : region.getAllAllocatedAgents()) {
			if (agent.getBC() instanceof InnovativeBC) {
				if (((InnovativeBC) agent.getBC()).getState(innovation) == state) {
					counter++;
				}
			}
		}
		assertEquals("Check number of agents of state " + state, number,
				counter);
	}
}
