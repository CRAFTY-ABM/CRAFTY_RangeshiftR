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
import static org.junit.Assert.assertNotEquals;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.decision.innovation.InnovationTestUtils;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.RepeatingInnovativeInstitution;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.status.InnovationStates;

/**
 * @author Sascha Holzhauer
 *
 */
public class RepetitiveRecruitmentTest extends InnovationTestUtils {

	public static final String REPETITIVE_RECRUITMENT_XML_FILE = "xml/RepetitiveRecruitment.xml";

	protected RepeatingInnovativeInstitution institution = null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.setupPseudoRandomEngine();

		// add 8 agents to region 1:
		addInnovationAgentsToRegion1(8, innovativeFarming);

		persister = runInfo.getPersister();
		try {
			this.institution = persister.read(
					RepeatingInnovativeInstitution.class,
 persister
							.getFullPath(REPETITIVE_RECRUITMENT_XML_FILE,
									this.r1.getPersisterContextExtra()));
			this.institution.initialise(modelData, runInfo, r1);
			this.institution.update(); // institution is not registered at Institutions...
										// initialises innovation

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Test
	public void test() {
		// Tick 0

		for (Agent agent : r1.getAllAllocatedAgents()) {
			agent.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD, 0.0);
		}
		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 1
		// Initial recruitment
		Set<InnovativeBC> ibcs = new LinkedHashSet<InnovativeBC>();
		for (Agent agent : r1.getAllAllocatedAgents()) {
			if (agent.getBC() instanceof InnovativeBC) {
				ibcs.add((InnovativeBC) agent.getBC());
			}
		}
		Collection<InnovativeBC> informedAgents = this.institution
				.getInstitutionTargetRecruitment().getRecruitedAgents(ibcs);
		assertEquals("50% of 8 agents should be recruited!", 4,
				informedAgents.size());

		// check awareness for first innovation
		Innovation firstInnovation = this.institution.getCurrentInnovation();
		checkInnovationState(firstInnovation, informedAgents,
				InnovationStates.TRIAL);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 2
		Innovation secondInnovation = this.institution.getCurrentInnovation();
		assertNotEquals("Innovation should have been renewed", firstInnovation, secondInnovation);

		// check awareness for second innovation (needs to be same set of agents):
		checkInnovationState(secondInnovation, informedAgents,
				InnovationStates.TRIAL);

		BasicTestsUtils.runInfo.getSchedule().tick();
		// Tick 3
		Innovation thirdInnovation = this.institution.getCurrentInnovation();
		assertNotEquals("Innovation should have been renewed", secondInnovation, thirdInnovation);

		// check awareness for third innovation (needs to be same set of agents):
		checkInnovationState(thirdInnovation, informedAgents,
				InnovationStates.TRIAL);
	}
}
