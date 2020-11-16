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
 */
package org.volante.abm.example;


import static java.lang.Math.sqrt;
import static org.volante.abm.example.SimpleCapital.HUMAN;
import static org.volante.abm.example.SimpleCapital.NATURAL_CROPS;
import static org.volante.abm.example.SimpleService.FOOD;
import static org.volante.abm.example.SimpleService.HOUSING;
import static org.volante.abm.example.SimpleService.RECREATION;
import static org.volante.abm.example.SimpleService.TIMBER;
import static org.volante.abm.example.SimpleService.simpleServices;

import org.junit.Test;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Service;

import com.moseph.modelutils.fastdata.DoubleMap;


public class SimpleProductionTest extends BasicTestsUtils {
	DoubleMap<Service>		production	= new DoubleMap<Service>(simpleServices);
	DoubleMap<Service>		expected	= new DoubleMap<Service>(simpleServices);
	SimpleProductionModel	fun			= new SimpleProductionModel();

	/*
	 * Capitals: HUMAN(0), INFRASTRUCTURE(1), ECONOMIC(2), NATURAL_GRASSLAND(3), NATURAL_FOREST(4),
	 * NATURAL_CROPS(5), NATURE_VALUE(6)
	 * 
	 * Services: HOUSING(0), TIMBER(1), FOOD(2), RECREATION(3),
	 */

	@Test
	public void testProduction() {
		// fun.initialise( modelData );
		checkProduction("All zeros should be full production", 1, 1, 1, 1);
		fun.setWeight(HUMAN, HOUSING, 1);
		checkProduction("Putting a 1 in the matrix stops production", 0, 1, 1, 1);
		c11.setBaseCapitals(capitals(0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5));
		checkProduction("Giving all baseCapitals 0.5 allows production at 0.5", 0.5, 1, 1, 1);
		fun.setWeight(NATURAL_CROPS, FOOD, 1);
		checkProduction("Setting Natural Crops weight changes food production", 0.5, 1, 0.5, 1);
		fun.setWeight(NATURAL_CROPS, FOOD, 0.5);
		checkProduction("Setting Natural Crops weight changes food production", 0.5, 1, sqrt(0.5),
				1);
		fun.setWeight(FOOD, 5);
		checkProduction("Setting production weights", 0.5, 1, 5 * sqrt(0.5), 1);
	}

	@Test
	public void testProductionForExampleValues() {
		fun = new SimpleProductionModel(extensiveFarmingCapitalWeights,
				extensiveFarmingProductionWeights);
		checkProduction("Setting everything all at once and weighting it", cellCapitalsA,
				extensiveFarmingOnCA);
		checkProduction("Setting everything all at once and weighting it", cellCapitalsB,
				extensiveFarmingOnCB);
		fun = new SimpleProductionModel(forestryCapitalWeights, forestryProductionWeights);
		checkProduction("Setting everything all at once and weighting it", cellCapitalsA,
				forestryOnCA);
		checkProduction("Setting everything all at once and weighting it", cellCapitalsB,
				forestryOnCB);
	}

	@Test
	public void testDeserealisation() throws Exception {
		SimpleProductionModel model = runInfo.getPersister().readXML(SimpleProductionModel.class,
				"xml/LowIntensityArableProduction.xml", null);
		model.initialise(modelData, runInfo, null);
		testLowIntensityArableProduction(model);
	}

	public static void testLowIntensityArableProduction(SimpleProductionModel model) {
		assertEqualMaps(services(0, 0, 4, 0), model.productionWeights);
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 1, 0), model.capitalWeights.getRow(FOOD));
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(TIMBER));
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(HOUSING));
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(RECREATION));
	}

	public static void testHighIntensityArableProduction(SimpleProductionModel model) {
		assertEqualMaps(services(0, 0, 10, 0), model.productionWeights);
		assertEqualMaps(capitals(0.5, 0.5, 0.5, 0, 0, 1, 0), model.capitalWeights.getRow(FOOD));
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(TIMBER));
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(HOUSING));
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(RECREATION));
	}

	public static void testCommercialForestryProduction(SimpleProductionModel model) {
		assertEqualMaps(services(0, 8, 0, 0), model.productionWeights);
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(FOOD));
		assertEqualMaps(capitals(0, 0, 0, 0, 1, 0, 0), model.capitalWeights.getRow(TIMBER));
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(HOUSING));
		assertEqualMaps(capitals(0, 0, 0, 0, 0, 0, 0), model.capitalWeights.getRow(RECREATION));
	}

	void checkProduction(String msg, double... vals) {
		checkProduction(msg, services(vals));
	}

	void checkProduction(String msg, DoubleMap<Capital> cellCapitals, DoubleMap<Service> expected) {
		c11.setBaseCapitals(cellCapitals);
		fun.production(c11, production);
		assertEqualMaps(msg, expected, production);
	}

	void checkProduction(String msg, DoubleMap<Service> expected) {
		fun.production(c11, production);
		assertEqualMaps(msg, expected, production);
	}
}
