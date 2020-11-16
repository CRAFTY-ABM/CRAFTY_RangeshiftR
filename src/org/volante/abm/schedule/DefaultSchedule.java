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
 */
package org.volante.abm.schedule;


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.DefaultSocialLandUseAgent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.example.RegionalDemandModel;
import org.volante.abm.institutions.global.GlobalInstitution;
import org.volante.abm.institutions.global.GlobalInstitutionsRegistry;
import org.volante.abm.models.WorldSynchronisationModel;
import org.volante.abm.output.Outputs;
import org.volante.abm.schedule.ScheduleStatusEvent.ScheduleStage;
import org.volante.abm.serialization.ModelRunner;
import org.volante.abm.visualisation.ModelDisplays;
import org.volante.abm.visualisation.DefaultModelDisplays;



public class DefaultSchedule implements WorldSyncSchedule {
	static int						idCounter		= 0;

	protected int					id				= idCounter++;

	static Logger logger = Logger.getLogger(DefaultSchedule.class);

	RegionSet						regions			= null;
	int								tick			= 0;
	int								targetTick		= 0;
	int								startTick		= 0;
	int								endTick			= Integer.MAX_VALUE;

	List<PrePreTickAction>			prePreTickActions	= new ArrayList<PrePreTickAction>();
	List<PreTickAction>				preTickActions	= new ArrayList<PreTickAction>();
	List<PostTickAction>			postTickActions	= new ArrayList<PostTickAction>();
	List<FinishAction> finishActions = new ArrayList<FinishAction>();

	Outputs							output			= new Outputs();
	private RunInfo					info			= null;

	List<ScheduleStatusListener>	listeners		= new ArrayList<ScheduleStatusListener>();

	WorldSynchronisationModel		worldSyncModel;

	/*
	 * Constructors
	 */
	public DefaultSchedule() {
	}

	public DefaultSchedule(RegionSet regions) {
		this();
		this.regions = regions;
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		this.info = info;
		output = info.getOutputs();
		info.setSchedule(this);
	}

	@Override
	public void setWorldSyncModel(WorldSynchronisationModel worldSyncModel) {
		this.worldSyncModel = worldSyncModel;
	}

	public WorldSynchronisationModel getWorldSyncModel() {
		return worldSyncModel;
	}

	@Override
	public void tick() {
		logger.info("\n********************\nStart of tick " + tick + "\n********************");
		fireScheduleStatus(new ScheduleStatusEvent(tick, ScheduleStage.PRE_TICK, true));
		info.getPersister().setContext("y", tick + "");

		prePreTickUpdates();

		// Reset the effective capital levels
		for (Cell c : regions.getAllCells()) {
			c.initEffectiveCapitals();
		}

		// check and register for decisions (which are performed at
		// preTickUpdates)
		Set<Agent> allAgents = new LinkedHashSet<Agent>();
		for (Region region : regions.getAllRegions()) {
			allAgents.addAll(region.getAllAllocatedAgents());
			allAgents.addAll(region.getAllAmbulantAgents());
		}

		// E.g. aging and trigger decision
		for (Agent a : allAgents) {
			a.tickStartUpdate();
		}

		for (GlobalInstitution institution : GlobalInstitutionsRegistry.getInstance().getGlobalInstitutions()) {
			if (institution instanceof Agent) {
				((Agent) institution).tickStartUpdate();
			}
		}
		
		for (Region r : regions.getAllRegions()) {
			if (r.hasInstitutions()) {
				r.getInstitutions().tickStartUpdate();
			}
		}

		// e.g. update institutions
		preTickUpdates();


		fireScheduleStatus(new ScheduleStatusEvent(tick,
				ScheduleStage.MAIN_LOOP, true));

		// Allow institutions to update capitals
		for (Region r : regions.getAllRegions()) {
			if (r.hasInstitutions()) {
				r.getInstitutions().updateCapitals();
			}
		}

		// perceive social network if existent:
		for (Region r : regions.getAllRegions()) {
			r.perceiveSocialNetwork();
		}

		// Recalculate agent competitiveness and give up
		if (this.getCurrentTick() > this.getStartTick()) {
			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug("Update agents' competitiveness and consider giving up ...");
			}
			// LOGGING ->

			for (LandUseAgent a : regions.getAllAllocatedAgents()) {
				if (a instanceof InnovativeBC) {
					((InnovativeBC) a).considerInnovationsNextStep(); // consider innovation
				}

				a.updateCompetitiveness();
				a.considerGivingUp(); // consider giving-up
			}

			// Remove any unneeded agents
			for (Region r : regions.getAllRegions()) {
				r.cleanupAgents();
			}

			// Allocate land
			for (Region r : regions.getAllRegions()) {
				r.getAllocationModel().allocateLand(r);
			}
		}

