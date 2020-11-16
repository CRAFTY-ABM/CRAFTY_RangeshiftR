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
 * Created by Sascha Holzhauer on 12 Feb 2016
 */
package org.volante.abm.output;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.example.GlobalBtRepository;
import org.volante.abm.institutions.global.GlobalInstitution;
import org.volante.abm.institutions.global.GlobalInstitutionsRegistry;
import org.volante.abm.lara.CobraLAgentComp;
import org.volante.abm.output.ActionCSVOutputter.TableEntry;
import org.volante.abm.schedule.PrePreTickAction;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.decision.LaraDecisionData;


/**
 * Outputs {@link CraftyPa} with additional information (trigger, score) for {@link Agent}s whose ID matches the given
 * <code>agentpattern</code>. Registers at {@link CobraLAgentComp}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class ActionCSVOutputter extends TableOutputter<TableEntry> implements ActionReporter, GloballyInitialisable,
		PrePreTickAction {

	@Attribute(required = false)
	boolean addTick = true;

	@Attribute(required = false)
	boolean addBtLabel = true;

	/**
	 * Adds actions of global institutions to regional files.
	 */
	@Attribute(required = false)
	boolean addGlobalActionsToRegions = false;

	/**
	 * Outputs actions of global institutions to a separate file. If both addGlobalActionsToRegions and
	 * outputGlobalActionsSeparately are true, global actions will be output both into region files and in a separate
	 * file.
	 */
	@Attribute(required = false)
	boolean outputGlobalActionsSeparately = false;

	@ElementList(required = false, entry = "column", inline = true)
	List<TableColumn<TableEntry>> additionalColumns = new ArrayList<>();

	/**
	 * Applied to {@link Agent#getID()}
	 */
	@Attribute(required = false)
	protected String agentpattern = ".*";

	/**
	 * Only considered when <code>perRegion==false</code> (otherwise, regions are considered in column headers).
	 */
	@Attribute(required = false)
	boolean addRegion = true;

	Map<Region, Set<TableEntry>> actions = new HashMap<>();
	Map<Region, Set<TableEntry>> lastActions = new HashMap<>();

	public class TableEntry {
		protected Agent agent;
		protected DecisionTrigger trigger;
		protected LaraDecisionConfiguration dConfig;
		protected LaraDecisionData<?, CraftyPa<?>> dData;
		protected CraftyPa<?> pa;
		protected double score;
		protected boolean selected;

		protected TableEntry(Agent agent, DecisionTrigger trigger, LaraDecisionConfiguration dConfig,
		        LaraDecisionData<?, CraftyPa<?>> dData, CraftyPa<?> pa, double score, boolean selected) {
			this.agent = agent;
			this.trigger = trigger;
			this.dConfig = dConfig;
			this.dData = dData;
			this.pa = pa;
			this.score = score;
			this.selected = selected;
		}

		public Agent getAgent() {
			return this.agent;
		}

		public DecisionTrigger getDTrigger() {
			return this.trigger;
		}

		public LaraDecisionConfiguration getDConfig() {
			return this.dConfig;
		}

		public LaraDecisionData<?, CraftyPa<?>> getDecisionData() {
			return this.dData;
		}

		public CraftyPa<?> getPa() {
			return this.pa;
		}
	}

	/**
	 * Register at agents, regional institutions, and global institutions.
	 * 
	 * @see org.volante.abm.serialization.GloballyInitialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		info.getSchedule().register(this);
		for (Region r : data.getRootRegionSet().getAllRegions()) {
			r.registerPaReporter(this);
		}
		for (GlobalInstitution institution : GlobalInstitutionsRegistry.getInstance().getGlobalInstitutions()) {
			if (institution instanceof Agent) {
				this.registerAtAgent((Agent) institution);
			}
		}
	}

	@Override
	public void doOutput(Regions regions) {
		if (perRegion) {

			for (Region r : regions.getAllRegions()) {
				writeFile(r);
			}
			if (this.outputGlobalActionsSeparately) {
				writeFile(GlobalBtRepository.getInstance().getPseudoRegion());
			}
		} else {
			writeFile(regions);
		}
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "Actions";
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#setOutputManager(org.volante.abm.output.Outputs)
	 */
	@Override
	public void setOutputManager(Outputs outputs) {
		super.setOutputManager(outputs);

		if (addTick) {
			addColumn(new TickColumn<TableEntry>());
		}

		if (addRegion && perRegion) {
			addColumn(new RegionsColumn<TableEntry>());
		}

		if (addBtLabel) {
			addColumn(new BtLabelColumn());
		}

		addColumn(new AgentColumn());
		addColumn(new TriggerColumn());
		addColumn(new ActionColumn());
		addColumn(new ScoreColumn());
		addColumn(new SelectedColumn());

		for (TableColumn<TableEntry> column : this.additionalColumns) {
			addColumn(column);
		}
	}


	public class BtLabelColumn implements TableColumn<TableEntry> {

		public BtLabelColumn() {
		}

		@Override
		public String getHeader() {
			return "BT";
		}

		@Override
		public String getValue(TableEntry te, ModelData data, RunInfo info, Regions rs) {
			return te.agent.getFC().getFR().getLabel();
		}
	}

	public class AgentColumn implements TableColumn<TableEntry> {

		public AgentColumn() {
		}

		@Override
		public String getHeader() {
			return "Agent";
		}

		@Override
		public String getValue(TableEntry te, ModelData data, RunInfo info, Regions rs) {
			return te.agent.getID();
		}
	}

	public class TriggerColumn implements TableColumn<TableEntry> {

		public TriggerColumn() {
		}

		@Override
		public String getHeader() {
			return ("Trigger");
		}

		/**
		 * @see org.volante.abm.output.TableColumn#getValue(java.lang.Object, org.volante.abm.data.ModelData,
		 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
		 */
		public String getValue(TableEntry te, ModelData data, RunInfo info, Regions rs) {
			return te.trigger.toString();
		}
	}

	public class ActionColumn implements TableColumn<TableEntry> {

		public ActionColumn() {
		}

		@Override
		public String getHeader() {
			return ("Action");
		}

		/**
		 * @see org.volante.abm.output.TableColumn#getValue(java.lang.Object, org.volante.abm.data.ModelData,
		 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
		 */
		public String getValue(TableEntry te, ModelData data, RunInfo info, Regions rs) {
			return te.pa.getKey();
		}
	}

	public class ScoreColumn implements TableColumn<TableEntry> {

		public ScoreColumn() {
		}

		@Override
		public String getHeader() {
			return ("Score");
		}

		/**
		 * @see org.volante.abm.output.TableColumn#getValue(java.lang.Object, org.volante.abm.data.ModelData,
		 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
		 */
		public String getValue(TableEntry te, ModelData data, RunInfo info, Regions rs) {
			return "" + te.score;
		}
	}

	public class SelectedColumn implements TableColumn<TableEntry> {

		public SelectedColumn() {
		}

		@Override
		public String getHeader() {
			return ("Selected");
		}

		/**
		 * @see org.volante.abm.output.TableColumn#getValue(java.lang.Object, org.volante.abm.data.ModelData,
		 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
		 */
		public String getValue(TableEntry te, ModelData data, RunInfo info, Regions rs) {
			return te.selected ? "1" : "0";
		}
	}

	public void registerAtAgent(Agent agent) {
		if (agent.getID().matches(agentpattern) && agent.getBC() instanceof LaraBehaviouralComponent) {
			((CobraLAgentComp) ((LaraBehaviouralComponent) agent.getBC()).getLaraComp()).addPaReporter(this);
		}
	}

	/**
	 * @see org.volante.abm.output.ActionReporter#setActionInfos(org.volante.abm.agent.Agent,
	 *      org.volante.abm.decision.trigger.DecisionTrigger,
	 *      de.cesr.lara.components.decision.LaraDecisionConfiguration, org.volante.abm.decision.pa.CraftyPa, double)
	 */
	@Override
	public void setActionInfos(Agent agent, DecisionTrigger trigger, LaraDecisionConfiguration dConfig,
	        LaraDecisionData<?, CraftyPa<?>> dData, CraftyPa<?> pa, double score, boolean selected) {
		if (!this.actions.containsKey(pa.getAgent().getAgent().getRegion())) {
			this.actions.put(pa.getAgent().getAgent().getRegion(), new HashSet<TableEntry>());
		}
		this.actions.get(pa.getAgent().getAgent().getRegion()).add(
		        new TableEntry(agent, trigger, dConfig, dData, pa, score, selected));
	}

	/**
	 * @see org.volante.abm.output.ActionReporter#setActionInfos(org.volante.abm.agent.Agent,
	 *      org.volante.abm.decision.trigger.DecisionTrigger,
	 *      de.cesr.lara.components.decision.LaraDecisionConfiguration, org.volante.abm.decision.pa.CraftyPa, double)
	 */
	@Override
	public void setActionInfos(Agent agent, DecisionTrigger trigger, LaraDecisionConfiguration dConfig, CraftyPa<?> pa,
	        double score, boolean selected) {
		this.setActionInfos(agent, trigger, dConfig, null, pa, score, selected);
	}

	/**
	 * @see org.volante.abm.output.ActionReporter#setActionInfos(org.volante.abm.agent.Agent,
	 *      org.volante.abm.decision.trigger.DecisionTrigger,
	 *      de.cesr.lara.components.decision.LaraDecisionConfiguration, org.volante.abm.decision.pa.CraftyPa, double,
	 *      boolean)
	 */
	@Override
	public void setActionInfos(Agent agent, DecisionTrigger trigger, LaraDecisionConfiguration dconfig, CraftyPa<?> pa,
			double score) {
		this.setActionInfos(agent, trigger, dconfig, pa, score, true);
	}


	/**
	 * @see org.volante.abm.output.ActionReporter#setActionInfos(org.volante.abm.agent.Agent,
	 *      org.volante.abm.decision.pa.CraftyPa)
	 */
	public void setActionInfos(Agent agent, CraftyPa<?> pa) {
		TableEntry lastEntry = null;
		for (TableEntry te : this.lastActions.get(pa.getAgent().getAgent().getRegion())) {
			if (te.getPa().equals(pa)) {
				lastEntry = te;
				break;
			}
		}
		if (lastEntry == null) {
			log.warn("There is no action stored at last tick for this renewed action: " + pa + "!");
		} else {
			this.actions.get(pa.getAgent().getAgent().getRegion()).add(
			        new TableEntry(agent, lastEntry.trigger, lastEntry.dConfig, lastEntry.dData, pa, lastEntry.score,
			                true));
		}
	}

	/**
	 * @see org.volante.abm.output.TableOutputter#getData(org.volante.abm.data.Regions)
	 */
	@Override
	public Iterable<TableEntry> getData(Regions r) {
		Set<TableEntry> tes = new HashSet<>();
		for (Region region : r.getAllRegions()) {
			if (actions.containsKey(region)) {
				tes.addAll(actions.get(region));
			}
		}
		if (addGlobalActionsToRegions) {
			if (actions.containsKey(GlobalBtRepository.getInstance().getPseudoRegion())) {
				tes.addAll(actions.get(GlobalBtRepository.getInstance().getPseudoRegion()));
			}
		}
		return tes;
	}

	/**
	 * @see org.volante.abm.schedule.PrePreTickAction#prePreTick()
	 */
	@Override
	public void prePreTick() {
		for (Region region : actions.keySet()) {
			if (!this.lastActions.containsKey(region)) {
				this.lastActions.put(region, new HashSet<TableEntry>());
			}
			this.lastActions.get(region).clear();
			this.lastActions.get(region).addAll(actions.get(region));
			actions.get(region).clear();
		}
	}
}
