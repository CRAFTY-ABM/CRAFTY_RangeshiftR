package org.volante.abm.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.volante.abm.example.BasicTestsUtils;

import com.moseph.modelutils.fastdata.DoubleMap;

public class CellTests extends BasicTestsUtils
{

	@Test
	public void testSettingSupply()
	{
		assertEquals( 4, modelData.services.size());
		DoubleMap<Service> dem = services( 3,4,5,6);
		c11.setSupply( dem );
		assertEqualMaps( services( 3,4,5,6 ), c11.getSupply() );
	}
	@Test
	public void testSettingCapitals()
	{
		assertEquals( 7, modelData.capitals.size());
		DoubleMap<Capital> cap = capitals( 1,2, 3,4,5,6,7);
		c11.setBaseCapitals( cap );
		assertEqualMaps( capitals( 1,2,3,4,5,6,7 ), c11.getEffectiveCapitals() );
	}
	

}
