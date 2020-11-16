package org.volante.abm.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;


public class IterativeCellSamplerTest extends BasicTestsUtils {

	protected IterativeCellSampler	sampler;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRetrieveAll() {
		this.sampler = new IterativeCellSampler(this.r1.getNumCells(), this.r1.getNumCells(),
				this.r1);
		Set<Integer> sampled = new HashSet<Integer>();
		for (int i = 0; i < this.r1.getNumCells(); i++) {
			sampled.add(sampler.sample());
		}
		assertEquals(sampled.size(), this.r1.getNumCells());
	}

	@Test
	public void testOrder() {
		this.sampler = new IterativeCellSampler(this.r1.getNumCells(), this.r1.getNumCells() / 2,
				this.r1);
		SortedSet<Integer> sampled = new TreeSet<Integer>();
		for (int i = 0; i < this.r1.getNumCells() / 2; i++) {
			sampled.add(sampler.sample());
		}

		int previous = -1;
		for (Integer i : sampled) {
			assertTrue(previous < i);
			previous = i;
		}
		assertEquals(sampled.size(), this.r1.getNumCells() / 2);
	}

	@Test(expected = IllegalStateException.class)
	public void testTryRetrieveMore() {
		this.sampler = new IterativeCellSampler(this.r1.getNumCells(), this.r1.getNumCells() - 1,
				this.r1);
		for (int i = 0; i < this.r1.getNumCells(); i++) {
			sampler.sample();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTryDesireMore() {
		this.sampler = new IterativeCellSampler(this.r1.getNumCells(), this.r1.getNumCells() + 1,
				this.r1);
	}
}
