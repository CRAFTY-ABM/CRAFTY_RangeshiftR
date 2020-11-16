package org.volante.abm.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.assembler.AgentAssembler;
import org.volante.abm.agent.assembler.DefaultAgentAssembler;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.schedule.Schedule;

public class DefaultAgentTest extends BasicTestsUtils
{

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(DefaultAgentTest.class);

	DefaultLandUseAgent farmer;
	AgentAssembler assembler;

	@Before
	public void setupAgent() throws Exception
	{
		assembler = new DefaultAgentAssembler();
		assembler.initialise(modelData, runInfo, r1);

		farmer = (DefaultLandUseAgent) assembler.assembleAgent(c11, "Cognitor",
				"C_Cereal");
		r1.setOwnership(farmer, c11);
		farmer.setProductionFunction(farmingProduction);
	}

	@Test
	public void testProduction()
	{
		demandR1.setResidual( c11, services(1,1,1,1) );
		c11.setBaseCapitals( cellCapitalsA );
		farmer.updateSupply();
		assertEqualMaps( "Extensive farming production is correct", extensiveFarmingOnCA, farmer.supply( c11 ));
		farmer.updateCompetitiveness();
		assertEquals(extensiveFarmingOnCA.getTotal(),
				farmer.getProperty(AgentPropertyIds.COMPETITIVENESS), 0.0001);
		c11.setBaseCapitals( cellCapitalsB );
		farmer.updateSupply();
		assertEqualMaps( "Extensive farming production is correct", extensiveFarmingOnCB, farmer.supply( c11 ));
		farmer.updateCompetitiveness();
		assertEquals(extensiveFarmingOnCB.getTotal(),
				farmer.getProperty(AgentPropertyIds.COMPETITIVENESS), 0.0001);
		demandR1.setResidual( c11, services(2,2,2,2) );
		farmer.updateSupply();
		assertEqualMaps( "Extensive farming production is correct", extensiveFarmingOnCB, farmer.supply( c11 ));
		farmer.updateCompetitiveness();
		assertEquals(2 * extensiveFarmingOnCB.getTotal(),
				farmer.getProperty(AgentPropertyIds.COMPETITIVENESS), 0.0001);
	}
	
	@Test
	public void testGivingUp()
	{
		demandR1.setResidual( c11, services(1,1,1,1) );
		c11.setBaseCapitals( cellCapitalsA );
		//Set the giving up threshold to a bit less than production
		farmer.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD,
				extensiveFarmingOnCA.getTotal() - 0.01);
		farmer.updateSupply();
		farmer.updateCompetitiveness();
		farmer.considerGivingUp();
		//Should still be there
		assertTrue( r1.getAgents().contains( farmer ));
		
		//Set the giving up threshold to a bit more than production
		farmer.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD,
				extensiveFarmingOnCA.getTotal() + 0.01);
		farmer.updateSupply();
		farmer.updateCompetitiveness();
		farmer.considerGivingUp();
		//And now we should have given up
		assertFalse( r1.getAgents().contains( farmer ));
	}
	
	@Test
	public void testAging()
	{
		farmer.setProperty(AgentPropertyIds.AGE, 20.0);
		assertEquals(20, farmer.getProperty(AgentPropertyIds.AGE).intValue());
		farmer.tickStartUpdate();
		assertEquals(21, farmer.getProperty(AgentPropertyIds.AGE).intValue());
	}
	
	@Test
	public void testRealAging() throws Exception
	{
		Cell c = new Cell(1, 1);
		Region r = setupBasicWorld(c);

		r.setOwnership(farmer, c);

		// Make sure he doesn't give up!
		farmer.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD, -5.0);
		farmer.setProperty(AgentPropertyIds.AGE, 20.0);

		for (Agent a : r.getAllAllocatedAgents()) {
			logger.info("Agent: " + a );
		}
		
		Schedule s = runInfo.getSchedule();
		s.setRegions(new RegionSet(r));

		assertEquals(20, farmer.getProperty(AgentPropertyIds.AGE).intValue());
		s.tick();
		assertEquals(21, farmer.getProperty(AgentPropertyIds.AGE).intValue());
		s.tick();
		assertEquals(22, farmer.getProperty(AgentPropertyIds.AGE).intValue());

		assembler = new DefaultAgentAssembler();
		assembler.initialise(modelData, runInfo, r);
		farmer = (DefaultLandUseAgent) assembler.assembleAgent(c, "Cognitor",
				"C_Cereal");

		farmer.setProperty(AgentPropertyIds.GIVING_UP_THRESHOLD, -5.0);
		farmer.setProperty(AgentPropertyIds.AGE, 10.0);
		
		assertEquals(10, farmer.getProperty(AgentPropertyIds.AGE).intValue());
		s.tick();
		assertEquals(11, farmer.getProperty(AgentPropertyIds.AGE).intValue());
	}
}
