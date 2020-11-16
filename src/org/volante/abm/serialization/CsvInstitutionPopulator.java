/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2016 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 7 Dec 2016
 */
package org.volante.abm.serialization;


import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.fr.LazyFR;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.agent.property.PropertyRegistry;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.institutions.InstitutionAgent;
import org.volante.abm.serialization.RegionLoader.CellInitialiser;
import org.volante.abm.serialization.transform.IntTransformer;

import com.csvreader.CsvReader;


/**
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CsvInstitutionPopulator implements CellInitialiser {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(CsvAftPopulator.class);

	@Element(required = true)
	String csvFile = "";

	@Element(required = false)
	String institutionIdColumnName = "ID";

	@Element(required = false)
	String classnameColumnName = "Classname";

	@Element(required = false)
	String btColumnName = "BT";

	@Element(required = false)
	String frColumnName = "FR";

	@Element(required = false)
	protected int defaultBtId = Integer.MIN_VALUE;

	@Element(required = false)
	protected int defaultFrId = Integer.MIN_VALUE;

	@Element(required = false)
	String xColumn = "X";
	@Element(required = false)
	String yColumn = "Y";

	@Element(required = false)
	IntTransformer xTransformer = null;

	@Element(required = false)
	IntTransformer yTransformer = null;

	Set<String> agentPropertyColumns = new HashSet<String>();

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
	public void initialise(RegionLoader rLoader) throws Exception {
		ModelData data = rLoader.modelData;

		boolean assignBT = false;
		boolean assignFR = false;

		Map<PropertyId, Double> agentProperties = new HashMap<PropertyId, Double>();

		// Basic CSV file validation:
		if (!rLoader.persister.csvFileOK("RegionLoader", csvFile, rLoader.getRegion().getPersisterContextExtra(),
		        xColumn, yColumn)) {
			return;
		}

		logger.info("Loading institutions from " + csvFile);

		CsvReader reader = rLoader.persister.getCSVReader(csvFile, rLoader.getRegion().getPersisterContextExtra());

		List<String> columns = Arrays.asList(reader.getHeaders());
		agentPropertyColumns.addAll(columns);
		agentPropertyColumns.remove(xColumn);
		agentPropertyColumns.remove(yColumn);
		agentPropertyColumns.remove(institutionIdColumnName);
		agentPropertyColumns.remove(classnameColumnName);

		if (columns.contains(btColumnName)) {
			assignBT = true;
			agentPropertyColumns.remove(btColumnName);
		}
		if (columns.contains(frColumnName)) {
			assignFR = true;
			agentPropertyColumns.remove(frColumnName);
		}

		// <- LOGGING
		logger.info("Reading: " + (assignBT ? "BT " : "") + (assignFR ? "FR " : ""));
		// LOGGING ->

		while (reader.readRecord()) {
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Read row " + reader.getCurrentRecord());
			}
			// LOGGING ->

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
			String agentId = reader.get(institutionIdColumnName);

			// assemble agent
			String btValue = reader.get(btColumnName);
			String frValue = reader.get(frColumnName);
			int btId, frId;
			if (assignBT) {
				if (btValue.matches("\\d+")) {
					btId = Integer.parseInt(btValue);
				} else {
					if (!rLoader.region.getBehaviouralTypeMapByLabel().containsKey(btValue)) {
						logger.warn("Couldn't find BehaviouralType by label: " + btValue + ". Assigning default!");
						btId = Integer.MIN_VALUE;
					} else {
						btId = rLoader.region.getBehaviouralTypeMapByLabel().get(btValue).getSerialID();
					}
				}
			} else {
				btId = Integer.MIN_VALUE;
			}

			if (assignFR) {
				if (frValue.matches("\\d+")) {
					frId = Integer.parseInt(frValue);
				} else {
					if (!rLoader.region.getFunctionalRoleMapByLabel().containsKey(frValue)) {
						logger.warn("Couldn't find FunctionalRole by label: " + frValue + ". Assigning default!");
						frId = Integer.MIN_VALUE;
					} else {
						frId = rLoader.region.getFunctionalRoleMapByLabel().get(frValue).getSerialID();
					}
				}
			} else {
				frId = Integer.MIN_VALUE;
			}

			InstitutionAgent agent =
			        assembleInstitutionAgent(c, btId, frId, agentId, reader.get(this.classnameColumnName));

			for (String agentPropertyColumn : agentPropertyColumns) {
				agent.setProperty(PropertyRegistry.get(agentPropertyColumn),
				        Double.parseDouble(reader.get(agentPropertyColumn)));
			}
			
			c.getRegion().getInstitutions().addInstitution(agent);
		}
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Finished reading CSV file "
			        + rLoader.persister.getFullPath(csvFile, rLoader.getRegion().getPersisterContextExtra()));
		}
		// LOGGING ->
	}


	/**
	 * @param homecell
	 * @param btIdInitial
	 * @param frIdInitial
	 * @param id
	 * @param classname
	 * @return
	 */
	protected InstitutionAgent assembleInstitutionAgent(Cell homecell, int btIdInitial, int frIdInitial, String id,
	        String classname) {

		int btId;
		if (btIdInitial == Integer.MIN_VALUE) {
			btId = this.defaultBtId;
		} else {
			btId = btIdInitial;
		}

		int frId;
		if (frIdInitial == Integer.MIN_VALUE) {
			frId = this.defaultFrId;
		} else {
			frId = frIdInitial;
		}

		InstitutionAgent agent = null;
		try {
			agent = (InstitutionAgent) Class.forName(classname).getConstructor(String.class).newInstance(id);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
		        | NoSuchMethodException | SecurityException | ClassNotFoundException exception) {
			exception.printStackTrace();
		}

		if (homecell.getRegion().getFunctionalRoleMapBySerialId().containsKey(frId)) {
			homecell.getRegion().getFunctionalRoleMapBySerialId().get(frId).assignNewFunctionalComp(agent);
		} else if (this.defaultFrId == Integer.MIN_VALUE) {
			LazyFR.getInstance().assignNewFunctionalComp(agent);
			logger.warn("Requested FunctionalRole (" + frId + ") not found. Using LazyFR!");
		} else {
			logger.error("Couldn't find FunctionalRole by id: " + frId);
		}

		if (homecell.getRegion().getBehaviouralTypeMapBySerialId().containsKey(btId)) {
			homecell.getRegion().getBehaviouralTypeMapBySerialId().get(btId).assignNewBehaviouralComp(agent);
		} else {
			logger.error("Couldn't find BehaviouralType by id: " + btId);
		}

		agent.setHomeCell(homecell);
		return agent;
	}

	@Override
	public String toString() {
		return "CsvAftPopulator for " + csvFile;
	}

}
