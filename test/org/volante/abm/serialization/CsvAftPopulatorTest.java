/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 28 Mar 2015
 */
package org.volante.abm.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.data.Cell;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.example.BasicTestsUtils;
import org.volante.abm.example.SimpleCapital;

/**
 * @author Sascha Holzhauer
 *
 */
public class CsvAftPopulatorTest extends BasicTestsUtils {

	static class CellIdentifier {
		int x, y;

		protected CellIdentifier(int x, int y) {
			this.x = x;
			this.y = y;
		}

		protected CellIdentifier(Cell cell) {
			this(cell.getX(), cell.getY());
		}

		public boolean equals(Object o) {
			if (o instanceof CellIdentifier) {
				return ((CellIdentifier)o).x == this.x && ((CellIdentifier)o).y == this.y; 
			} else {
				return false;
			}
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return this.x * 10000 + this.y;
		}
	}

	protected RegionLoader rl;

	static final double[][] CAPITALS_INFRASTRUCTURE = { { .7, .5 }, { .2, 0 } };

	// SingleCell Agents (an agent manages only one cell)
	static final String FILENAME_XML_POPULATOR_SINGLE_CELL_AGENTS = "./xml/CsvAftPopulator_SingleCellAgents.xml";

	static final int NUM_AGENTS_SINGLE_CELL_AGENTS = 4;
	static final Map<String, Cell> HOME_CELLS_SINGLE_CELL_AGENTS = new HashMap<String, Cell>();
	static {
		HOME_CELLS_SINGLE_CELL_AGENTS.put("Agent1", new Cell(1, 1));
		HOME_CELLS_SINGLE_CELL_AGENTS.put("Agent2", new Cell(1, 2));
		HOME_CELLS_SINGLE_CELL_AGENTS.put("Agent3", new Cell(2, 1));
		HOME_CELLS_SINGLE_CELL_AGENTS.put("Agent4", new Cell(2, 2));
	}
	static final Map<CellIdentifier, String> CELL_OWNERS_SINGLE_CELL_AGENTS = new HashMap<CellIdentifier, String>();
	static {
		// default agentIDs since not given in CSV file
		CELL_OWNERS_SINGLE_CELL_AGENTS.put(new CellIdentifier(1, 1), "Agent_0");
		CELL_OWNERS_SINGLE_CELL_AGENTS.put(new CellIdentifier(1, 2), "Agent_1");
		CELL_OWNERS_SINGLE_CELL_AGENTS.put(new CellIdentifier(2, 1), "Agent_2");
		CELL_OWNERS_SINGLE_CELL_AGENTS.put(new CellIdentifier(2, 2), "Agent_3");
	}
	static final Map<String, Set<Cell>> MANAGED_CELLS_SINGLE_CELL_AGENTS = new HashMap<String, Set<Cell>>();


	// Manage every home cell
	static final String FILENAME_XML_POPULATOR_MANAGE_EVERY_HOMECELL = "./xml/CsvAftPopulator_ManageEveryHomeCell.xml";
	static final int NUM_AGENTS_MANAGE_EVERY_HOMECELL = 2;
	static final Map<String, Cell> HOME_CELLS_MANAGE_EVERY_HOMECELL = new HashMap<String, Cell>();
	static {
		HOME_CELLS_MANAGE_EVERY_HOMECELL.put("Agent1", new Cell(1, 1));
		HOME_CELLS_MANAGE_EVERY_HOMECELL.put("Agent2", new Cell(2, 1));
	}
	static final Map<CellIdentifier, String> CELL_OWNERS_MANAGE_EVERY_HOMECELL = new HashMap<CellIdentifier, String>();
	static {
		CELL_OWNERS_MANAGE_EVERY_HOMECELL.put(new CellIdentifier(1, 1),
				"Agent1");
		CELL_OWNERS_MANAGE_EVERY_HOMECELL.put(new CellIdentifier(1, 2),
				"Agent1");
		CELL_OWNERS_MANAGE_EVERY_HOMECELL.put(new CellIdentifier(2, 1),
				"Agent2");
		CELL_OWNERS_MANAGE_EVERY_HOMECELL.put(new CellIdentifier(2, 2),
				"Agent2");
	}
	static final Map<String, Set<Cell>> MANAGED_CELLS_MANAGE_EVERY_HOMECELL = new HashMap<String, Set<Cell>>();

