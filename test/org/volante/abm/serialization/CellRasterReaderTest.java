package org.volante.abm.serialization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.SimpleCapital;
import org.volante.abm.schedule.RunInfo;

public class CellRasterReaderTest extends BasicTestsUtils
{

	private RegionLoader rl;

	@Test
	public void testBasicOperation() throws Exception
	{
		CellRasterReader crr = new CellRasterReader();
		crr.rasterFile = "raster/testMap.asc";
		checkHumanCapitalReading( crr );
	}
	
	@Test
	public void testLoading() throws Exception
	{
		CellRasterReader crr = runInfo.getPersister().readXML(CellRasterReader.class,
				"xml/HumanCapitalRasterReader.xml", null);
		checkHumanCapitalReading( crr );
	}
	
	/**
	 * Utility method: puts the reader into a RegionLoader, and initialises
	 * then checks against the values in raster/testMap.asc
	 * @param crr
	 * @throws Exception
	 */
	void checkHumanCapitalReading( CellRasterReader crr ) throws Exception
	{
		rl = new RegionLoader();
		rl.setDefaults();
		rl.cellInitialisers.add( crr );
		rl.initialise( new RunInfo() );
		
		checkCell( 4, 8, 0.1);
		checkCell( 4, 13, 0.6);
		checkCell( 6, 8, 2.1);
		checkCell( 6, 13, 2.6);
	}
	void checkCell( int x, int y, double capital )
	{
		assertEquals( capital, 
				rl.getCell( x, y ).getEffectiveCapitals().getDouble( SimpleCapital.HUMAN ), 0.0001 );
	}

}
