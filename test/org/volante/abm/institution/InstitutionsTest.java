package org.volante.abm.institution;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.volante.abm.agent.DefaultLandUseAgent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.RegionalDemandModel;
import org.volante.abm.institutions.DefaultInstitution;
import org.volante.abm.institutions.Institutions;

import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

public class InstitutionsTest extends BasicTestsUtils
{

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(InstitutionsTest.class);

	@Test
	public void testBasicIntegration() throws Exception
	{
		logger.info("Test basic integration of institutions");

		c11 = new Cell( 1, 1 );
		c12 = new Cell( 1, 2 );
		c21 = new Cell( 2, 1 );
		c22 = new Cell( 2, 2 );
		assertFalse( c11.isInitialised() );
		
		Region r = setupBasicWorld( c11, c12, c21, c22 );
		Institutions institutions = r.getInstitutions();

		DefaultInstitution a = getTestInstitution(1, 1, r);
		institutions.addInstitution( a );
		
		
		c11.setBaseCapitals( capitals(1,0,0,0,0,0,0) );
		c12.setBaseCapitals( capitals(2,0,0,0,0,0,0) );
		c21.setBaseCapitals( capitals(3,0,0,0,0,0,0) );
		c22.setBaseCapitals( capitals(4,0,0,0,0,0,0) );
		
		assertEqualMaps(capitals(1, 0, 0, 0, 0, 0, 0), c11.getBaseCapitals());
		assertEqualMaps(capitals(2, 0, 0, 0, 0, 0, 0), c12.getBaseCapitals());
		assertEqualMaps(capitals(3, 0, 0, 0, 0, 0, 0), c21.getBaseCapitals());
		assertEqualMaps(capitals(4, 0, 0, 0, 0, 0, 0), c22.getBaseCapitals());

		assertEqualMaps(capitals(1, 0, 0, 0, 0, 0, 0), c11.getEffectiveCapitals());
		assertEqualMaps(capitals(2, 0, 0, 0, 0, 0, 0), c12.getEffectiveCapitals());
		assertEqualMaps(capitals(3, 0, 0, 0, 0, 0, 0), c21.getEffectiveCapitals());
		assertEqualMaps(capitals(4, 0, 0, 0, 0, 0, 0), c22.getEffectiveCapitals());

		runInfo.getSchedule().tick();

		assertEqualMaps( capitals(1,0,0,0,0,0,0), c11.getBaseCapitals() );
		assertEqualMaps( capitals(2,0,0,0,0,0,0), c12.getBaseCapitals() );
		assertEqualMaps( capitals(3,0,0,0,0,0,0), c21.getBaseCapitals() );
		assertEqualMaps( capitals(4,0,0,0,0,0,0), c22.getBaseCapitals() );

		assertEqualMaps( capitals(2,1,1,1,1,1,1), c11.getEffectiveCapitals() );
		assertEqualMaps( capitals(3,1,1,1,1,1,1), c12.getEffectiveCapitals() );
		assertEqualMaps( capitals(4,1,1,1,1,1,1), c21.getEffectiveCapitals() );
		assertEqualMaps( capitals(5,1,1,1,1,1,1), c22.getEffectiveCapitals() );
	}
	
	@Test
	public void testCompetitivenessChanges() throws Exception
	{

		Region r = setupBasicWorld( c11 );
		RegionalDemandModel dem = (RegionalDemandModel) r.getDemandModel();
		dem.setDemand( services(1,1,1,1) );
		c11.setBaseCapitals( cellCapitalsA );
		c11.initEffectiveCapitals();
		double farmComp = r.getCompetitiveness( farmingR1, c11 ); //Get the initial competitiveness
		double forestComp = r.getCompetitiveness( forestryR1, c11 ); //Get the initial competitiveness
		assertTrue( abs( farmComp ) > 0.001 ); //And make sure that its not zero, just to be sure
		
		DefaultInstitution a = getTestInstitution(1, 1, r);
		Institutions inst = r.getInstitutions();
		inst.addInstitution( a );

		assertEquals( farmComp + 1, r.getCompetitiveness( farmingR1, c11 ), 0.001 ); //Check that the competition is adjusted
		assertEquals( forestComp + 1, r.getCompetitiveness( forestryR1, c11 ), 0.001 ); 
		
		a.setSubsidy( farmingR1, 3 );
		assertEquals( farmComp + 3, r.getCompetitiveness( farmingR1, c11 ), 0.001 ); //Check that the competition is adjusted per agent
		assertEquals( forestComp + 1, r.getCompetitiveness( forestryR1, c11 ), 0.001 ); 
	}
	
