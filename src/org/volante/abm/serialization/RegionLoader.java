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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.stream.NodeBuilder;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.example.RegionalDemandModel;
import org.volante.abm.example.SimpleAllocationModel;
import org.volante.abm.example.SimpleCompetitivenessModel;
import org.volante.abm.institutions.Institution;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.lara.RegionalLaraModel;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.schedule.TickAction;
import org.volante.abm.update.Updater;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import de.cesr.lara.toolbox.config.xml.LPersister;
import de.cesr.parma.core.PmParameterManager;
import de.cesr.parma.definition.PmFrameworkPa;
import de.cesr.parma.reader.PmXmlParameterReader;

/**
 * Class to load Regions from serialised data. Needs to load: * competitiveness
 * models * demand model * allocation model * baseCapitals for all cells *
 * initial agents for all cells
 * 
 * @author dmrust
 * 
 */
@Root(name = "region")
public class RegionLoader {

	/**
	 * Logger
	 */
	static private Logger log = Logger.getLogger(RegionLoader.class);

	static int						currentUid				= 0;
	
	final static String INSTITUTION_LIST_ELEMENT_NAME = "institutionsList";

	@Attribute(name = "id")
	String							id						= "Unknown";

	/**
	 * Required for parallel mpiJava computing:
	 */
	@Element(required = false)
	int pid = -1;

	@Element(required = false)
	String competitionFile = "";
	@Element(required = false)
	CompetitivenessModel competition = null;

	@Element(required = false)
	AllocationModel allocation = null;
	@Element(required = false)
	String allocationFile = "";

	@Element(required = false)
	boolean handleAmbulantAgents = true;

	@Element(required = false)
	DemandModel demand = null;
	@Element(required = false)
	String demandFile = "";

	@Element(required = false)
	BTList bTypes = new BTList();

	@Element(required = false)
	FRList fRoles = new FRList();
	
	
 

	@Element(required = false)
	SocialNetworkLoaderList socialNetworkLoaders = new SocialNetworkLoaderList();

	@ElementList(required = false, inline = true, entry = "btFile")
	List<String> btFileList = new ArrayList<String>();

	@ElementList(required = false, inline = true, entry = "frFile")
	List<String> frFileList = new ArrayList<String>();

	@ElementList(inline = true, required = false, empty = false, entry = "cellInitialiser")
	List<CellInitialiser> cellInitialisers = new ArrayList<CellInitialiser>();
	@ElementList(required = false, inline = true, entry = "cellInitialiserFile")
	List<String> cellInitialiserFiles = new ArrayList<String>();

	@ElementList(inline = true, required = false, empty = false, entry = "agentInitialiser")
	List<AgentInitialiser> agentInitialisers = new ArrayList<AgentInitialiser>();
	@ElementList(required = false, inline = true, entry = "agentInitialiserFile")
	List<String> agentInitialiserFiles = new ArrayList<String>();

	@Element(required = false)
	String pmParameterFile = null;

	/**
	 * Location of XML parameter file for social network initialisations (it is
	 * possible to have several networks of agents to build up a multiplex
	 * social network).
	 */
	@ElementList(required = false, inline = true, entry = "socialNetworkParamFile")
	List<String> socialNetworkFileList = new ArrayList<String>();

	@ElementList(inline = true, required = false, entry = "updater")
	List<Updater> updaters = new ArrayList<Updater>();
	@ElementList(inline = true, required = false, entry = "updaterFile")
	List<String> updaterFiles = new ArrayList<String>();

	@ElementList(inline = true, required = false, entry = "institution")
	List<Institution> institutions = new ArrayList<Institution>();
	@ElementList(inline = true, required = false, entry = "institutionFile")
	List<String> institutionFiles = new ArrayList<String>();

	@ElementList(inline = true, required = false, entry = "initialiser")
	List<Initialisable> initialisers = new ArrayList<Initialisable>();
	@ElementList(inline = true, required = false, entry = "initialiserFile")
	List<String> initialiserFiles = new ArrayList<String>();

	@Element(required = false)
	String regionalLaraModelFile = null;

	@Element(required = false)
	RegionalLaraModel regionalLaraModel = null;

	@Element(required = false)
	int randomSeed = Integer.MIN_VALUE;

	/**
	 * The given agent ID is recognised as unmanaged and therefore no error
	 * message is issued.
	 */
	@Element(required = false)
	String idUnmanaged = "UNMANAGED";