	// Unmanaged home cell
	static final String FILENAME_XML_POPULATOR_UNMANAGED_HOMECELL = "./xml/CsvAftPopulator_UnmanagedHomeCell.xml";
	static final int NUM_AGENTS_UNMANAGED_HOMECELL = 2;
	static final Map<String, Cell> HOME_CELLS_UNMANAGED_HOMECELL = new HashMap<String, Cell>();
	static {
		HOME_CELLS_UNMANAGED_HOMECELL.put("Agent1", new Cell(1, 1));
		HOME_CELLS_UNMANAGED_HOMECELL.put("Agent2", new Cell(2, 1));
	}
	static final Map<CellIdentifier, String> CELL_OWNERS_UNMANAGED_HOMECELL = new HashMap<CellIdentifier, String>();
	static {
		CELL_OWNERS_UNMANAGED_HOMECELL.put(new CellIdentifier(1, 1),
				Agent.NOT_MANAGED_AGENT_ID);
		CELL_OWNERS_UNMANAGED_HOMECELL.put(new CellIdentifier(1, 2), "Agent1");
		CELL_OWNERS_UNMANAGED_HOMECELL.put(new CellIdentifier(2, 1),
				Agent.NOT_MANAGED_AGENT_ID);
		CELL_OWNERS_UNMANAGED_HOMECELL.put(new CellIdentifier(2, 2), "Agent2");
	}
	static final Map<String, Set<Cell>> MANAGED_CELLS_UNMANAGED_HOMECELL = new HashMap<String, Set<Cell>>();

	// Tagged home cells
	static final String FILENAME_XML_POPULATOR_TAGGED_HOMECELLS = "./xml/CsvAftPopulator_TaggedHomeCells.xml";
	static final int NUM_AGENTS_TAGGED_HOMECELLS = 2;
	static final Map<String, Cell> HOME_CELLS_TAGGED_HOMECELLS = new HashMap<String, Cell>();
	static {
		HOME_CELLS_TAGGED_HOMECELLS.put("Agent1", new Cell(1, 1));
		HOME_CELLS_TAGGED_HOMECELLS.put("Agent2", new Cell(2, 1));
	}
	static final Map<CellIdentifier, String> CELL_OWNERS_TAGGED_HOMECELLS = new HashMap<CellIdentifier, String>();
	static {
		CELL_OWNERS_TAGGED_HOMECELLS.put(new CellIdentifier(1, 1),
				Agent.NOT_MANAGED_AGENT_ID);
		CELL_OWNERS_TAGGED_HOMECELLS.put(new CellIdentifier(1, 2), "Agent1");
		CELL_OWNERS_TAGGED_HOMECELLS.put(new CellIdentifier(2, 1), "Agent2");
		CELL_OWNERS_TAGGED_HOMECELLS.put(new CellIdentifier(2, 2), "Agent2");
	}
	static final Map<String, Set<Cell>> MANAGED_CELLS_TAGGED_HOMECELLS = new HashMap<String, Set<Cell>>();

	// SingleCell Agents (an agent manages only one cell) unmanaged
	static final String FILENAME_XML_POPULATOR_SINGLE_CELL_AGENTS_UNMANAGED = "./xml/CsvAftPopulator_SingleCellAgentsUnmanaged.xml";

	static final int NUM_AGENTS_SINGLE_CELL_AGENTS_UNMANAGED = 0;

	static final Map<CellIdentifier, String> CELL_OWNERS_SINGLE_CELL_AGENTS_UNMANAGED = new HashMap<CellIdentifier, String>();
	static {
		// default agentIDs since not given in CSV file
		CELL_OWNERS_SINGLE_CELL_AGENTS_UNMANAGED.put(new CellIdentifier(1, 1),
				Agent.NOT_MANAGED_AGENT_ID);
		CELL_OWNERS_SINGLE_CELL_AGENTS_UNMANAGED.put(new CellIdentifier(1, 2),
				Agent.NOT_MANAGED_AGENT_ID);
		CELL_OWNERS_SINGLE_CELL_AGENTS_UNMANAGED.put(new CellIdentifier(2, 1),
				Agent.NOT_MANAGED_AGENT_ID);
		CELL_OWNERS_SINGLE_CELL_AGENTS_UNMANAGED.put(new CellIdentifier(2, 2),
				Agent.NOT_MANAGED_AGENT_ID);
	}
	static final Map<String, Set<Cell>> MANAGED_CELLS_SINGLE_CELL_AGENTS_UNMANAGED = new HashMap<String, Set<Cell>>();

	static final String FILENAME_XML_POPULATOR_UNMANAGED_HOMECELL_PROPERTIES =
			"./xml/CsvAftPopulator_UnmanagedHomeCell_Properties.xml";
	static final Map<String, Integer> AGENT_AGES = new HashMap<String, Integer>(2);
	static {
		AGENT_AGES.put("Agent1", 23);
		AGENT_AGES.put("Agent2", 50);
	}
	static final Map<String, Double> AGENT_FARMSIZES = new HashMap<String, Double>(2);
	static {
		AGENT_FARMSIZES.put("Agent1", 2.0);
		AGENT_FARMSIZES.put("Agent2", 330.2);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		rl = new RegionLoader();
		rl.setDefaults();
		rl.initialise(runInfo);
		rl.region = setupBasicWorld();
	}

