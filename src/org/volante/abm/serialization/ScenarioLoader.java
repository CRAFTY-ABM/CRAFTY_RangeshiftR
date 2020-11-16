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
package org.volante.abm.serialization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mpi.MPI;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.Capital;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.data.Service;
import org.volante.abm.example.GlobalBtRepository;
import org.volante.abm.example.SerialSingleMarketSynchronisationModel;
import org.volante.abm.institutions.global.GlobalInstitution;
import org.volante.abm.institutions.global.GlobalInstitutionsList;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.output.Outputs;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.schedule.Schedule;
import org.volante.abm.schedule.WorldSyncSchedule;
import org.volante.abm.visualisation.DefaultModelDisplays;
import org.volante.abm.visualisation.ModelDisplays;

import de.cesr.more.basic.MManager;

/**
 * The scenario loader is responsible for setting up the following things:
 * <ul>
 * <li>ModelData, with appropriate Capitals, Services etc.</li>
 * <li>RunInfo with scenario and run id
 * <li>A RegionSet to run</li>
 * <li>A schedule</li>
 * <li>Outputs</li>
 * </ul>
 * 
 * If a {@link WorldLoader} is defined this initialise the {@link RegionSet}
 * first before {@link RegionLoader}s directly defined in the scenario XML file
 * add {@link Region}s to that set.
 * 
 * @author dmrust
 * 
 */
public class ScenarioLoader {
	ModelData modelData = new ModelData();
	RunInfo					info			= null;
	RegionSet regions = new RegionSet();
	ABMPersister			persister		= null;

	/**
	 * Scenario name (default: "Unknown")
	 */
	@Attribute(name = "version", required = false)
	String					version			= "V0";

	/**
	 * Regionalisation (default: "Unknown")
	 */
	@Attribute(name = "regionalisation", required = false)
	String					regionalisation	= "Unknown";

	/**
	 * Scenario name (default: "Unknown")
	 */
	@Attribute(name = "scenario", required = false)
	String scenario = "Unknown";
	
	/**
	 * World Name (default: "World")
	 */
	@Attribute(name = "world", required = false)
	String worldName = "World";

	/**
	 * run Identifier (default: "")
	 */
	@Attribute(name = "runID", required = false)
	String					runID			= "SET_INTERNAL";

	/**
	 * After the scenario configuration file has been parsed, this string is
	 * appended to the persister's basedir. This is useful if a configuration
	 * adapts some parameters and points to the super directory otherwise.
	 */
	@Attribute(name = "basedirAdaptation", required = false)
	String					basedirAdaptation			= "";
	
	/**
	 * This is appended to the adapted basedir when looking up CSV parameter
	 * files from parameters in batch mode (usually points to the same directory
	 * as the scenario file).
	 */
	@Attribute(name = "csvParamBasedirCorrection", required = false)
	String					csvParamBasedirCorrection	= "";

	@Attribute(name = "linkCsvBasedirCorrection", required = false)
	String linkCsvBasedirCorrection = null;

	/**
	 * startTick (int, default: 2000)
	 */
	@Attribute(name = "startTick", required = false)
	int startTick = 2000;

	/**
	 * endTick (int, default: 2015)
	 */
	@Attribute(name = "endTick", required = false)
	int						endTick			= 2015;
	
	@Element(name = "schedule", required = false)
	Schedule				schedule		= null;

	@Element(name = "worldSyncModel", required = false)
	WorldSynchronisationModel worldSyncModel = new SerialSingleMarketSynchronisationModel();

	@Element(name = "capitals", required = false)
	DataTypeLoader<Capital>	capitals		= null;

	@Element(name = "services", required = false)
	DataTypeLoader<Service>	services		= null;

	@ElementList(required = false, inline = true, entry = "region")
	List<RegionLoader> regionList = new ArrayList<RegionLoader>();
	@ElementList(required = false, inline = true, entry = "regionFile")
	List<String> regionFileList = new ArrayList<String>();

	@ElementList(inline = true, required = false, entry = "globalInstitutionFile")
	List<String> globalInstitutionFiles = new ArrayList<String>();

	@Element(required = false)
	String globalBtReposFile = null;

	@Element(required = false)
	WorldLoader worldLoader = null;

	@Element(required = false)
	String worldLoaderFile = null;

	@Element(required = false)
	Outputs outputs = new Outputs();

	@Element(required = false)
	String outputFile = null;

	Logger log = Logger.getLogger(getClass());

	@Element(required = false)
	private ModelDisplays			displays		= null;