	ABMPersister persister = null;
	ModelData modelData = null;
	RunInfo runInfo = null;
	Region region = null;
	Table<Integer, Integer, Cell> cellTable = TreeBasedTable.create();

	public RegionLoader() {
		this(null, null);
	}

	public RegionLoader(ModelData data, ABMPersister persister) {
		this.persister = persister;
		this.modelData = data;
	}

	public RegionLoader(String pid, String id, String competition, String allocation,
 String demand, String btfiles,
			String frfiles, String cellInitialisers,
			String agentInitialisers) {
		this(pid, id, competition, allocation, true, demand, btfiles, frfiles,
 cellInitialisers, agentInitialisers, null,
		        null, null,
				null);
	}

	public RegionLoader(String pid, String id, String competition, String allocation,
	        boolean handleAmbulantAgents,
 String demand, String btfiles,
			String frfiles, String cellInitialisers, String agentInitialisers, String socialNetworkFile,
			String institutionFile,
 String updatersFile,
			String laraModelFile) {
		this.pid = Integer.parseInt(pid);
		this.id = id;
		this.competitionFile = competition;
		this.allocationFile = allocation;
		this.handleAmbulantAgents = handleAmbulantAgents;
		this.demandFile = demand;
		this.btFileList.addAll(ABMPersister.splitTags(btfiles));
		this.frFileList.addAll(ABMPersister.splitTags(frfiles));
		this.cellInitialiserFiles.addAll(ABMPersister
				.splitTags(cellInitialisers));

		if (agentInitialisers != null && !agentInitialisers.equals("")) {
			this.agentInitialiserFiles.addAll(ABMPersister
					.splitTags(agentInitialisers));
		}

		if (socialNetworkFile != null && !socialNetworkFile.equals("")) {
			this.socialNetworkFileList.addAll(ABMPersister.splitTags(socialNetworkFile));
		}

		if (institutionFile != null && !institutionFile.equals("")) {
			this.institutionFiles.addAll(ABMPersister.splitTags(institutionFile));
		}

		if (updatersFile != null && !updatersFile.equals("")) {
			this.updaterFiles.addAll(ABMPersister.splitTags(updatersFile));
		}

		if (laraModelFile != null && !laraModelFile.equals("")) {
			regionalLaraModelFile = laraModelFile;
		}
	}

	public void initialise(RunInfo info) throws Exception {

		log.info(">>> Initialise region " + id);

		this.runInfo = info;
		if (modelData == null) {
			modelData = new ModelData();
		}
		if (persister == null) {
			persister = ABMPersister.getInstance();
		}

		region = new Region();
		region.setID(id);

		readPmParameters();

		initInitialisers();

		initLaraModel();

		loadFunctionalRoles();
		loadBehaviouralTypes();

		loadModels();
		loadSocialNetworks();

		passInfoToRegion();
		initialiseCells();

		loadInstitutions();
		initialiseAgents();
		loadUpdaters();
	}

	/**
	 * @throws Exception
	 * 
	 */
	private void initLaraModel() throws Exception {
		// <- LOGGING
		log.info("LaraModel file: " + regionalLaraModelFile);
		// LOGGING ->

		if (regionalLaraModel == null) {
			if (regionalLaraModelFile != null && !regionalLaraModelFile.equals("")) {
				regionalLaraModel =
						persister.readXML(RegionalLaraModel.class, regionalLaraModelFile,
								this.region.getPersisterContextExtra());
			} else {
				log.warn("LARA model could not be loaded in RegionLoader (regionalLaraModelFile: "
						+ regionalLaraModelFile + ")!");
			}
		}

		if (this.regionalLaraModel != null) {
			this.regionalLaraModel.initialise(this.modelData, this.runInfo,
					this.region);
		} else {
			log.warn("LARA model could not be initialised in RegionLoader!");
		}
	}

	/**
	 * 
	 */
	protected void readPmParameters() {
		if (this.pmParameterFile != null) {
			PmParameterManager pm = PmParameterManager.getInstance(this.region);
			pm.setParam(
					PmFrameworkPa.XML_PARAMETER_FILE,
					ABMPersister.getInstance().getFullPath(pmParameterFile,
							this.region.getPersisterContextExtra()));
			new PmXmlParameterReader(pm, PmFrameworkPa.XML_PARAMETER_FILE)
					.initParameters();
		}
	}

