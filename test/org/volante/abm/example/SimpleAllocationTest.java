package org.volante.abm.example;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.collections15.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultLandUseAgent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;

public class SimpleAllocationTest extends BasicTestsUtils
{

	static final String	PROPORTION_ALLOCATION_XML	= "xml/SimpleProportionAllocation.xml";
	static final double	PROPORTION					= 0.3;
	
	
	@Before
	public void setupBasicTestEnvironment() {
		super.setupBasicTestEnvironment();
		r1.clearBehaviouralTypes();
		r1.addBehaviouralTypes(behaviouralTypes);
		r1.clearFunctionalRoles();
		r1.addfunctionalRoles(functionalRolesR1);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testSimpleAllocation() throws Exception
	{
		log.info("Test simple Allocation...");
		log.info(r1.getFunctionalRoles());
		log.info(r2.getFunctionalRoles());
		assertTrue(CollectionUtils.isEqualCollection(functionalRolesR1,
				r1.getFunctionalRoles()));

		allocation = persister.roundTripSerialise( allocation );
		r1.setAvailable( c11 );
		c11.setBaseCapitals( cellCapitalsA );
		assertNotNull( r1.getCompetitiveness( c11 ));
		FunctionalRole ag = r1.getFunctionalRoles().iterator().next();
		assertNotNull( ag );
		print(r1.getCompetitiveness(c11), ag.getExpectedSupply(c11), c11);
		
		assertTrue(r1.getCells().contains(c11));
		assertTrue(demandR1.demand.containsKey(c11));
		assertEquals(demandR1, r1.getDemandModel());

		demandR1.setResidual( c11, services(5, 0, 5, 0) );
		r1.getAllocationModel().allocateLand( r1 );

		// Make sure that demand for food gives a farmer
		assertEquals(farmingR1.getSerialID(), c11.getOwnersFrSerialID());
		print(c11.getOwner().getID());
		
		demandR1.setResidual( c11, services(0, 0, 0, 0) );
		((DefaultLandUseAgent) c11.getOwner()).setProperty(
AgentPropertyIds.GIVING_UP_THRESHOLD, 1.0);
		c11.getOwner().updateCompetitiveness();
		c11.getOwner().considerGivingUp();
		
		assertEquals(Agent.NOT_MANAGED,c11.getOwner());
		
		demandR1.setResidual( c11, services(0, 8, 0, 0) );
		r1.getAllocationModel().allocateLand( r1 );
		// Make sure that demand for food gives a farmer
		assertEquals(forestryR1.getSerialID(), c11.getOwner().getFC().getFR()
				.getSerialID());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testProportionalAllocation() {
		
		persister = runInfo.getPersister();
		try {
			this.allocation = persister.read(SimpleAllocationModel.class,
				persister.getFullPath(PROPORTION_ALLOCATION_XML, this.r1.getPersisterContextExtra()));
			this.allocation.initialise(modelData, runInfo, r1);
			r1.setAllocationModel(this.allocation);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		log.info("Test simple allocation of proportion of available cells...");

		assertTrue(CollectionUtils.isEqualCollection(functionalRolesR1, r1.getFunctionalRoles()));

		int numCellsTotal = r1.getNumCells();
		for (Cell c : r1.getAllCells()) {
			c.setBaseCapitals(cellCapitalsA);
			r1.setAvailable(c);
			demandR1.setResidual(c, services(0, 8, 0, 0));
		}
		assertEquals(numCellsTotal, r1.getAvailable().size());
		
		r1.getAllocationModel().allocateLand(r1);
		assertEquals((int) Math.ceil(numCellsTotal * (1 - PROPORTION)), r1.getAvailable().size());
	}
}