	@Test
	public void testServiceValuationChanges() throws Exception
	{
		Region r = setupBasicWorld( c11 );
		RegionalDemandModel dem = (RegionalDemandModel) r.getDemandModel();
		dem.setDemand( services(1,1,1,1) );
		c11.setBaseCapitals( cellCapitalsA );
		c11.initEffectiveCapitals();
		double farmComp = r.getCompetitiveness( farmingR1, c11 ); //Get the initial competitiveness
		double forestComp = r.getCompetitiveness( forestryR1, c11 ); //Get the initial competitiveness
		assertTrue( abs( farmComp ) > 0.001 ); //And make sure that its not zero, just to be sure
		UnmodifiableNumberMap<Service> farmSupply = farmingR1
				.getExpectedSupply(c11);
		UnmodifiableNumberMap<Service> forestSupply = forestryR1
				.getExpectedSupply(c11);
		@SuppressWarnings("deprecation")
		double baseComp = r.getCompetitionModel().getCompetitiveness( r.getDemandModel(), farmSupply );
		assertEquals( farmComp, baseComp, 0.0001 );
		
		Institutions inst = r.getInstitutions();
		DefaultInstitution a = getTestInstitution(0, 0, r);
		inst.addInstitution( a );
		
		assertEquals( farmComp, r.getCompetitiveness( farmingR1, c11 ), 0.001 ); //Check that the competition is adjusted
		assertEquals( forestComp, r.getCompetitiveness( forestryR1, c11 ), 0.001 ); 
		
		DoubleMap<Service> subsidies = services(2,2,2,2);
		double farmSubsidy = farmSupply.dotProduct( subsidies );
		double forestSubsidy = forestSupply.dotProduct( subsidies );
		assertTrue( farmSubsidy > 0 );
		assertTrue( forestSubsidy > 0 );
		assertTrue( abs(forestSubsidy-farmSubsidy) > 0 );
		a.setSubsidies( subsidies );
		assertEquals( farmComp + farmSubsidy, r.getCompetitiveness( farmingR1, c11 ), 0.001 ); //Check that the competition is adjusted based on production
		assertEquals( forestComp + forestSubsidy, r.getCompetitiveness( forestryR1, c11 ), 0.001 ); 
	}
	