	/**
	 * Initialises {@link SocialNetworkLoader}s.
	 * 
	 * @throws Exception
	 */
	protected void loadSocialNetworks() throws Exception {
		for (String socialNetworkFile : socialNetworkFileList) {
			socialNetworkLoaders.loaders.addAll(persister.readXML(
					SocialNetworkLoaderList.class, socialNetworkFile,
					this.region.getPersisterContextExtra()).loaders);
		}

		for (SocialNetworkLoader l : socialNetworkLoaders.loaders) {
			log.info("Initialise social network loader: " + l.getName());
			l.initialise(modelData, runInfo, region);
		}
	}

	public void loadBehaviouralTypes() throws Exception {
		for (String btFile : btFileList) {
			// <- LOGGING
			log.info("Behavioural Types file: " + btFile);
			// LOGGING ->

			// we need to apply the LARA specific persister here because a
			// region-specific persister needs to
			// refer to the region-specific LaraPreferecenRegistry!
			bTypes.bTypes.addAll(LPersister.getPersister(region).readXML(
					BTList.class, btFile).bTypes);
		}
		for (BehaviouralType bt : bTypes.bTypes) {
			log.info("Initialise behavioural type: " + bt.getLabel());
			bt.initialise(modelData, runInfo, region);
		}
	}

	public void loadFunctionalRoles() throws Exception {
		for (String frFile : frFileList) {
			// <- LOGGING
			log.info("Functional Roles file: " + frFile);
			// LOGGING ->

			fRoles.fRoles.addAll(persister.readXML(FRList.class, frFile,
					this.region.getPersisterContextExtra()).fRoles);
		}
		for (FunctionalRole fr : fRoles.fRoles) {
			log.info("Initialise functional role: " + fr.getLabel());
			fr.initialise(modelData, runInfo, region);
		}
	}

	public void loadModels() throws Exception {
		if (allocation == null) {
			
			// <- LOGGING
			log.info("Allocation Model file: " + allocationFile);
			// LOGGING ->
			allocation = persister.readXML(AllocationModel.class,
					allocationFile, this.region.getPersisterContextExtra());
		}
		if (demand == null) {
			// <- LOGGING
			log.info("Demand Model file: " + demandFile);
			// LOGGING ->
			demand = persister.readXML(DemandModel.class, demandFile,
					this.region.getPersisterContextExtra());
		}

		if (competition == null) {
			// <- LOGGING
			log.info("Competition Model file: " + competitionFile);
			// LOGGING ->
 			
			competition = persister.readXML(CompetitivenessModel.class,
					competitionFile, this.region.getPersisterContextExtra());
		}
		if (allocation instanceof TickAction) {
			runInfo.getSchedule().register((TickAction) allocation);
		}
		if (demand instanceof TickAction) {
			runInfo.getSchedule().register((TickAction) demand);
		}
		if (competition instanceof TickAction) {
			runInfo.getSchedule().register((TickAction) competition);
		}
		runInfo.getSchedule().register(region);
	}

	/**
	 * 
	 */
	private void initInitialisers() throws Exception {
		for (String initialiserFile : initialiserFiles) {
			initialisers.addAll(persister.readXML(InitialiserList.class, initialiserFile,
					this.region.getPersisterContextExtra()).initialisers);
		}
		for (Initialisable i : initialisers) {
			i.initialise(modelData, runInfo, region);
		}
	}

	private void loadUpdaters() throws Exception {
		for (String updaterFile : updaterFiles) {
			updaters.add(persister.readXML(Updater.class, updaterFile,
					this.region.getPersisterContextExtra()));
		}
		for (Updater u : updaters) {
			u.initialise(modelData, runInfo, region);
			runInfo.getSchedule().register(u);
		}
	}

	private void loadInstitutions() throws Exception {
		for (String institutionFile : institutionFiles) {

			// TODO document (SH)
			if (NodeBuilder.read(
					new FileInputStream(new File(persister.getFullPath(
							institutionFile,
							this.region.getPersisterContextExtra())))).getName() == INSTITUTION_LIST_ELEMENT_NAME) {
				institutions
						.addAll(persister.readXML(InstitutionsList.class,
								institutionFile,
								this.region.getPersisterContextExtra()).institutions);
			} else {
				institutions
						.add(persister.readXML(Institution.class,
								institutionFile,
								this.region.getPersisterContextExtra()));
			}
		}
		if (institutions.size() > 0) {
			Institutions in = region.getInstitutions();
			for (Institution i : institutions) {
				in.addInstitution(i);
			}
		}
	}

