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
 * Created by Sascha Holzhauer on 19 Mar 2015
 */
package org.volante.abm.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.geotools.util.UnsupportedImplementationException;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.assembler.AgentAssembler;
import org.volante.abm.agent.assembler.DefaultSocialAgentAssembler;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.agent.property.PropertyRegistry;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.param.RandomPa;
import org.volante.abm.serialization.transform.IntTransformer;

import repast.simphony.parameter.IllegalParameterException;

import com.csvreader.CsvReader;
import com.moseph.modelutils.fastdata.DoubleMap;

import de.cesr.parma.core.PmParameterManager;


/**
 * Responsible for reading cell and agent properties.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CsvAftPopulator implements AftPopulator {

	@Element(required = true)
	String	csvFile		= "";
	
	@Element(required = false)
	String csvCapitalFactorFile = null;

	@Element(required = false)
	String agentIdColumnName = "AgentID";

	@Element(required = false)
	String btColumnName = "BT";
	
	@Element(required = false)
	String frColumnName = "FR";
	
	@Element(required = false)
	String			xColumn			= "X";
	@Element(required = false)
	String			yColumn			= "Y";
	
	@Element(required = false)
	IntTransformer	xTransformer	= null;

	@Element(required = false)
	IntTransformer	yTransformer	= null;
	
	@Element(required = false)
	boolean manageEveryHomeCell = true;

	@Element(required = false)
	boolean manageNoHomeCell = false;

	@Element(required = false)
	boolean taggedHomeCells = false;


	@Element(required = false)
	String homeCellColumnName = "Homecell";

	@Element(required = false)
	String manageHomeCellColumnName = "manageHomecell";

	@Element(required = false)
	String unmanagedIdentifier = "";

	@Element(required = false)
	String trueIdentifier = "1";
	
	
	@Element(required = false)
	AgentAssembler agentAssembler = new DefaultSocialAgentAssembler();

	@Element(required=false)
	boolean shuffleCellsBeforeAssembling = true;
	
	@Element(required = false)
	int notificationInterval = 10000;

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(CsvAftPopulator.class);

	Set<String> agentPropertyColumns = new HashSet<String>();

	enum HomeCellMode {
		MANAGE_EVERY, UNMANAGED, TAGGED, UNDETERMINED;
	}
	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(RegionLoader rLoader) throws Exception {
		ModelData data = rLoader.modelData;

		boolean hasAgentColumn = false;
		boolean singleCellAgentMode = false;
		boolean assignBT = false;
		boolean assignFR = false;
		HomeCellMode homeCellMode = HomeCellMode.UNDETERMINED;
		int agentCounter = 0;

		Map<String, Set<Cell>> agentCellMap = new LinkedHashMap<String, Set<Cell>>();
		Map<String, Cell> agentHomeCells = new LinkedHashMap<String, Cell>();

		Map<String, String> agentBTs = new LinkedHashMap<String, String>();
		Map<String, String> agentFRs = new LinkedHashMap<String, String>();

		Map<String, Map<PropertyId, Double>> agentProperties = new HashMap<String, Map<PropertyId, Double>>();


		// Basic CSV file validation:
		if (!rLoader.persister.csvFileOK("RegionLoader", csvFile, rLoader
				.getRegion()
				.getPersisterContextExtra(), xColumn, yColumn)) {
			return;
		}

		logger.info("Loading cell CSV from " + csvFile);

		CsvReader reader = rLoader.persister.getCSVReader(csvFile, rLoader
				.getRegion()
				.getPersisterContextExtra());

		List<String> columns = Arrays.asList(reader.getHeaders());
		agentPropertyColumns.addAll(columns);

		agentPropertyColumns.remove(xColumn);
		agentPropertyColumns.remove(yColumn);
		for (Capital cap : data.capitals) {
			agentPropertyColumns.remove(cap.getName());
		}

		if (columns.contains(agentIdColumnName)) {
			hasAgentColumn = true;
			agentPropertyColumns.remove(agentIdColumnName);
		}
		if (!columns.contains(homeCellColumnName)) {
			singleCellAgentMode = true;
		} else {
			agentPropertyColumns.remove(homeCellColumnName);
		}

		if (columns.contains(btColumnName)) {
			assignBT = true;
			agentPropertyColumns.remove(btColumnName);
		}
		if (columns.contains(frColumnName)) {
			assignFR = true;
			agentPropertyColumns.remove(frColumnName);
		}

		if (taggedHomeCells) {
			agentPropertyColumns.remove(manageHomeCellColumnName);
		}

		if (manageEveryHomeCell && !manageNoHomeCell && !taggedHomeCells) {
			homeCellMode = HomeCellMode.MANAGE_EVERY;
		} else if (!manageEveryHomeCell && manageNoHomeCell
				&& !taggedHomeCells) {
			homeCellMode = HomeCellMode.UNMANAGED;
		} else if (!manageEveryHomeCell && !manageNoHomeCell
				&& taggedHomeCells) {
			homeCellMode = HomeCellMode.TAGGED;
		} else if (!manageEveryHomeCell && !manageNoHomeCell
				&& !taggedHomeCells) {
			throw new IllegalParameterException(
					"One of manageEveryHomeCell/unmanagedHomeCell/taggedHomeCells "
							+ "must be true!!");
		} else {
			throw new IllegalParameterException(
					"Only one of manageEveryHomeCell/unmanagedHomeCell/taggedHomeCells "
							+ "may be true!!");
		}

		// More validation:
		if (!singleCellAgentMode) {
			checkColumnExists(columns, homeCellColumnName);
			checkColumnExists(columns, agentIdColumnName);
		}

		if (homeCellMode == HomeCellMode.TAGGED) {
			checkColumnExists(columns, manageHomeCellColumnName);
		}
		if (singleCellAgentMode && homeCellMode == HomeCellMode.TAGGED) {
			throw new UnsupportedImplementationException(
					"Tagged home cell mode is not supported in"
							+ "single cell agent mode!");
		}

		// <- LOGGING
		logger.info("Reading: "
				+ (assignBT ? "BT " : "")
				+ (assignFR ? "FR " : "")
				+ "in "
				+ (singleCellAgentMode ? "SingleCellPerAgentMode "
						: "MultiCellPerAgentMode ")
				+ "with home cell managing mode " + homeCellMode);
		// LOGGING ->

		this.agentAssembler.initialise(data, rLoader.runInfo, rLoader.region);
		
		int counter = 0;

		while (reader.readRecord()) {
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Read row " + reader.getCurrentRecord());
			}
			// LOGGING ->
			
			counter++;
			
			if (counter % notificationInterval == 0) {
				// <- LOGGING
				logger.info("\tNumber of read rows: " + counter);
				// LOGGING ->
			}

			int x = Integer.parseInt(reader.get(xColumn));
			if (xTransformer != null) {
				x = xTransformer.transform(x);
			}

			int y = Integer.parseInt(reader.get(yColumn));
			if (yTransformer != null) {
				y = yTransformer.transform(y);
			}

			// Read, initialise and set cell:
			Cell c = rLoader.getCell(x, y);
			DoubleMap<Capital> adjusted = data.capitalMap();
			c.getBaseCapitals().copyInto(adjusted);
			for (Capital cap : data.capitals) {
				String s = reader.get(cap.getName());
				if (!s.equals("")) {
					try {
						adjusted.putDouble(cap, Double.parseDouble(s));
					} catch (Exception exception) {
						logger.error("Exception in row "
								+ reader.getCurrentRecord() + " ("
								+ exception.getMessage() + ") for capital " + cap.getName());
					}
				}
			}
			c.setBaseCapitals(adjusted);

			if (singleCellAgentMode && !shuffleCellsBeforeAssembling) {

				String agentId = null;
				if (hasAgentColumn) {
					// assemble agents straight away
					agentId = reader.get(agentIdColumnName);
				}
						
				// assemble agent
				LandUseAgent agent =
						assembleAgent(rLoader, assignBT, assignFR,
						reader, c, agentId, reader.get(btColumnName),
						reader.get(frColumnName));

				if (agent != Agent.NOT_MANAGED) {
					for (String agentPropertyColumn : agentPropertyColumns) {
						agent.setProperty(PropertyRegistry.get(agentPropertyColumn),
								Double.parseDouble(reader.get(agentPropertyColumn)));
					}

					if (homeCellMode == HomeCellMode.MANAGE_EVERY) {
						rLoader.region.setInitialOwnership(agent, c);
					}
				}

			} else if (!(singleCellAgentMode && homeCellMode == HomeCellMode.UNMANAGED)) {
				// exclude case when no agents shall be initialised!

				// store all agentIDs of home-cell as keys in a map
				String agentId = "";
				if (!columns.contains(agentIdColumnName)) {
					agentId = "Agent_" + agentCounter++;
				} else {
					agentId = reader.get(agentIdColumnName);
				}

				if (singleCellAgentMode
						|| reader.get(homeCellColumnName)
								.equals(trueIdentifier)) {

					if (agentHomeCells.containsKey(agentId)) {
						logger.warn("There is more than one home cell for the same agent ID ("
								+ agentId
								+ ") defined. Will ignore the previous one!");
					}
					agentHomeCells.put(agentId, c);

					// check if home cell is managed:
					if (homeCellMode == HomeCellMode.MANAGE_EVERY) {
						addManagedCell(agentCellMap, c, agentId);
					} else if (homeCellMode == HomeCellMode.TAGGED) {
						if (reader.get(manageHomeCellColumnName).equals(
								trueIdentifier)) {
							addManagedCell(agentCellMap, c, agentId);
						}
					}

					agentBTs.put(agentId, reader.get(btColumnName));
					agentFRs.put(agentId, reader.get(frColumnName));

					Map<PropertyId, Double> agentPropertyMap = new HashMap<PropertyId, Double>();
					for (String agentPropertyColumn : agentPropertyColumns) {
						try {
							agentPropertyMap.put(PropertyRegistry.get(agentPropertyColumn),
							        Double.parseDouble(reader.get(agentPropertyColumn)));
						} catch (NumberFormatException ex) {
							throw new IllegalParameterException("Property " + agentPropertyColumn
							        + " could not be evaluated for " + agentId);
						}
					}
					agentProperties.put(agentId, agentPropertyMap);

				} else {
					// assign all non-home cells in the 'agentID -> cells' map
					addManagedCell(agentCellMap, c, agentId);
				}
			}
		}

		this.applyCapitalFactors(rLoader);

		// shuffle cells before assembling (requires caching of
		// home-cells/agents)
		List<String> agentIdSet = new ArrayList<String>(agentHomeCells.keySet());
		if (shuffleCellsBeforeAssembling) {
			Collections.shuffle(agentIdSet, new Random(
					(Integer) PmParameterManager.getInstance(rLoader.region)
							.getParam(RandomPa.RANDOM_SEED_INIT_AGENTS)));
		}

		for (String agentId : agentIdSet) {
			// assemble agent
			LandUseAgent agent =
					assembleAgent(rLoader, assignBT, assignFR, reader,
					agentHomeCells.get(agentId), agentId,
					agentBTs.get(agentId), agentFRs.get(agentId));
			
			rLoader.region.setInitialOwnership(agent, agentCellMap.get(agentId)
					.toArray(new Cell[1]));

			for (Entry<PropertyId, Double> property : agentProperties.get(agentId).entrySet()) {
				agent.setProperty(property.getKey(), property.getValue());
			}
		}

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Finished reading CSV file "
					+ rLoader.persister.getFullPath(csvFile, rLoader
							.getRegion().getPersisterContextExtra()));
		}
		// LOGGING ->
	}

	/**
	 * @param rLoader
	 * @param assignBT
	 * @param assignFR
	 * @param reader
	 * @param c
	 * @param agentId
	 * @return agent
	 * @throws IOException
	 */
	protected LandUseAgent assembleAgent(RegionLoader rLoader, boolean assignBT,
			boolean assignFR, CsvReader reader, Cell c, String agentId,
			String btValue, String frValue)
			throws IOException {

		if (frValue.equals(unmanagedIdentifier)) {
			return Agent.NOT_MANAGED;
		}

		int btId, frId;
		if (assignBT) {
			if (btValue.matches("\\d+")) {
				btId = Integer.parseInt(btValue);
			} else {
				if (!rLoader.region.getBehaviouralTypeMapByLabel().containsKey(
						btValue)) {
					logger.warn("Couldn't find BehaviouralType by label: " + btValue + ". Assigning default!");
					btId = Integer.MIN_VALUE;
				} else {
					btId = rLoader.region.getBehaviouralTypeMapByLabel()
							.get(btValue).getSerialID();
				}
			}
		} else {
			btId = Integer.MIN_VALUE;
		}

		if (assignFR) {
			if (frValue.matches("\\d+")) {
				frId = Integer.parseInt(frValue);
			} else {
				if (!rLoader.region.getFunctionalRoleMapByLabel().containsKey(
						frValue)) {
					logger.warn("Couldn't find FunctionalRole by label: "
							+ frValue + ". Assigning default!");
					frId = Integer.MIN_VALUE;
				} else {
					frId = rLoader.region.getFunctionalRoleMapByLabel()
							.get(frValue).getSerialID();
				}
			}
		} else {
			frId = Integer.MIN_VALUE;
		}

		LandUseAgent agent = agentAssembler.assembleAgent(c, btId, frId, agentId);
		return agent;
	}

	/**
	 * @param reader
	 * @param homeCellColumnName2
	 */
	private void checkColumnExists(List<String> columns, String columnName) {
		if (!columns.contains(columnName)) {
			String message = "The CSV agent initialisation file '" + csvFile
					+ "' does not contain the column '" + columnName
					+ "' which is required for the current mode!";

			logger.error(message);
			throw new IllegalStateException(message);
		}
	}

	/**
	 * @param agentCellMap
	 * @param c
	 * @param agentId
	 */
	protected void addManagedCell(Map<String, Set<Cell>> agentCellMap, Cell c,
			String agentId) {
		if (!agentCellMap.containsKey(agentId)) {
			agentCellMap.put(agentId, new HashSet<Cell>());
		}
		agentCellMap.get(agentId).add(c);
	}
	
	/**
	 * Uses same column names as defined for the 'main' CSV file.
	 * 
	 * TODO test
	 * 
	 * @param rLoader
	 * @throws Exception
	 */
	protected void applyCapitalFactors(RegionLoader rLoader) throws Exception {
		if (this.csvCapitalFactorFile != null) {
			logger.info("Loading capital factor CSV from " + csvCapitalFactorFile);

			ModelData data = rLoader.modelData;
			CsvReader capitalFactorReader =
					rLoader.persister.getCSVReader(csvCapitalFactorFile, rLoader.getRegion().getPersisterContextExtra());

			while (capitalFactorReader.readRecord()) {

				int x = Integer.parseInt(capitalFactorReader.get(xColumn));
				if (xTransformer != null) {
					x = xTransformer.transform(x);
				}

				int y = Integer.parseInt(capitalFactorReader.get(yColumn));
				if (yTransformer != null) {
					y = yTransformer.transform(y);
				}

				Cell c = rLoader.getCell(x, y);
				for (Capital cap : data.capitals) {
					String s = capitalFactorReader.get(cap.getName());
					if (!s.equals("")) {
						try {
							DoubleMap<Capital> adjusted = data.capitalMap();
							c.getBaseCapitals().copyInto(adjusted);
							adjusted.putDouble(cap, adjusted.get(cap) * Double.parseDouble(s));
							c.setBaseCapitals(adjusted);

						} catch (Exception exception) {
							logger.error("Exception in row " + capitalFactorReader.getCurrentRecord() + " ("
									+ exception.getMessage() + ") for capital " + cap.getName());
						}
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return "CsvAftPopulator for " + csvFile;
	}
}
