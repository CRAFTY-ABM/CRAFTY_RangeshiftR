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
package org.volante.abm.agent;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.bt.BehaviouralComponent;
import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.agent.property.PropertyRegistry;
import org.volante.abm.agent.property.DoublePropertyProvider;
import org.volante.abm.agent.property.DoublePropertyProviderComp;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.example.AgentPropertyIds;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * Contains useful functionality for building agents. Covers: * having an age and increasing it by 1
 * each year * having a Region and a set of Cells * knowing the current service provision level and
 * competitiveness
 * 
 * @author dmrust
 * 
 */
public abstract class AbstractLandUseAgent implements LandUseAgent {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(AbstractLandUseAgent.class);

	@ElementMap(inline = true, entry = "property", attribute = true, required = false, key = "param", valueType = Double.class)
	Map<String, Object> params = new HashMap<String, Object>();

	protected FunctionalComponent functionalComp = null;

	protected BehaviouralComponent behaviouralComp = null;

	protected DoublePropertyProvider propertyProvider;

	protected DoubleMap<Service> productivity = null;

	protected String				id						= "Default";

	protected Region				region					= null;

	protected Cell homecell = null;
	protected Set<Cell>				cells					= new HashSet<Cell>();
	Set<Cell>						uCells					= Collections.unmodifiableSet(cells);


	public AbstractLandUseAgent(Region region) {
		this.region = region;
		this.propertyProvider = new DoublePropertyProviderComp();

		// initialise important agent properties:
		this.setProperty(AgentPropertyIds.AGE, 1.0);
		this.setProperty(AgentPropertyIds.COMPETITIVENESS, 0.0);

		for (Entry<String, Object> property : params.entrySet()) {
			if (PropertyRegistry.get(property.getKey()) != null) {
				if (property.getValue() instanceof Number) {
					this.propertyProvider.setProperty(PropertyRegistry.get(property.getKey()),
							(Double) property.getValue());
				}
			}
		}
		this.initHook();
	}

	protected void initHook() {

	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#isProvided(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public boolean isProvided(PropertyId property) {
		return this.propertyProvider.isProvided(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#getProperty(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public Double getProperty(PropertyId property) {
		return this.propertyProvider.getProperty(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#setProperty(org.volante.abm.agent.property.PropertyId,
	 *      Double)
	 */
	@Override
	public void setProperty(PropertyId propertyId, Double value) {
		this.propertyProvider.setProperty(propertyId, value);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#getProperty(org.volante.abm.agent.property.PropertyId)
	 */
	@Override
	public Object getObjectProperty(PropertyId property) {
		return this.propertyProvider.getObjectProperty(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#setProperty(org.volante.abm.agent.property.PropertyId,
	 *      Double)
	 */
	@Override
	public void setObjectProperty(PropertyId propertyId, Object value) {
		this.propertyProvider.setObjectProperty(propertyId, value);
	}

	/**
	 * @see org.volante.abm.agent.Agent#getBC()
	 */
	@Override
	public BehaviouralComponent getBC() {
		return this.behaviouralComp;
	}

	/**
	 * @see org.volante.abm.agent.Agent#getFC()
	 */
	@Override
	public FunctionalComponent getFC() {
		return this.functionalComp;
	}

	/**
	 * @see org.volante.abm.agent.Agent#setBC(org.volante.abm.agent.bt.BehaviouralComponent)
	 */
	@Override
	public void setBC(BehaviouralComponent bt) {
		this.behaviouralComp = bt;
	}

	/**
	 * @see org.volante.abm.agent.Agent#setFC(org.volante.abm.agent.fr.FunctionalComponent)
	 */
	@Override
	public void setFC(FunctionalComponent fr) {
		this.functionalComp = fr;
	}

	/*
	 * Generally useful methods
	 */
	@Override
	public void addCell(Cell c) {
		cells.add(c);
	}

	@Override
	public void removeCell(Cell c) {
		cells.remove(c);
	}


	@Override
	public Set<Cell> getCells() {
		return uCells;
	}

	public void setHomeCell(Cell homecell) {
		this.homecell = homecell;
	}

	/**
	 * Return simply the first cell of an iterator of cells.
	 * 
	 * @see org.volante.abm.agent.Agent#getHomeCell()
	 */
	@Override
	public Cell getHomeCell() {
		return this.homecell;
	}

	@Override
	public boolean notAllocated() {
		return cells.size() == 0;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public String toString() {
		return getID()
 + " (" + (this.getBC() == null ? "None" : this.getBC().getType().getLabel()) + "/"
				+ (this.getFC() == null ? "None" : this.getFC().getFR().getLabel())
				+ (this.getHomeCell() != null ? "@" + this.getHomeCell() : "") + ") #" + hashCode();
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void tickStartUpdate() {
		this.propertyProvider.setProperty(AgentPropertyIds.AGE,
				this.propertyProvider.getProperty(AgentPropertyIds.AGE) + 1);
		this.behaviouralComp.triggerDecisions(this);
 
	}

	@Override
	public void tickEndUpdate() {
	}

	@Override
	public void setRegion(Region r) {
		region = r;
	}

	@Override
	public Region getRegion() {
		return region;
	}

	public void giveUp() {
		region.removeAgent(this);
	}

	/**
	 * Uses the current level of production in each Cell to update competitiveness (hence
	 * independent of the Agent)
	 */
	@Override
	public void updateCompetitiveness() {
		double comp = 0;

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Update competitiveness of " + this);
		}
		// LOGGING ->

		if (this.getFC() instanceof CompetitivenessUpdatingFC) { // FunctionalComponent (FC)
			comp = ((CompetitivenessUpdatingFC) this.getFC()).getUpdatedCompetitiveness(this);
		} else {
			for (Cell c : cells) { // @TODO parallelise
				comp += region.getCompetitiveness(c);
			}
		}

		this.propertyProvider.setProperty(AgentPropertyIds.COMPETITIVENESS, comp / cells.size());
	}
}
