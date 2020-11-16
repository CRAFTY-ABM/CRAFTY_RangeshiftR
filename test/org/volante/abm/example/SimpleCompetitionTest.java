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
 */
package org.volante.abm.example;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SimpleCompetitionTest extends BasicTestsUtils
{
	SimpleCompetitivenessModel comp = new SimpleCompetitivenessModel();
	
	@Test
	public void testCompetitiveness() throws Exception
	{
		//Competitiveness should be the dot product of services provided and the
		//residual demand in a cell
		persister.roundTripSerialise(comp);
		demandR1.setResidual( c11, services( 0, 1, 2, 0 ));
		assertEqualMaps( services( 0, 1, 2, 0 ), demandR1.getResidualDemand( c11 ) );
		assertEquals( 10, comp.getCompetitiveness( demandR1, services( 5, 4, 3, 2 ), c11 ), 0.00001 );
	}
	
	@Test
	public void testRemovingCurrentSupply()
	{
		c11.setSupply( services( 1, 1, 1, 1));
		assertEqualMaps( services( 1, 1, 1, 1 ), c11.getSupply() );
		demandR1.setResidual( c11, services( 0, 1, 2, 0 ));
		assertEqualMaps( services( 0, 1, 2, 0 ), demandR1.getResidualDemand( c11 ) );
		assertEquals( 10, comp.getCompetitiveness( demandR1, services( 5, 4, 3, 2 ), c11 ), 0.00001 );
		
		comp.removeCurrentLevel = true; //Now residual should be (1,2,3,1)
		assertEquals( 1*5 + 2*4 + 3*3 + 1*2, comp.getCompetitiveness( demandR1, services( 5, 4, 3, 2 ), c11 ), 0.00001 );
		
		c11.setSupply( services( 0, 0, 0, 0));
		assertEquals( 0*5 + 1*4 + 2*3 + 0*2, comp.getCompetitiveness( demandR1, services( 5, 4, 3, 2 ), c11 ), 0.00001 );
	}
	
	@Test
	public void testRemovingNegative()
	{
		c11.setSupply( services( 0, 0, 0, 0));
		demandR1.setResidual( c11, services( 1, -1, 2, -3 ));
		assertEquals( 1*5 + -1*4 + 2*3 + -3*2, comp.getCompetitiveness( demandR1, services( 5, 4, 3, 2 ), c11 ), 0.00001 );
		
		comp.removeNegative = true;
		assertEquals( 1*5 + 0*4 + 2*3 + 0*2, comp.getCompetitiveness( demandR1, services( 5, 4, 3, 2 ), c11 ), 0.00001 );
		
	}
	
	@Test
	public void testBothAtOnce()
	{
		c11.setSupply( services( 1, 2, 1, 1));
		demandR1.setResidual( c11, services( 1, -1, 2, -3 ));
		assertEquals( 1*5 + -1*4 + 2*3 + -3*2, comp.getCompetitiveness( demandR1, services( 5, 4, 3, 2 ), c11 ), 0.00001 );
		
		comp.removeNegative = true;
		comp.removeCurrentLevel = true;
		assertEquals( 2*5 + 1*4 + 3*3 + 0*2, comp.getCompetitiveness( demandR1, services( 5, 4, 3, 2 ), c11 ), 0.00001 );
		
	}

}
