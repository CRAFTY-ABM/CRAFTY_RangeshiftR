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
package org.volante.abm.data;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.geotools.factory.GeoTools;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.agent.bt.BehaviouralTypeMap;
import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.institutions.Institutions;
import org.volante.abm.institutions.innovation.InnovationRegistry;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.output.ActionReporter;
import org.volante.abm.param.GeoPa;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import de.cesr.more.basic.edge.MoreEdge;
import de.cesr.more.basic.network.MoreNetwork;
import de.cesr.more.building.network.MoreNetworkService;
import de.cesr.parma.core.PmParameterManager;
import de.cesr.uranus.util.UIdentifyCallerException;
import repast.simphony.space.gis.DefaultGeography;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;


public class Region implements Regions, PreTickAction {

	/**
	 * Logger
	 */
	static private Logger	logger						= Logger.getLogger(Region.class);

	static final String		GEOGRAPHY_NAME_EXTENSION	= "_Geography";

	/*
	 * Main data fields
	 * 
	 * LinkedHashMaps are required to guarantee a defined order of agent creation
	 * which usually involves random number generation (cells > available > allocation)
	 */
	Set<Cell>				cells						= new LinkedHashSet<Cell>();
	Set<LandUseAgent> allocatedAgents = new LinkedHashSet<>();

	/**
	 * Agents which are currently not allocated to any cell.
	 */
	Set<LandUseAgent> ambulantAgents = new LinkedHashSet<>();

	AllocationModel			allocation;
	CompetitivenessModel	competition;
	DemandModel				demand;
	Set<Cell>				available					= new LinkedHashSet<Cell>();

	Map<String, BehaviouralType> behaviouralTypesByLabel = new BehaviouralTypeMap<>(this);
	Map<Integer, BehaviouralType> behaviouralTypesBySerialId = new BehaviouralTypeMap<>(this);

	Map<String, FunctionalRole> functionalRolesByLabel = new LinkedHashMap<String, FunctionalRole>();
	Map<Integer, FunctionalRole> functionalRolesBySerialId = new LinkedHashMap<Integer, FunctionalRole>();

	protected ModelData data;
	RunInfo					rinfo;
	Institutions institutions = null;
	String					id							= "UnknownRegion";
	Map<String, String>			peristerContextExtra					= new HashMap<String, String>();

	Set<ActionReporter> registeredPaReporter = new HashSet<>();

	boolean requiresEffectiveCapitalData = false;
	boolean hasCompetitivenessAdjustingInstitution = false;

	boolean skipInitialAllocation = false;

	boolean handleAmbulantAgents = true;

	Map<Object, RegionHelper>	helpers					= new LinkedHashMap<Object, RegionHelper>();

	InnovationRegistry		innovationRegistry			= new InnovationRegistry(this);

	/**
	 * @return the innovationRegistry
	 */
	public InnovationRegistry getInnovationRegistry() {
		return innovationRegistry;
	}

	Geography<Object>	geography;
	GeometryFactory		geoFactory;

	RegionalRandom			random			= null;

	Map<FunctionalRole, Double> maxGivingUpDiff = null;

	/*
	 * Unmodifiable versions to pass out as necessary
	 */
	Set<LandUseAgent> uAgents = Collections.unmodifiableSet(allocatedAgents);

	Map<String, BehaviouralType> uBehaviouralTypesByLabel = Collections
			.unmodifiableMap(behaviouralTypesByLabel);

	Map<Integer, BehaviouralType> uBehaviouralTypesBySerialId = Collections
			.unmodifiableMap(behaviouralTypesBySerialId);

	Map<String, FunctionalRole> ufunctionalRolesByLabel = Collections
			.unmodifiableMap(functionalRolesByLabel);

	Map<Integer, FunctionalRole> uFunctionalRolesBySerialId = Collections
			.unmodifiableMap(functionalRolesBySerialId);

	Set<Cell> uCells = Collections.unmodifiableSet(cells);
	Set<Cell> uAvailable = Collections.unmodifiableSet(available);
	Set<Region> uRegions = Collections.unmodifiableSet(new HashSet<Region>(
			Arrays.asList(new Region[] { this })));
	Table<Integer, Integer, Cell> cellTable = null;

