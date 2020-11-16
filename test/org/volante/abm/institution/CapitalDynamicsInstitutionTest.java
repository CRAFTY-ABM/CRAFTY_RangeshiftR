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
 * Created by Sascha Holzhauer on 24 Sep 2014
 */
package org.volante.abm.institution;

import static org.junit.Assert.assertFalse;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.institutions.CapitalDynamicsInstitution;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;

/**
 * @author Sascha Holzhauer
 *
 */
public class CapitalDynamicsInstitutionTest extends BasicTestsUtils {

	private static final String	XML_FILENAME	= "xml/Institutions_CapitalDynamics.xml";

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(CapitalDynamicsInstitutionTest.class);

	Region						r				= null;

	/**
	 * @throws Exception
	 */
	protected CapitalDynamicsInstitution getTestInstitution() throws Exception
	{
		CapitalDynamicsInstitution i = ABMPersister.getInstance().readXML(
				CapitalDynamicsInstitution.class, XML_FILENAME, r.getPersisterContextExtra());
		i.initialise(modelData, runInfo, r);
		return i;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		logger.info("Test basic integration of institutions");

		c11 = new Cell(1, 1);
		c12 = new Cell(1, 2);
		c21 = new Cell(2, 1);
		c22 = new Cell(2, 2);
		assertFalse(c11.isInitialised());

		r = setupBasicWorld(c11, c12, c21, c22);
		Institutions institutions = r.getInstitutions();
		CapitalDynamicsInstitution a = getTestInstitution();
		institutions.addInstitution(a);

		c11.setBaseCapitals(capitals(1, 1, 1, 1, 1, 1, 1));
		c12.setBaseCapitals(capitals(0, 0, 0, 0, 0, 0, 0));
		c21.setBaseCapitals(capitals(2, 2, 2, 2, 2, 2, 2));
		c22.setBaseCapitals(capitals(8, 8, 8, 0, 8, 8, 8));
	}

	/**
	 * Test method for
	 * {@link org.volante.abm.institutions.CapitalDynamicsInstitution#adjustCapitals(org.volante.abm.data.Cell)}
	 * .
	 */
	@Test
	public void testAdjustCapitals() {
		runInfo.getSchedule().tick();
		assertEqualMaps(capitals(1, 1, 1, 1, 1, 1, 1), c11.getBaseCapitals());
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), c12.getBaseCapitals());
		assertEqualMaps(capitals(2, 2, 2, 2, 2, 2, 2), c21.getBaseCapitals());
		assertEqualMaps(capitals(8, 8, 8, 0, 8, 8, 8), c22.getBaseCapitals());

		assertEqualMaps(capitals(1, 1, 1.2, 1, 1, 0.8, 1), c11.getEffectiveCapitals());
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), c12.getEffectiveCapitals());
		assertEqualMaps(capitals(2, 2, 2.4, 2, 2, 1.6, 2), c21.getEffectiveCapitals());
		assertEqualMaps(capitals(8, 8, 9.6, 0, 8, 6.4, 8), c22.getEffectiveCapitals());

		runInfo.getSchedule().tick();
		assertEqualMaps(capitals(1, 1, 1, 1, 1, 1, 1), c11.getBaseCapitals());
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), c12.getBaseCapitals());
		assertEqualMaps(capitals(2, 2, 2, 2, 2, 2, 2), c21.getBaseCapitals());
		assertEqualMaps(capitals(8, 8, 8, 0, 8, 8, 8), c22.getBaseCapitals());

		assertEqualMaps(capitals(1.2, 1, 1, 1, 1, 0.8, 1), c11.getEffectiveCapitals());
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), c12.getEffectiveCapitals());
		assertEqualMaps(capitals(2.4, 2, 2, 2, 2, 1.6, 2), c21.getEffectiveCapitals());
		assertEqualMaps(capitals(9.6, 8, 8, 0, 8, 6.4, 8), c22.getEffectiveCapitals());

		runInfo.getSchedule().tick();
		assertEqualMaps(capitals(1, 1, 1, 1, 1, 1, 1), c11.getBaseCapitals());
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), c12.getBaseCapitals());
		assertEqualMaps(capitals(2, 2, 2, 2, 2, 2, 2), c21.getBaseCapitals());
		assertEqualMaps(capitals(8, 8, 8, 0, 8, 8, 8), c22.getBaseCapitals());

		assertEqualMaps(capitals(1.4, 1, 1, 1, 1, 0.8, 1),
				c11.getEffectiveCapitals());
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0),
				c12.getEffectiveCapitals());
		assertEqualMaps(capitals(2.8, 2, 2, 2, 2, 1.6, 2),
				c21.getEffectiveCapitals());
		assertEqualMaps(capitals(11.2, 8, 8, 0, 8, 6.4, 8),
				c22.getEffectiveCapitals());
	}

	@Test
	public void testAdjustCapitalsBeforeAllocation() {
		r.setAllocationModel(new AllocationModel() {

			@Override
			public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
			}

			@Override
			public void allocateLand(Region r) {
				// check whether the institution has already adapted capital levels:
				assertEqualMaps(capitals(1, 1, 1, 1, 1, 1, 1), c11.getBaseCapitals());
				assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), c12.getBaseCapitals());
				assertEqualMaps(capitals(2, 2, 2, 2, 2, 2, 2), c21.getBaseCapitals());
				assertEqualMaps(capitals(8, 8, 8, 0, 8, 8, 8), c22.getBaseCapitals());

				assertEqualMaps(capitals(1, 1, 1.2, 1, 1, 0.8, 1), c11.getEffectiveCapitals());
				assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), c12.getEffectiveCapitals());
				assertEqualMaps(capitals(2, 2, 2.4, 2, 2, 1.6, 2), c21.getEffectiveCapitals());
				assertEqualMaps(capitals(8, 8, 9.6, 0, 8, 6.4, 8), c22.getEffectiveCapitals());
			}

			@Override
			public AllocationDisplay getDisplay() {
				return null;
			}

		});
		runInfo.getSchedule().tick();
	}
}
