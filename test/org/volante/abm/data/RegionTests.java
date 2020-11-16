package org.volante.abm.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections15.CollectionUtils;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.example.BasicTestsUtils;

public class RegionTests extends BasicTestsUtils
{

	@Test
	public void testOwnership()
	{
		r1.setOwnership( a1, c11,c12 );
		assertEquals( a1, c11.getOwner() );
		assertEquals( a1, c12.getOwner() );
		assertEquals( Agent.NOT_MANAGED, c13.getOwner() );
		checkSet( r1.allocatedAgents, a1);
		
		r1.setOwnership( a2,  c12, c13 );
		assertEquals( a1, c11.getOwner() );
		assertEquals( a2, c12.getOwner() );
		assertEquals( a2, c13.getOwner() );
		checkSet( r1.allocatedAgents, a1, a2 );
		
		r1.setOwnership( a2,  c11 );
		assertEquals( a2, c11.getOwner() );
		assertEquals( a2, c12.getOwner() );
		assertEquals( a2, c13.getOwner() );
		checkSet( r1.allocatedAgents, a2 );
	}

	@Test
	public void testGetAdjacentCells() {
		Cell c31 = new Cell(3, 1);
		Cell c32 = new Cell(3, 2);
		Cell c33 = new Cell(3, 3);
		Cell c34 = new Cell(3, 4);

		r1.addCell(c21);
		r1.addCell(c22);
		r1.addCell(c23);
		r1.addCell(c24);
		r1.addCell(c25);
		r1.addCell(c26);
		r1.addCell(c27);
		r1.addCell(c28);
		r1.addCell(c29);

		r1.addCell(c31);
		r1.addCell(c32);
		r1.addCell(c33);
		r1.addCell(c34);

		Set<Cell> adjacent = new HashSet<Cell>();

		// 11 12 13 14 15 16 17 18 19
		// 21 22 23 24 25 26 27 28 29
		// 31 32 33 34

		// adjacent to 21
		adjacent.add(c11);
		adjacent.add(c12);
		adjacent.add(c22);
		adjacent.add(c31);
		adjacent.add(c32);
		assertTrue(CollectionUtils.isEqualCollection(adjacent,
				r1.getAdjacentCells(c21)));

		// adjacent to 23
		adjacent.clear();
		adjacent.add(c12);
		adjacent.add(c13);
		adjacent.add(c14);
		adjacent.add(c22);
		adjacent.add(c24);
		adjacent.add(c32);
		adjacent.add(c33);
		adjacent.add(c34);
		assertTrue(CollectionUtils.isEqualCollection(adjacent,
				r1.getAdjacentCells(c23)));

		// adjacent to 19
		adjacent.clear();
		adjacent.add(c18);
		adjacent.add(c28);
		adjacent.add(c29);
		assertTrue(CollectionUtils.isEqualCollection(adjacent,
				r1.getAdjacentCells(c19)));
	}
}
