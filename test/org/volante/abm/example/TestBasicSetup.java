package org.volante.abm.example;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Sets;

public class TestBasicSetup extends BasicTestsUtils
{

	@Test
	public void testWorldHasRegions()
	{
		assertEquals( "R1 and R2 are in the regions", Sets.newHashSet( w.getRegions() ), regions);
	}

	@Test
	public void testRegionsHaveCells()
	{
		assertEquals("R1 has cells C11..C19", r1.getCells(), r1cells );
		assertEquals("R2 has cells C21..C29", r2.getCells(), r2cells );
	}
}
