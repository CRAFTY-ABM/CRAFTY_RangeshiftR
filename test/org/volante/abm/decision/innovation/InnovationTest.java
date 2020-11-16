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
 * Created by Sascha Holzhauer on 12.03.2014
 */
package org.volante.abm.decision.innovation;


import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.Region;
import org.volante.abm.decision.pa.InnovationPa;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.InnovationRegistry;


/**
 * Tests {@link InnovationRegistry}
 * 
 * @author Sascha Holzhauer
 * 
 */
public class InnovationTest extends BasicTestsUtils {

	public final String	INNOVATION_ID					= "TestInnovation";
	public final String	SINGLETON_INNOVATION_XML_FILE	= "xml/Innovations.xml";

	public static class TestInnovation extends Innovation {

		static TestInnovation	innovation;

		public TestInnovation(@Attribute(name = "id") String identifier) {
			super(identifier);
		}

		@Override
		public void perform(InnovativeBC agent) {
			// do nothing
		}

		@Override
		public void unperform(InnovativeBC agent) {
			// do nothing
		}

		/**
		 * @see org.volante.abm.institutions.innovation.Innovation#getWaitingBo(org.volante.abm.agent.bt.InnovativeBC)
		 */
		@Override
		public InnovationPa getWaitingBo(InnovativeBC bComp) {
			return null;
		}
	}

	public static class SingletonTestClass {

		@Element
		protected String		innovationID;

		@Element
		protected Innovation	inno;

		protected Innovation	innoAlter;

		public void initialise(Region region) {
			innoAlter = region.getInnovationRegistry().getInnovation(innovationID);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInnovationDeserialisation() {
		persister = runInfo.getPersister();
		try {
			SingletonTestClass tclass = persister.read(SingletonTestClass.class,
 persister.getFullPath(
							SINGLETON_INNOVATION_XML_FILE,
							this.r1.getPersisterContextExtra()));
			tclass.inno.initialise(modelData, runInfo, r1);

			// innoAlter has not been set
			assertEquals(null, tclass.innoAlter);

			assertEquals("TestInnovation", tclass.innovationID);

			// sets inno as innoAlter...
			tclass.initialise(r1);
			assertEquals(tclass.inno, tclass.innoAlter);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testException() throws Exception {
		TestInnovation innovation = new TestInnovation(INNOVATION_ID);
		innovation.initialise(modelData, runInfo, r1);

		persister = runInfo.getPersister();
		SingletonTestClass tclass = persister.read(SingletonTestClass.class,
				persister.getFullPath(SINGLETON_INNOVATION_XML_FILE,
						this.r1.getPersisterContextExtra()));
		tclass.inno.initialise(modelData, runInfo, r1);
		tclass.initialise(r1);
	}
}