	@Test
	public void testSerialisedInstitution() throws Exception
	{
		c11 =  new Cell(1,1);
		Region r = setupBasicWorld( c11 );
		r.addfunctionalRoles(Arrays.asList(new FunctionalRole[] { forestryR1,
				farmingR1 }));
		RegionalDemandModel dem = (RegionalDemandModel) r.getDemandModel();
		dem.setDemand( services(1,1,1,1) );
		c11.setBaseCapitals( cellCapitalsA );
		c11.initEffectiveCapitals();
		double farmComp = r.getCompetitiveness( farmingR1, c11 ); //Get the initial competitiveness
		double forestComp = r.getCompetitiveness( forestryR1, c11 ); //Get the initial competitiveness
		assertTrue( abs( farmComp ) > 0.001 ); //And make sure that its not zero, just to be sure
		UnmodifiableNumberMap<Service> farmSupply = farmingR1
				.getExpectedSupply(c11);
		UnmodifiableNumberMap<Service> forestSupply = forestryR1
				.getExpectedSupply(c11);
		@SuppressWarnings("deprecation")
		double baseComp = r.getCompetitionModel().getCompetitiveness( r.getDemandModel(), farmSupply );
		assertEquals( farmComp, baseComp, 0.0001 );
		
		assertEquals( farmComp, r.getCompetitiveness( farmingR1, c11 ), 0.001 ); //Check that the competition is adjusted
		assertEquals( forestComp, r.getCompetitiveness( forestryR1, c11 ), 0.001 ); 
		
		Institutions inst = r.getInstitutions();
		DefaultInstitution a = persister.readXML(DefaultInstitution.class,
				"xml/TestInstitution.xml",
				r.getPersisterContextExtra());
		inst.addInstitution( a );
		
		//Subsidy levels set in the XML file
		DoubleMap<Service> subsidies = services(0,0.7,1.2,0);
		double farmSubsidy = farmSupply.dotProduct( subsidies );
		double forestSubsidy = forestSupply.dotProduct( subsidies );
		assertTrue( farmSubsidy > 0 );
		assertTrue( forestSubsidy > 0 );
		assertTrue( abs(forestSubsidy-farmSubsidy) > 0 );
		//Farming subsidy set in the XML file
		assertEquals( farmComp + farmSubsidy + 3.2, r.getCompetitiveness( farmingR1, c11 ), 0.001 ); //Check that the competition is adjusted based on production
		assertEquals( forestComp + forestSubsidy, r.getCompetitiveness( forestryR1, c11 ), 0.001 ); 
		
		//Capital sub set in XML file
		DoubleMap<Capital> capitals = capitals(0,0,0.2,0,0,0,0);
		cellCapitalsA.addInto( capitals );
		runInfo.getSchedule().tick();
		assertEqualMaps( cellCapitalsA, c11.getBaseCapitals() );
		assertEqualMaps( capitals, c11.getEffectiveCapitals() );
		
		//Figure out expected production with altered capital levels
		DoubleMap<Service> expected = services(0,0,0,0);
		farmingProduction.production( capitals, expected );
		//And check against potential supply
		assertEqualMaps( expected, farmingR1.getExpectedSupply( c11 ));
		
		//And check they're used by the agent
		DefaultLandUseAgent agent = new DefaultLandUseAgent(farmingR1, "TestFarmer", modelData,
				r);
		r.setOwnership(agent, c11);
		agent.supply( c11 );
		assertEqualMaps( expected, c11.getSupply() );
	}
	
	DefaultInstitution getTestInstitution(double cap, double comp, Region r)
			throws Exception
	{
		DefaultInstitution i = new DefaultInstitution();
		i.initialise(modelData, runInfo, r);
		i.setAdjustment( capitals(cap,cap,cap,cap,cap,cap,cap) );
		i.setSubsidy( farmingR1, comp );
		i.setSubsidy( forestryR1, comp );
		return i;
	}

	/*
	 * class TestInstitution extends AbstractInstitution { double capAdjust = 0.1; double compAdjust
	 * = 0.1; Map<PotentialAgent, Double> perAgentAdjust = new HashMap<PotentialAgent, Double>();
	 * DoubleMap<Service> subsidies = services( 0, 0, 0, 0 ); public TestInstitution( double cap,
	 * double comp ) { this.capAdjust = cap; this.compAdjust = comp; }
	 * 
	 * public TestInstitution( double cap, double comp, Map<PotentialAgent,Double> perAgent ) {
	 * this( cap, comp ); perAgentAdjust.putAll( perAgent ); } public void adjustCapitals( Cell c )
	 * { DoubleMap<Capital> adjusted = c.getModifiableEffectiveCapitals(); for( Capital cap :
	 * adjusted.getKeys() ) adjusted.add( cap, capAdjust ); logger.info("Adjusting capitals. \n\t" +
	 * c.getBaseCapitals().prettyPrint() + " \n=>\t" + adjusted.prettyPrint() ); }
	 * 
	 * public double adjustCompetitiveness( PotentialAgent agent, Cell location,
	 * UnmodifiableNumberMap<Service> provision, double competitiveness ) { double subsidy =
	 * provision.dotProduct( subsidies ); competitiveness += subsidy; if(
	 * perAgentAdjust.containsKey( agent ) ) return competitiveness + perAgentAdjust.get( agent );
	 * return competitiveness + compAdjust; }
	 * 
	 * public void setBonus( PotentialAgent a, double l ) { perAgentAdjust.put( a, l ); }
	 * 
	 * public void setSubsidy( Service s, double level ) { subsidies.put( s, level ); }
	 * 
	 * public void setSubsidies( DoubleMap<Service> levels ) { subsidies.copyFrom( levels ); }
	 * 
	 * 
	 * };
	 */
}
