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

import static org.volante.abm.agent.Agent.NOT_MANAGED_COMPETITION;
import static org.volante.abm.agent.Agent.NOT_MANAGED_FR_ID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.fr.DefaultFR;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.schedule.DefaultSchedule;
import org.volante.abm.schedule.RunInfo;

/**
 * A simple test of the integrated system. A couple of potential agents,
 * a few cells, setting demand for different services, and making sure that
 * the agents get swapped out as they should
 * @author dmrust
 *
 */
public class IntegratedTest extends BasicTestsUtils
{
	public static double [] farmingProduction = new double[] { 0, 0, 1, 0 };
	public static double [][] farmingCapital = new double[][] {
			{0, 0, 0, 0, 0, 0, 0}, //Housing
			{0, 0, 0, 0, 0, 0, 0}, //Timber
			{0, 0, 0, 0, 0, 1, 0}, //Food
			{0, 0, 0, 0, 0, 0, 0} //Recreation
	};
	
	public static double [] forestProduction = new double[] { 0, 1, 0, 0 };
	public static double [][] forestCapital = new double[][] {
			{0, 0, 0, 0, 0, 0, 0}, //Housing
			{0, 0, 0, 0, 1, 0, 0}, //Timber
			{0, 0, 0, 0, 0, 0, 0}, //Food
			{0, 0, 0, 0, 0, 0, 0} //Recreation
	};

	ProductionModel farmingProdModel = new SimpleProductionModel( farmingCapital, farmingProduction );
	ProductionModel forestProdModel = new SimpleProductionModel( forestCapital, forestProduction );
	
	Cell c1, c2, c3, c4;
	Set<Cell> cells;

	StaticPerCellDemandModel demand;

	DefaultFR farming;
	DefaultFR forest;
	Set<FunctionalRole> fRoles;
	
	DefaultSchedule sched;
	Region r1;
	RegionSet w;

	@Before
	public void setupBasicTestEnvironment() {
		super.setupBasicTestEnvironment();

		farmingProdModel = new SimpleProductionModel(farmingCapital,
				farmingProduction);
		forestProdModel = new SimpleProductionModel(forestCapital,
				forestProduction);

		demand = new StaticPerCellDemandModel();
		c1 = new Cell(1, 1);
		c2 = new Cell(1, 2);
		c3 = new Cell(1, 3);
		c4 = new Cell(1, 4);

		cells = new HashSet<Cell>(Arrays.asList(c1, c2, c3, c4));

		farming = new DefaultFR("Farming", 1, farmingProdModel, 1, 1);
		forest = new DefaultFR("Forest", 2, forestProdModel, 1, 1);

		fRoles = new HashSet<FunctionalRole>(Arrays.asList(farming, forest));

		r1 = new Region(allocation, true, competition, demand, behaviouralTypes,
				fRoles, c1, c2, c3, c4);

		w = new RegionSet(r1);
		try {
			w.initialise(modelData, new RunInfo(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sched = new DefaultSchedule(w);
		sched.register(r1);
	}

	public IntegratedTest() {
	}

	@Test
	public void integratedTest() throws Exception
	{
		((SimpleCompetitivenessModel) competition).setRemoveCurrentLevel(true);
		for( Cell c : cells ) {
			c.setBaseCapitals( capitals( 1, 1, 1, 1, 1, 1, 1 ) );
		}
		sched.initialise( modelData, runInfo, null );
		sched.tick(); // Tick 0
		assertUnmanaged( c1, c2, c3, c4  );
		
		demand.setDemand( c1, services( 0, 0, 10, 0 ));
		demand.updateSupply();
		assertEqualMaps( services(0,0,1,0), farming.getExpectedSupply( c1 ));
		assertEqualMaps( demand.getDemand( c1 ), services(0,0,10,0));
		assertEqualMaps( demand.getResidualDemand( c1 ), services(0,0,10,0));
		sched.tick(); // Tick 1
		print(services(0,0,10,0).prettyPrint());
		assertUnmanaged(  c2, c3, c4  );
		assertFunctionalRole( "Farming", 10, c1 );
		
		demand.setDemand( c1, services( 0, 0, 5, 0 ));
		sched.tick(); // Tick 2
		assertFunctionalRole( "Farming", 5, c1 );

		demand.setDemand( c1, services( 0, 0, 0.5, 0 ));
		sched.tick(); // Tick 3
		assertUnmanaged( c1, c2, c3, c4  );
		assertFunctionalRole(NOT_MANAGED_FR_ID, NOT_MANAGED_COMPETITION, c1);

		demand.setDemand( c1, services( 0, 5, 0, 0 ));
		demand.setDemand( c2, services( 0, 5, 0, 0 ));
		sched.tick(); // Tick 5
		assertUnmanaged(  c3, c4  );
		assertFunctionalRole( "Forest", 5, c1, c2 );
		
		demand.setDemand( c1, services( 0, 0, 5, 0 ));
		sched.tick(); // Tick 6
		assertUnmanaged(  c3, c4  );
		assertFunctionalRole( "Forest", 5, c2 );
		assertFunctionalRole( "Farming", 5, c1 );
	}

}