	/**
	 * 
	 */
	protected void fillManagedCellsMap(Map<CellIdentifier, String> cellOwners,
			Map<String, Set<Cell>> managedCells) {
		for (Entry<CellIdentifier, String> e : cellOwners
				.entrySet()) {
			if (!managedCells.containsKey(e.getValue())) {
				managedCells.put(e.getValue(),
						new HashSet<Cell>());
			}
			managedCells.get(e.getValue()).add(
					new Cell(e.getKey().x, e.getKey().y));
		}
	}

	@Test
	public void testSingleCellAgents() throws Exception {
		fillManagedCellsMap(CELL_OWNERS_SINGLE_CELL_AGENTS,
				MANAGED_CELLS_SINGLE_CELL_AGENTS);
		CsvAftPopulator populator = runInfo.getPersister().readXML(
				CsvAftPopulator.class,
				FILENAME_XML_POPULATOR_SINGLE_CELL_AGENTS,
				rl.getRegion().getPersisterContextExtra());
		populator.initialise(rl);

		// check capitals
		checkInfrastructureCapitalReading();

		// check number of agents
		assertEquals("Number of agents", NUM_AGENTS_SINGLE_CELL_AGENTS,
 rl.region.getAgents().size());

		// check agent home cells
		checkHomeCells(HOME_CELLS_SINGLE_CELL_AGENTS);

		// check cell's owners
		checkCellOwners(CELL_OWNERS_SINGLE_CELL_AGENTS);

		// check agent's managed cells
		checkManagedCells(MANAGED_CELLS_SINGLE_CELL_AGENTS);
	}

	@Test
	public void testManageEveryCell() throws Exception {
		fillManagedCellsMap(CELL_OWNERS_MANAGE_EVERY_HOMECELL,
				MANAGED_CELLS_MANAGE_EVERY_HOMECELL);
		CsvAftPopulator populator = runInfo.getPersister().readXML(CsvAftPopulator.class,
				FILENAME_XML_POPULATOR_MANAGE_EVERY_HOMECELL,
				rl.getRegion().getPersisterContextExtra());
		populator.initialise(rl);

		// check capitals
		checkInfrastructureCapitalReading();
		
		// check number of agents
		assertEquals("Number of agents", NUM_AGENTS_MANAGE_EVERY_HOMECELL,
 rl.region.getAgents().size());
		
		// check agent home cells
		checkHomeCells(HOME_CELLS_MANAGE_EVERY_HOMECELL);
		
		// check cell's owners
		checkCellOwners(CELL_OWNERS_MANAGE_EVERY_HOMECELL);
		
		// check agent's managed cells
		checkManagedCells(MANAGED_CELLS_MANAGE_EVERY_HOMECELL);
	}

	@Test
	public void testUnmanagedHomeCells() throws Exception {
		fillManagedCellsMap(CELL_OWNERS_UNMANAGED_HOMECELL,
				MANAGED_CELLS_UNMANAGED_HOMECELL);
		CsvAftPopulator populator = runInfo.getPersister().readXML(
				CsvAftPopulator.class,
				FILENAME_XML_POPULATOR_UNMANAGED_HOMECELL,
				rl.getRegion().getPersisterContextExtra());
		populator.initialise(rl);

		// check capitals
		checkInfrastructureCapitalReading();

		// check number of agents
		assertEquals("Number of agents", NUM_AGENTS_UNMANAGED_HOMECELL,
 rl.region.getAgents().size());

		// check agent home cells
		checkHomeCells(HOME_CELLS_UNMANAGED_HOMECELL);

		// check cell's owners
		checkCellOwners(CELL_OWNERS_UNMANAGED_HOMECELL);

		// check agent's managed cells
		checkManagedCells(MANAGED_CELLS_UNMANAGED_HOMECELL);
	}

	@Test
	public void testUnmanagedHomeCellsWithAge() throws Exception {
		fillManagedCellsMap(CELL_OWNERS_UNMANAGED_HOMECELL,
				MANAGED_CELLS_UNMANAGED_HOMECELL);
		CsvAftPopulator populator = runInfo.getPersister().readXML(
				CsvAftPopulator.class,
 FILENAME_XML_POPULATOR_UNMANAGED_HOMECELL_PROPERTIES,
				rl.getRegion().getPersisterContextExtra());
		populator.initialise(rl);

		// check capitals
		checkInfrastructureCapitalReading();

		// check number of agents
		assertEquals("Number of agents", NUM_AGENTS_UNMANAGED_HOMECELL,
 rl.region.getAgents().size());

		// check agent home cells
		checkHomeCells(HOME_CELLS_UNMANAGED_HOMECELL);

		// check cell's owners
		checkCellOwners(CELL_OWNERS_UNMANAGED_HOMECELL);

		// check agent's managed cells
		checkManagedCells(MANAGED_CELLS_UNMANAGED_HOMECELL);

		// check agent properties
		for (Agent agent : rl.region.getAllAllocatedAgents()) {
			assertEquals("Check AGE property", AGENT_AGES.get(agent.getID()).doubleValue(),
					agent
					.getProperty(AgentPropertyIds.AGE), 0.001);
		}
		for (Agent agent : rl.region.getAllAllocatedAgents()) {
			assertEquals("Check FARM_SIZE property", AGENT_FARMSIZES.get(agent.getID()).doubleValue(),
					agent.getProperty(AgentPropertyIds.FARM_SIZE), 0.001);
		}
	}