	/**
	 * @param info
	 * @throws Exception
	 */
	public void initialise(RunInfo info) throws Exception {
		this.setSchedule(info.getSchedule());

		MManager.init();
		
		this.scenario = BatchRunParser.parseString(scenario, info);

		this.info = info;
		persister = info.getPersister();
		persister.setContext("s", scenario);
		ModelRunner.clog("Scenario", scenario);

		persister.setContext("v", version);
		ModelRunner.clog("Version", version);

		persister.setContext("w", worldName);
		ModelRunner.clog("WorldName", worldName);

		persister.setContext("k", regionalisation);
		ModelRunner.clog("Regionalisation", regionalisation);

		persister.setContext("c", "" + this.info.getCurrentRun());
		ModelRunner.clog("CurrentRun", "" + this.info.getCurrentRun());

		schedule.setStartTick(startTick);
		schedule.setEndTick(endTick);
		
		if (capitals != null) {
			log.info("Loading capitals");
			modelData.capitals = capitals.getDataTypes(persister);
		}
		log.info("Capitals: " + modelData.capitals);
		if (services != null) {
			log.info("Loading Services");
			modelData.services = services.getDataTypes(persister);
		}
		log.info("Services: " + modelData.services);

		info.setScenario(scenario);
		info.setRunID(runID);

		this.persister.setBaseDir(this.persister.getBaseDir() + this.basedirAdaptation);
		this.info.setCsvParamBasedirCorrection(this.csvParamBasedirCorrection);
		this.info.setLinksCsvBasedirCorrection(this.linkCsvBasedirCorrection != null ? this.linkCsvBasedirCorrection
				: this.csvParamBasedirCorrection);

		if (this.globalBtReposFile != null) {
			GlobalBtRepository repos = persister.readXML(GlobalBtRepository.class, this.globalBtReposFile, null);
			repos.initialise(modelData, info);
		}

		if (worldLoader == null && worldLoaderFile != null) {
			// TODO override persister method
			worldLoader = persister.readXML(WorldLoader.class, worldLoaderFile, null);
		}
		if (worldLoader != null) {
			worldLoader.setModelData(modelData);
			worldLoader.initialise(info);

			// regions for parallel processing have been selected here:
			regions = worldLoader.getWorld();
		}

		if (worldSyncModel != null) {
			worldSyncModel.initialise(modelData, info);
			if (schedule instanceof WorldSyncSchedule) {
				((WorldSyncSchedule) schedule).setWorldSyncModel(worldSyncModel);
			} else {
				log.warn("WorldSynchronisationModel could not be assigned to schedule!");
			}
		}
		
		if (outputFile != null) {
			// TODO override persister method
			outputs = persister.readXML(Outputs.class, outputFile, null);
		}
		info.setOutputs(outputs);

		schedule.initialise(modelData, info, null);

		log.info("About to load regions");
		for (String s : regionFileList) {
			// <- LOGGING
			log.warn("This way of initialising regions for parallel computing is untested!");
			// LOGGING ->
			// TODO override persister method
			regionList.add(persister.readXML(RegionLoader.class, s, null));
		}
		for (RegionLoader rl : regionList) {
			rl.initialise(info);
			
			if (MPI.COMM_WORLD.Rank() == rl.getUid()) {
				Region r = rl.getRegion();
				regions.addRegion(r);

				log.info("Run region " + r + " on rank " + MPI.COMM_WORLD.Rank());
			}
			
			
			Region reg = rl.getRegion();
			regions.addRegion(reg);
		}
		log.info("Final extent: " + regions.getExtent());

		this.modelData.setRootRegionalSet(regions);

		regions.initialise(modelData, info, null);


		// global institutions:
		log.info("About to initialise global institutions");
		Set<GlobalInstitution> institutions = new HashSet<GlobalInstitution>();

		for (String institutionsFile : globalInstitutionFiles) {
			institutions.addAll(persister.readXML(GlobalInstitutionsList.class, institutionsFile, null)
					.getGlobalInstitutions());
		}
		for (GlobalInstitution institution : institutions) {
			institution.initialise(modelData, info);
		}
		// Regional institutions are initialised elsewhere.. 
		
		outputs.initialise(modelData, info);

		if (getDisplays() == null) {
			setDisplays(new DefaultModelDisplays());
		}
		getDisplays().initialise(modelData, info, regions);
	}

	public void setSchedule(Schedule sched) {
		this.schedule = sched;
	}

	/**
	 * TODO doc
	 * 
	 * @param runID
	 */
	public void setRunID(String runID) {
		if (this.runID.equals("SET_INTERNAL")) {
			this.runID = runID;
		}
	}

	public RegionSet getRegions() {
		return regions;
	}

	/**
	 * @return the displays
	 */
	public ModelDisplays getDisplays() {
		return displays;
	}

	/**
	 * @param displays the displays to set
	 */
	private void setDisplays(ModelDisplays displays) {
		this.displays = displays;
	}
}