	Extent extent = new Extent();

	boolean initialised = false;

	Logger log = Logger.getLogger(getClass());

	/*
	 * Constructors, with initial sets of cells for convenience
	 */
	public Region() {
		PmParameterManager pm = PmParameterManager.getNewInstance(this);
		pm.setDefaultPm(PmParameterManager.getInstance(null));
		this.random = new RegionalRandom(this);
		this.random.init();
		this.peristerContextExtra.put(ABMPersister.REGION_CONTEXT_KEY, this.id);
	}

	/**
	 * Initialises {@link FunctionalRole}s.
	 * 
	 * @param allocation
	 * @param competition
	 * @param demand
	 * @param bts
	 * @param frs
	 * @param initialCells
	 */
	public Region(AllocationModel allocation, boolean handleAmbulantAgents, CompetitivenessModel competition,
			DemandModel demand, Set<BehaviouralType> bts,
			Set<FunctionalRole> frs, Cell... initialCells) {
		this(initialCells);

		this.addBehaviouralTypes(bts);
		this.addfunctionalRoles(frs);

		this.allocation = allocation;
		this.handleAmbulantAgents = handleAmbulantAgents;
		this.competition = competition;
		this.demand = demand;
	}

	public Region(Cell... initialCells) {
		this(Arrays.asList(initialCells));
	}

	public Region(Collection<Cell> initialCells) {
		this();
		cells.addAll(initialCells);
		available.addAll(initialCells);
		for (Cell c : initialCells) {
			updateExtent(c);
		}
	}

	/**
	 * @return the random
	 */
	public RegionalRandom getRandom() {
		return random;
	}

	/**
	 * @return the geoFactory
	 */
	public GeometryFactory getGeoFactory() {
		if (this.geoFactory == null) {
			// geometry factory with floating precision model (default)
			this.geoFactory =
					new GeometryFactory(new PrecisionModel(), 32632);
		}
		return geoFactory;
	}

	/**
	 * @return the geography
	 */
	public Geography<Object> getGeography() {


		// gt-opengis-9.0.jar must be included before geoapi-20050403.jar!
		// Otherwise this method throws an uncatchable error.

		if (this.geography == null) {

			try {
				// Causes the CRS factory to apply (longitude, latitude) order of
				// axis:
				// TODO
				// System.setProperty(GeoTools.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				//  "true");
				GeographyParameters<Object> geoParams = new GeographyParameters<Object>();
				geoParams.setCrs((String) PmParameterManager
						.getParameter(GeoPa.CRS));

				String crsCode = geoParams.getCrs();
				this.geography = new DefaultGeography<Object>(this.id
						+ GEOGRAPHY_NAME_EXTENSION,
						crsCode);

				this.geography.setAdder(geoParams.getAdder());

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Geography CRS: " + this.geography.getCRS());
				}
				// LOGGING ->
			} catch (Exception e) { 

				logger.info("Geography is null and failed to generate one");
				e.printStackTrace();

			}

		}