		// Calculate supply
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Update agents' supply...");
		}
		// LOGGING ->

		for (LandUseAgent a : regions.getAllAllocatedAgents()) {
			a.updateSupply();
		}

		// Allow the demand model to update for global supply supply for each region
		for (Region r : regions.getAllRegions()) {
			r.getDemandModel().updateSupply();
		}

		// in order to recalculate residuals (which is done during updateSupply()) and to calculate
		// competitiveness, the market-level residuals must be known:
		if (worldSyncModel != null) {
			this.worldSyncModel.synchronizeNumOfCells(regions);
			this.worldSyncModel.synchronizeDemand(regions);
			this.worldSyncModel.synchronizeSupply(regions);
		}

		for (Region r : regions.getAllRegions()) {
			if (r.getDemandModel() instanceof RegionalDemandModel) {
				((RegionalDemandModel) r.getDemandModel())
				.recalculateResidual();
			}
		}

		for (LandUseAgent a : regions.getAllAllocatedAgents()) {
			a.updateCompetitiveness();
			a.tickEndUpdate();
		}
		// iterate ambulant agents:
		for (Agent a : regions.getAllAmbulantAgents()) {
			a.tickEndUpdate();
		}

		fireScheduleStatus(new ScheduleStatusEvent(tick, ScheduleStage.POST_TICK, true));

		postTickUpdates();

		// Displaying model running information 
  		final ModelDisplays displays = ModelRunner.getLoader().getDisplays(); 
 
   		if (displays != null && (displays instanceof DefaultModelDisplays)) {
   			 
  			DefaultModelDisplays display = (DefaultModelDisplays) displays;
  	  		JFrame frame = display.getFrame();
  	  		String framelabel = "CRAFTY using " + this.info.getScenario() + " scenario in " + Integer.toString(this.getCurrentTick());
  	  		frame.setTitle(framelabel);
  	  		frame.repaint();
 		}
  		
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Number of Agents in total: " + DefaultSocialLandUseAgent.numberAgents);
		}
		// LOGGING ->

		output();
		logger.info("\n********************\nEnd of tick " + tick + "\n********************");
		fireScheduleStatus(new ScheduleStatusEvent(tick, ScheduleStage.PAUSED, false));
		tick++;
	}

	@Override
	public void finish() {
		output.finished();
		this.finishUpdates();
		fireScheduleStatus(new ScheduleStatusEvent(tick, ScheduleStage.FINISHING, true));
	}

	/*
	 * Run controls
	 */

	/**
	 * @see org.volante.abm.schedule.Schedule#runFromTo(int, int)
	 */
	@Override
	public void runFromTo(int start, int end) {
		logger.info("Starting run for set number of ticks");
		logger.info("Start: " + start + ", End: " + end);

		setStartTick(start);
		setEndTick(end);
		run();
		finish();
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#runUntil(int)
	 */
	@Override
	public void runUntil(int target) {
		setTargetTick(target);
		while (tick <= targetTick) {
			tick();
		}
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#run()
	 */
	@Override
	public void run() {
		while (tick <= endTick) {
			tick();
		}
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#setTargetTick(int)
	 */
	@Override
	public void setTargetTick(int target) {
		this.targetTick = target;
	}

	/**
	 * @see org.volante.abm.schedule.Schedule#setEndTick(int)
	 */
	@Override
	public void setEndTick(int end) {
		this.endTick = end;
	}

	@Override
	public int getEndTick() {
		return endTick;
	}

	@Override
	public int getTargetTick() {
		return targetTick;
	}

	@Override
	public int getStartTick() {
		return startTick;
	}

	@Override
	public void setTargetToNextTick() {
		setTargetTick(tick);
	}

	/*
	 * Pre and post tick events and registering
	 */

	private void prePreTickUpdates() {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Pre PreTick\t\t (DefaultSchedule ID " + id + ")");
		}
		// LOGGING ->

		// copy to prevent concurrent modifications:
		List<PrePreTickAction> prePreTickActionsCopy = new ArrayList<PrePreTickAction>(
				prePreTickActions);

		for (PrePreTickAction p : prePreTickActionsCopy) {
			// <- LOGGING
			if (logger.isTraceEnabled()) {
				logger.trace("Do PrePreTick action " + p);
			}
			// LOGGING ->

			p.prePreTick();
		}
	}

	private void preTickUpdates() {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Pre Tick\t\t (DefaultSchedule ID " + id + ")");
		}
		// LOGGING ->

		// copy to prevent concurrent modifications:
		List<PreTickAction> preTickActionsCopy = new ArrayList<PreTickAction>(
				preTickActions);

		for (PreTickAction p : preTickActionsCopy) {
			// <- LOGGING
			if (logger.isTraceEnabled()) {
				logger.trace("Do PreTick action " + p);
			}
			// LOGGING ->

			p.preTick();
		}
	}

	private void postTickUpdates() {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Post Tick\t\t (DefaultSchedule ID " + id + ")");
		}
		// LOGGING ->

		// copy to prevent concurrent modifications:
		List<PostTickAction> postTickActionsCopy = new ArrayList<PostTickAction>(
				postTickActions);

		for (PostTickAction p : postTickActionsCopy) {
			p.postTick();
		}
	}

	private void finishUpdates() {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Finish\t\t (DefaultSchedule ID " + id + ")");
		}
		// LOGGING ->

		// copy to prevent concurrent modifications:
		List<FinishAction> finishActionsCopy = new ArrayList<FinishAction>(
				finishActions);

		for (FinishAction p : finishActionsCopy) {
			p.afterLastTick();
		}
	}

	@Override
	public void register(TickAction o) {
		if (o instanceof PrePreTickAction && !prePreTickActions.contains(o)) {
			prePreTickActions.add((PrePreTickAction) o);
		}
		if (o instanceof PreTickAction && !preTickActions.contains(o)) {
			preTickActions.add((PreTickAction) o);
		}
		if (o instanceof PostTickAction && !postTickActions.contains(o)) {
			postTickActions.add((PostTickAction) o);
		}
		if (o instanceof FinishAction && !finishActions.contains(o)) {
			finishActions.add((FinishAction) o);
		}
	}

	 /**
	  * To register listener objects esp. in the interactive mode (by ABS in 2020)
	  * @see org.volante.abm.schedule.Schedule#registerListeners(org.volante.abm.schedule.ScheduleStatusListener)
	  */
	@Override
	public void registerListeners(ScheduleStatusListener o) {
		if (o instanceof ScheduleStatusListener && !listeners.contains(o)) {
			listeners.add( o);
		}
	}
	 
	
	/**
	 * @see org.volante.abm.schedule.Schedule#unregister(org.volante.abm.schedule.TickAction)
	 */
	@Override
	public boolean unregister(TickAction o) {
		if (o instanceof PreTickAction) {
			return preTickActions.remove(o);
		}
		if (o instanceof PostTickAction) {
			return postTickActions.remove(o);
		}
		logger.warn("The specified object is not a PreTickAction or PostTickAction!");
		return false;
	}

	private void output() {
		output.doOutput(regions);
	}

	/*
	 * Getters and setters
	 */

	/**
	 * @see org.volante.abm.schedule.Schedule#setStartTick(int)
	 */
	@Override
	public void setStartTick(int tick) {
		this.startTick = tick;
		this.tick = tick;
	}

	@Override
	public int getCurrentTick() {
		return tick;
	}

	@Override
	public void setRegions(RegionSet regions) {
		this.regions = regions;
	}

	void fireScheduleStatus(ScheduleStatusEvent e) {
		
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Fire\t\t (DefaultSchedule ID " + id + ")");
		}
		// LOGGING ->

		// copy to prevent concurrent modifications:
		List<ScheduleStatusListener> listenersCopy = new ArrayList<ScheduleStatusListener>(
				listeners);
		 
		for (ScheduleStatusListener l : listenersCopy) {
			l.scheduleStatus(e);
		}
	}
}