	public void initialiseCells() throws Exception {
		for (String s : cellInitialiserFiles) {
			cellInitialisers.add(persister.readXML(CellInitialiser.class, s,
					this.region.getPersisterContextExtra()));
		}
		for (CellInitialiser ci : cellInitialisers) {
			if (ci instanceof Initialisable) {
				((Initialisable) ci).initialise(modelData, runInfo, region);
			}
			ci.initialise(this);
		}
		region.cellsCreated();
		log.info("Loaded " + cellTable.size() + " cells from "
				+ cellInitialisers.size() + " loader(s)");
	}

	public void initialiseAgents() throws Exception {
		for (String s : agentInitialiserFiles) {
			agentInitialisers.add(persister.readXML(AgentInitialiser.class, s,
					this.region.getPersisterContextExtra()));
		}
		for (AgentInitialiser ci : agentInitialisers) {
			ci.initialise(this);
		}
		region.makeUnmanagedCellsAvailable();
	}

	public void passInfoToRegion() throws Exception {
		region.setDemandModel(demand);
		region.setAllocationModel(allocation);
		region.setCompetitivenessModel(competition);
		region.addBehaviouralTypes(bTypes.bTypes);
		region.addfunctionalRoles(fRoles.fRoles);
		if (this.randomSeed != Integer.MIN_VALUE) {
			PmParameterManager.getInstance(region).setParam(
					RandomPa.RANDOM_SEED, randomSeed);
		}
	}

	/**
	 * In case there is not yet a cell at the given coordinates, it instantiates
	 * and initialises a new cell. Add it adds it to region at given coordinates.
	 * Furthermore, sets initial ownership to {@link Agent#NOT_MANAGED}.
	 * 
	 * @param x
	 * @param y
	 * @return new cell object
	 */
	public Cell getCell(int x, int y) {
		if (cellTable.contains(x, y)) {
			return cellTable.get(x, y);
		}
		Cell c = new Cell(x, y);
		c.initialise(modelData, runInfo, region);
		region.addCell(c);
		cellTable.put(x, y, c);
		region.setInitialOwnership(Agent.NOT_MANAGED, c);
		return c;
	}

	/*
	 * Getters and setters
	 */
	public String getCompetitionFile() {
		return competitionFile;
	}

	public void setCompetitionFile(String competitionFile) {
		this.competitionFile = competitionFile;
	}

	public CompetitivenessModel getCompetition() {
		return competition;
	}

	public void setCompetition(CompetitivenessModel competition) {
		this.competition = competition;
	}

	public AllocationModel getAllocation() {
		return allocation;
	}

	public void setAllocation(AllocationModel allocation) {
		this.allocation = allocation;
	}

	public String getAllocationFile() {
		return allocationFile;
	}

	public void setAllocationFile(String allocationFile) {
		this.allocationFile = allocationFile;
	}

	public DemandModel getDemand() {
		return demand;
	}

	public void setDemand(DemandModel demand) {
		this.demand = demand;
	}

	public String getDemandFile() {
		return demandFile;
	}

	public void setDemandFile(String demandFile) {
		this.demandFile = demandFile;
	}

	public ABMPersister getPersister() {
		return persister;
	}

	public void setPersister(ABMPersister persister) {
		this.persister = persister;
	}

	public void setModelData(ModelData modelData) {
		this.modelData = modelData;
	}

	public void setRunInfo(RunInfo runInfo) {
		this.runInfo = runInfo;
	}

	public Region getRegion() {
		return region;
	}

	public static interface CellInitialiser {
		public void initialise(RegionLoader rl) throws Exception;
	}

	public static interface AgentInitialiser {
		public void initialise(RegionLoader rl) throws Exception;
	}

	public void setDefaults() {
		demand = new RegionalDemandModel();
		competition = new SimpleCompetitivenessModel();
		allocation = new SimpleAllocationModel();
	}

	public int getRandomSeed() {
		return this.randomSeed;
	}

	/**
	 * @return the pid
	 */
	public int getUid() {
		return pid;
	}
}