		return this.geography;
	}

	/**
	 * @return the rinfo
	 */
	public RunInfo getRinfo() {
		return rinfo;
	}

	MoreNetworkService<SocialAgent, ? extends MoreEdge<SocialAgent>> networkService;

	/**
	 * @return the networkService
	 */
	public MoreNetworkService<SocialAgent, ? extends MoreEdge<SocialAgent>> getNetworkService() {
		return networkService;
	}

	/**
	 * Sets the network service.
	 * 
	 * @param networkService
	 *        the networkService to set
	 */
	public void setNetworkService(
			MoreNetworkService<SocialAgent, ? extends MoreEdge<SocialAgent>> networkService) {
		this.networkService = networkService;
	}

	MoreNetwork<SocialAgent, MoreEdge<SocialAgent>> network;

	/**
	 * @return the network
	 */
	public MoreNetwork<SocialAgent, MoreEdge<SocialAgent>> getNetwork() {
		return network;
	}

	/**
	 * @param network
	 *        the network to set
	 */
	public void setNetwork(
			MoreNetwork<SocialAgent, MoreEdge<SocialAgent>> network) {
		this.network = network;
	}

	/**
	 * 
	 */
	public void perceiveSocialNetwork() {
		if (this.getNetwork() != null) {

			logger.info("Perceive social network in " + this);

			for (Agent a : this.getAgents()) {
				if (a instanceof SocialAgent) {
					((SocialAgent) a).perceiveSocialNetwork();
				}
			}

			for (RegionHelper helper : this.helpers.values()) {
				if (helper instanceof SocialRegionHelper) {
					((SocialRegionHelper) helper).socialNetworkPerceived(this);
				}
			}
		}
	}

	/*
	 * Initialisation
	 */
	/**
	 * Sets of the Region from a ModelData. Currently just initialises each cell in the region
	 * 
	 * @param data
	 */
	@Override
	public void initialise(ModelData data, RunInfo info, Region r) throws Exception {
		// <- LOGGING
		logger.info("Initialise region " + this + "...");
		// LOGGING ->


		this.data = data;
		this.rinfo = info;

		for (Cell c : cells) {
			c.initialise(data, info, this);
		}

		try {
			for (BehaviouralType type : behaviouralTypesByLabel.values()) {
				if (!type.isInitialised()) {
					type.initialise(data, rinfo, this);
				}
			}

			for (FunctionalRole role : functionalRolesByLabel.values()) {
				if (!role.isInitialised()) {
					role.initialise(data, rinfo, this);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		allocation.initialise(data, info, this);

		logger.info("Allocation model initialised " + this + "...");
		// LOGGING ->
		competition.initialise(data, info, this);
		// <- LOGGING
		logger.info("Competition model initialised " + this + "...");
		// LOGGING ->
		demand.initialise(data, info, this);
		// <- LOGGING
		logger.info("Demand model initialised " + this + "...");



		if (this.institutions != null) {
			this.institutions.initialise(data, info, this);
			info.getSchedule().register(institutions);
			logger.info("Institution model initialised " + this + "...");

		}

		this.initialised = true;
	}

	/*
	 * Function accessors
	 */
	public AllocationModel getAllocationModel() {
		return allocation;
	}

	@Deprecated
	// Deprecated to show it should only be used in tests - normally ask the Region
	public CompetitivenessModel getCompetitionModel() {
		return competition;
	}

	public CompetitivenessModel getCompetitionModelCopy() {
		if (this.competition == null) {
			throw new IllegalStateException("Competition model has not been set at region " + this);
		}
		return competition.getDeepCopy();
	}

	public DemandModel getDemandModel() {
		return demand;
	}

	public void setDemandModel(DemandModel d) {
		this.demand = d;
	}

	public void setAllocationModel(AllocationModel d) {
		this.allocation = d;
	}

	public void setCompetitivenessModel(CompetitivenessModel d) {
		this.competition = d;
	}

	/**
	 * 
	 */
	public void clearBehaviouralTypes() {
		behaviouralTypesByLabel.clear();
		behaviouralTypesBySerialId.clear();
	}

	/**
	 * Behavioural types need to be initialised when added after the region has
	 * been initialised!
	 * 
	 * @param types
	 */
	public void addBehaviouralTypes(Collection<BehaviouralType> types) {
		for (BehaviouralType type : types) {

			if (behaviouralTypesByLabel.containsKey(type.getLabel())) {
				logger.warn("New Behavioural Type overwrites existing one with label "
						+ type.getLabel());
			}
			behaviouralTypesByLabel.put(type.getLabel(), type);

			if (behaviouralTypesBySerialId.containsKey(type.getSerialID())) {
				logger.warn("New Behavioural Type overwrites existing one with serial ID "
						+ type.getSerialID());
			}
			behaviouralTypesBySerialId.put(type.getSerialID(), type);
		}
	}

	/**
	 * 
	 */
	public void clearFunctionalRoles() {
		functionalRolesByLabel.clear();
		functionalRolesBySerialId.clear();
	}

	/**
	 * Functions roles need to be initialised when added after the region has
	 * been initialised!
	 * 
	 * @param roles
	 */
	public void addfunctionalRoles(Collection<FunctionalRole> roles) {
		for (FunctionalRole role : roles) {
			if (functionalRolesByLabel.containsKey(role.getLabel())) {
				logger.warn("New Functional Role overwrites existing one with label "
						+ role.getLabel());
			}
			functionalRolesByLabel.put(role.getLabel(), role);

			UIdentifyCallerException.setOmitLineNumbers(false);
			if (functionalRolesBySerialId.containsKey(role.getSerialID())) {
				logger.warn("New Functional Role overwrites existing one with serial ID "
						+ role.getSerialID(),
						new UIdentifyCallerException());
			}
			functionalRolesBySerialId.put(role.getSerialID(), role);
		}
	}

	/*
	 * Cell methods
	 */
	public void addCell(Cell c) {
		cells.add(c);
		updateExtent(c);
	}

	public Collection<Cell> getCells() {
		return uCells;
	}

	public Collection<Cell> getAvailable() {
		return uAvailable;
	}

	@Deprecated
	public void setAvailable(Cell c) {
		available.add(c);
	}

	/*
	 * Agent methods
	 */

	/**
	 * Note that the returned collections is not modification save. In the risk
	 * of modifications the collection needs to be cached.
	 * 
	 * @return collection of managing agents.
	 */
	public Collection<LandUseAgent> getAgents() {
		return uAgents;
	}

	public Collection<FunctionalRole> getFunctionalRoles() {
		return ufunctionalRolesByLabel.values();
	}

	/**
	 * Also tries to remove agent from list of allocated agents.
	 * 
	 * @param agent
	 */
	public void setAmbulant(LandUseAgent agent) {
		allocatedAgents.remove(agent);

		// check whether too remove agent or make ambulant
		if (handleAmbulantAgents) {
			ambulantAgents.add(agent);
		}
	}

	/**
	 * Resets ownership of cells the agent still owns. Removes the given agent
	 * from both allocated and ambulant agent list.
	 * 
	 * @param agent
	 */
	public void removeAgent(LandUseAgent agent) {
		for (Cell c : agent.getCells()) {
			c.setOwner(Agent.NOT_MANAGED);
			c.resetSupply();
			available.add(c);
			demand.agentChange(c);
		}

		allocatedAgents.remove(agent);
		ambulantAgents.remove(agent);

		agent.die();

		for (RegionHelper helper : this.helpers.values()) {
			if (helper instanceof PopulationRegionHelper) {
				((PopulationRegionHelper) helper).agentRemoved(agent);
			}
		}

		for (RegionHelper helper : this.helpers.values()) {
			if (helper instanceof CleanupRegionHelper) {
				((CleanupRegionHelper) helper).cleanUpAgent(this, agent);
			}
		}
	}

	public void cleanupAgents() {
		for (RegionHelper helper : this.helpers.values()) {
			if (helper instanceof CleanupRegionHelper) {
				((CleanupRegionHelper) helper).cleanUp(this);
			}
		}
	}


	/*
	 * Regions methods
	 */
	@Override
	public Iterable<Region> getAllRegions() {
		return uRegions;
	}

	/**
	 * Returns a new {@link LinkedHashSet}.
	 * 
	 * @see org.volante.abm.data.Regions#getAllAllocatedAgents()
	 */
	@Override
	public Collection<LandUseAgent> getAllAllocatedAgents() {
		return new LinkedHashSet<LandUseAgent>(this.allocatedAgents);
	}

	/**
	 * Returns a new {@link LinkedHashSet}.
	 * 
	 * @see org.volante.abm.data.Regions#getAllAmbulantAgents()
	 */
	@Override
	public Collection<LandUseAgent> getAllAmbulantAgents() {
		return new LinkedHashSet<LandUseAgent>(this.ambulantAgents);
	}

	@Override
	public Iterable<Cell> getAllCells() {
		return uCells;
	}

	@Override
	public Map<String, BehaviouralType> getBehaviouralTypeMapByLabel() {
		return uBehaviouralTypesByLabel;
	}

	@Override
	public Map<Integer, BehaviouralType> getBehaviouralTypeMapBySerialId() {
		return uBehaviouralTypesBySerialId;
	}

	@Override
	public Map<String, FunctionalRole> getFunctionalRoleMapByLabel() {
		return ufunctionalRolesByLabel;
	}

	@Override
	public Map<Integer, FunctionalRole> getFunctionalRoleMapBySerialId() {
		return uFunctionalRolesBySerialId;
	}

	/*
	 * Convenience methods
	 */
	/**
	 * Gets the competitiveness of the given services on the given cell for the
	 * current demand model and level of demand
	 * 
	 * @param fr
	 * @param c
	 * @return competitiveness for the given potential agent on the given cell
	 */
	public double getCompetitiveness(FunctionalRole fr, Cell c) {
		if (hasCompetitivenessAdjustingInstitution()) {
			UnmodifiableNumberMap<Service> provision = fr.getExpectedSupply(c);
			// same as getUnadjustedCompetitiveness() but this way omits
			// calculating provision twice:
			double comp = competition.getCompetitiveness(demand, provision, c);
			// same as getUnadjustedCompetitiveness() but this way omits
			// calculating provision twice:
			return institutions.adjustCompetitiveness(fr, c, provision, comp);
		} else {
			return getUnadjustedCompetitiveness(fr, c);
		}
	}

	/**
	 * Gets the competitiveness of the given services on the given cell for the current demand model and level of demand
	 * 
	 * @param fc
	 * @param c
	 * @return competitiveness for the given potential agent on the given cell
	 */
	public double getCompetitiveness(FunctionalComponent fc, Cell c) {
		if (hasCompetitivenessAdjustingInstitution()) {
			UnmodifiableNumberMap<Service> provision = fc.getExpectedSupply(c);
			// same as getUnadjustedCompetitiveness() but this way omits
			// calculating provision twice:
			double comp = competition.getCompetitiveness(demand, provision, c);
			// same as getUnadjustedCompetitiveness() but this way omits
			// calculating provision twice:
			return institutions.adjustCompetitiveness(fc.getFR(), c, provision, comp);
		} else {
			return getUnadjustedCompetitiveness(fc, c);
		}
	}

	/**
	 * Just used for displays and checking to see the effect without institutions
	 * 
	 * @param fr
	 * @param c
	 * @return unadjusted competitiveness for the given potential agent on the given cell
	 */
	public double getUnadjustedCompetitiveness(FunctionalRole fr, Cell c) {
		return competition.getCompetitiveness(demand, fr.getExpectedSupply(c), c);
	}

	/**
	 * Just used for displays and checking to see the effect without institutions
	 * 
	 * @param agent
	 * @param c
	 * @return unadjusted competitiveness for the given potential agent on the given cell
	 */
	public double getUnadjustedCompetitiveness(FunctionalComponent fc, Cell c) {
		return competition.getCompetitiveness(demand, fc.getExpectedSupply(c), c);
	}

	/**
	 * Gets the competitiveness of the cell's current production for the current demand model and levels of demand
	 * 
	 * @param c
	 * @return get competitiveness of given cell
	 */
	public double getCompetitiveness(Cell c) {
		double comp = getUnadjustedCompetitiveness(c); // before institution intervenes

		if (hasCompetitivenessAdjustingInstitution()) {
			FunctionalRole a = c.getOwner() == null ? null : c.getOwner()
					.getFC().getFR();
			return institutions.adjustCompetitiveness(a, c, c.getSupply(), comp);
		} else {
			return comp;
		}
	}

	/**
	 * Just used for displays and checking, so see the effect without
	 * institutions
	 * 
	 * @param c
	 * @return unadjusted competitiveness for the given cell
	 */
	public double getUnadjustedCompetitiveness(Cell c) {
		if (competition == null || demand == null) {
			return Double.NaN;
		}
		return competition.getCompetitiveness(demand, c.getSupply(), c);
	}

	/**
	 * Applied e.g. in optimisation allocation model
	 * 
	 * @param supply
	 * @return competitiveness of the given supply
	 */
	public double getUnadjustedCompetitiveness(UnmodifiableNumberMap<Service> supply) {
		if (competition == null || demand == null) {
			return Double.NaN;
		}
		return competition.getCompetitiveness(demand, supply);
	}

	/**
	 * Sets the ownership of all the cells to the given agent Adds the agent to the region, removes
	 * any agents with no cells left
	 * 
	 * @param a
	 * @param cells
	 */
	public void setOwnership(LandUseAgent a, Cell... cells) {
		a.setRegion(this);
		for (Cell c : cells) {
			LandUseAgent cur = c.getOwner();
			if (cur != null) {
				log.trace(" removing agent " + cur + " from cell " + c);
				cur.removeCell(c);
				if (cur.notAllocated()) {
					log.trace("also removing entire agent " + cur);
					setAmbulant(cur);
				}
			}

			a.addCell(c);
			c.setOwner(a);
			a.updateSupply();
			a.updateCompetitiveness();

			if (log.isTraceEnabled()) {
				log.trace(" adding agent " + a + " to cell " + c
						+ " (competitiveness: "
						+ a.getProperty(AgentPropertyIds.COMPETITIVENESS) + ")");
			}

			available.remove(c);
			if (demand != null) {
				demand.agentChange(c); // could be null in initialisation
			}
			if (log.isDebugEnabled()
					&& a.getProperty(AgentPropertyIds.COMPETITIVENESS) < a
					.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD)) {
				log.debug(" Cell below new " + a.getID()
				+ "'s GivingUp threshold: comp = "
				+ a.getProperty(AgentPropertyIds.COMPETITIVENESS)
				+ " GU = "
				+ a.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD));
			}
			if (log.isTraceEnabled()) {
				log.trace(" owner is now " + a);
			}
		}

		if (!this.allocatedAgents.contains(a) && !this.ambulantAgents.contains(a)) {
			for (ActionReporter reporter : this.registeredPaReporter) {
				reporter.registerAtAgent(a);
			}
		}

		allocatedAgents.add(a);
		ambulantAgents.remove(a);

		for (RegionHelper helper : this.helpers.values()) {
			if (helper instanceof PopulationRegionHelper) {
				((PopulationRegionHelper) helper).agentAdded(a);
			}
		}
	}

	/**
	 * Similar to setOwnership, but doesn't assume that anything is working yet. Useful for adding
	 * an initial population of agents
	 * 
	 * @param a
	 * @param cells
	 */
	public void setInitialOwnership(LandUseAgent a, Cell... cells) {
		for (Cell c : cells) {
			a.addCell(c);
			c.setOwner(a);
			if (a != null) {
				available.remove(c);
			}
		}
		if (a != null && a != Agent.NOT_MANAGED) {
			allocatedAgents.add(a);
			ambulantAgents.remove(a);
		}
	}

	/**
	 * Sets all of the unmanaged cells to be available. Bit of a hack
	 */
	public void makeUnmanagedCellsAvailable() {
		for (Cell c : cells) {
			if (c.getOwner() == Agent.NOT_MANAGED || c.getOwner() == null) {
				available.add(c);
			}
		}
	}

	@Override
	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
		this.peristerContextExtra.put(ABMPersister.REGION_CONTEXT_KEY, id);
	}

	private void updateExtent(Cell c) {
		extent.update(c);
	}

	@Override
	public Extent getExtent() {
		return extent;
	}

	/**
	 * Called after all cells in the region have been created, to allow building a table of them
	 */
	public void cellsCreated()
	{
		log.info("Update Extent...");
		for (Cell c : cells) {
			// <- LOGGING
			if (log.isDebugEnabled()) {
				log.debug("Update extent by cell " + c);
			}
			// LOGGING ->

			updateExtent(c);
		}
		cellTable = TreeBasedTable.create(); // Would rather use the ArrayTable below, but requires
		// setting up ranges, and the code below doesn't
		// work
		/*
		 * cellTable = ArrayTable.create( Ranges.open( extent.minY, extent.maxY ).asSet(
		 * DiscreteDomains.integers() ), Ranges.open( extent.minX, extent.maxX ).asSet(
		 * DiscreteDomains.integers() ) );
		 */
		for (Cell c : cells) {
			cellTable.put(c.getY(), c.getX(), c);
		}
	}

	/**
	 * Returns the cell with the given x and y coordinates. Returns null if no
	 * cells are present or the table has not been built yet.
	 * 
	 * @param x
	 * @param y
	 * @return cell of given coordinates
	 */
	public Cell getCell(int x, int y) {
		if (cellTable == null) {
			return null;
		}
		return cellTable.get(y, x);
	}

	@Override
	public int getNumCells() {
		return cells.size();
	}

	/**
	 * Returns More-neighbourhood of the given cell.
	 * 
	 * @param c
	 * @return set of cell belonging the the given cell's More neighbourhood
	 */
	public Set<Cell> getAdjacentCells(Cell c) {
		if (cellTable == null) {
			this.cellsCreated();
		}
		Set<Cell> adjacent = new HashSet<Cell>();

		for (int x = c.getX() - 1; x <= c.getX() + 1; x++) {
			for (int y = c.getY() - 1; y <= c.getY() + 1; y++) {
				if ((x != c.getX() || y != c.getY())
						&& this.getCell(x, y) != null) {
					adjacent.add(this.getCell(x, y));
				}
			}
		}
		return adjacent;
	}

	public boolean hasInstitutions() {
		return institutions == null ? false : institutions.hasInstitutions();
	}

	public boolean doesRequireEffectiveCapitalData() {
		return requiresEffectiveCapitalData;
	}

	public boolean hasCompetitivenessAdjustingInstitution() {
		return hasCompetitivenessAdjustingInstitution;
	}

	public void setRequiresEffectiveCapitalData() {
		this.requiresEffectiveCapitalData = true;
	}

	public void setHasCompetitivenessAdjustingInstitution() {
		this.hasCompetitivenessAdjustingInstitution = true;
	}

	/**
	 * @return institutions
	 */
	public Institutions getInstitutions() {
		if (this.institutions == null) {
			this.institutions = new Institutions();
			try {
				if (this.initialised) {
					this.institutions.initialise(this.data, this.rinfo, this);
					this.rinfo.getSchedule().register(institutions);
				}
			} catch (Exception exception) {
				logger.error("Error while initialising Institutions");
				exception.printStackTrace();
			}
		}
		return institutions;
	}

	public ModelData getModelData() {
		return data;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getID();
	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void preTick() {
		this.maxGivingUpDiff = null;
		for (RegionHelper helper : this.helpers.values()) {
			if (helper instanceof PreTickRegionHelper) {
				((PreTickRegionHelper) helper).preTick(this);
				;
			}
		}

		if (demand.getDemand() != null) {
			for (Service s : data.services) {
				rinfo.getParamRepos().addParameter(this, "Demand_" + s, demand.getDemand().get(s));
			}
		}
	}

	/**
	 * @param id
	 * @param rHelper
	 * @return see {@link HashMap#put(Object, Object)}
	 */
	public RegionHelper registerHelper(Object id, RegionHelper rHelper) {
		return this.helpers.put(id, rHelper);
	}

	/**
	 * @param id
	 * @return region helper with given ID
	 */
	public RegionHelper getHelper(Object id) {
		return this.helpers.get(id);
	}

	/**
	 * Removes the {@link RegionHelper} which was registered by the given ID object.
	 * 
	 * @param id
	 * @return removed region helper
	 */
	public RegionHelper removeHelper(Object id) {
		return this.helpers.remove(id);
	}

	public Map<String, String> getPersisterContextExtra() {
		return this.peristerContextExtra;
	}

	/**
	 * Let the given {@link ActionReporter} register at institutions and agents.
	 * 
	 * @param reporter
	 */
	public void registerPaReporter(ActionReporter reporter) {
		this.registeredPaReporter.add(reporter);

		if (this.institutions != null) {
			this.institutions.registerPaReporter(reporter);
		}

		for (Agent agent : this.getAgents()) {
			reporter.registerAtAgent(agent);
		}
	}

	public Map<FunctionalRole, Double> getMaxGivingUpThresholdDeviation() {
		if (this.maxGivingUpDiff == null) {
			this.maxGivingUpDiff = new HashMap<>();
			double diff = 0.0;
			for (LandUseAgent a : this.getAllAllocatedAgents()) {
				diff = a.getProperty(AgentPropertyIds.GIVING_UP_THRESHOLD)
						- a.getProperty(AgentPropertyIds.COMPETITIVENESS);
				this.maxGivingUpDiff.put(a.getFC().getFR(),
						new Double(Math.max(diff,
								this.maxGivingUpDiff.containsKey(a.getFC().getFR())
								? this.maxGivingUpDiff.get(a.getFC().getFR())
										: 0)));
			}
		}
		return this.maxGivingUpDiff;
	}
}