	@Test
	public void testTaggedHomeCells() throws Exception {
		fillManagedCellsMap(CELL_OWNERS_TAGGED_HOMECELLS,
				MANAGED_CELLS_TAGGED_HOMECELLS);
		CsvAftPopulator populator = runInfo.getPersister().readXML(
				CsvAftPopulator.class, FILENAME_XML_POPULATOR_TAGGED_HOMECELLS,
				rl.getRegion().getPersisterContextExtra());
		populator.initialise(rl);

		// check capitals
		checkInfrastructureCapitalReading();

		// check number of agents
		assertEquals("Number of agents", NUM_AGENTS_TAGGED_HOMECELLS, rl.region
.getAgents().size());

		// check agent home cells
		checkHomeCells(HOME_CELLS_TAGGED_HOMECELLS);

		// check cell's owners
		checkCellOwners(CELL_OWNERS_TAGGED_HOMECELLS);

		// check agent's managed cells
		checkManagedCells(MANAGED_CELLS_TAGGED_HOMECELLS);
	}

	@Test
	public void testSingleCellUnmanaged() throws Exception {
		fillManagedCellsMap(CELL_OWNERS_SINGLE_CELL_AGENTS_UNMANAGED,
				MANAGED_CELLS_SINGLE_CELL_AGENTS_UNMANAGED);
		CsvAftPopulator populator = runInfo.getPersister().readXML(
				CsvAftPopulator.class,
				FILENAME_XML_POPULATOR_SINGLE_CELL_AGENTS_UNMANAGED,
				rl.getRegion().getPersisterContextExtra());
		populator.initialise(rl);

		// check capitals
		checkInfrastructureCapitalReading();

		// check number of agents
		assertEquals("Number of agents",
				NUM_AGENTS_SINGLE_CELL_AGENTS_UNMANAGED,
 rl.region.getAgents().size());

		// check agent home cells
		checkHomeCells(HOME_CELLS_SINGLE_CELL_AGENTS);

		// check cell's owners
		checkCellOwners(CELL_OWNERS_SINGLE_CELL_AGENTS_UNMANAGED);

		// check agent's managed cells
		checkManagedCells(MANAGED_CELLS_SINGLE_CELL_AGENTS_UNMANAGED);
	}

	/**
	 * 
	 */
	protected void checkHomeCells(Map<String, Cell> homeCells) {
		for (Agent agent : this.r1.getAgents()) {
			assertEquals("Check home cell X coord", homeCells
					.get(agent.getID()).getX(), agent.getHomeCell().getX());
			assertEquals("Check home cell Y coord", homeCells
					.get(agent.getID()).getY(), agent.getHomeCell().getY());
		}
	}

	/**
	 * 
	 */
	protected void checkCellOwners(Map<CellIdentifier, String> cellOwners) {
		for (Cell cell : rl.region.getAllCells()) {
			assertEquals("Check Owner",
					cellOwners.get(new CellIdentifier(cell)), cell.getOwner()
					.getID());
		}
	}

	/**
	 * 
	 */
	protected void checkManagedCells(Map<String, Set<Cell>> managedCells) {
		for (LandUseAgent agent : this.r1.getAgents()) {
			for (Cell cell : agent.getCells()) {
				assertTrue("Check if cell shall be managed by the given agent",
						managedCells.get(agent.getID()).contains(cell));
			}
		}
	}


	/**
	 * Utility method: puts the reader into a RegionLoader, and initialises then
	 * checks against the values in raster/testMap.asc
	 * 
	 * @param populator
	 * @throws Exception
	 */
	void checkInfrastructureCapitalReading() throws Exception {
		int[] indices = { 1, 2 };
		for (int x : indices) {
			for (int y : indices) {
				checkCell(x, y, "INFRASTRUCTURE", CAPITALS_INFRASTRUCTURE);
			}
		}
	}

	void checkCell(int x, int y, String capName, double[][] capitals) {
		assertEquals(
				capitals[x - 1][y - 1],
				rl.getCell(x, y).getEffectiveCapitals()
				.getDouble(SimpleCapital.valueOf(capName)), 0.0001);
	}
}
