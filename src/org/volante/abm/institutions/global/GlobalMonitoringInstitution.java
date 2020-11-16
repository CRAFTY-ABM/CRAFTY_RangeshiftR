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
 * Created by Sascha Holzhauer on 29 Jul 2015
 */
package org.volante.abm.institutions.global;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.DecisionTriggerPrecheckingAgent;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.decision.pa.CompetitivenessAdjustingPa;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.schedule.PrePreTickAction;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

import de.cesr.lara.components.preprocessor.impl.LDefaultDecisionModeSelector;


/**
 * 
 * Requires the definition of a {@link DecisionTrigger} in the {@link BehaviouralType} for this institution.
 * 
 * Applies adjusted competitiveness when service trigger checks are positive.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class GlobalMonitoringInstitution extends AbstractCobraGlobalInstitution implements PrePreTickAction {

//	public static class DecisionModeSelector extends LDefaultDecisionModeSelector {
//
//	}

	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(GlobalMonitoringInstitution.class);

	@Element(required = false)
	protected int actionRuntime = 1;

	@Attribute(required = false)
	protected boolean allowMultipleActions = false;

	@Element(required = false)
	protected boolean triggerDecisionAfterRuntime = false;

	protected int reportedTick = Integer.MIN_VALUE;
	
	@Element(required = false)
	protected TickChecker tickChecker = new TickChecker.DefaultTickChecker();

	protected int actionExpiry = Integer.MIN_VALUE;

	/**
	 * @param id
	 */
	public GlobalMonitoringInstitution(@Attribute(name = "id") String id) {
		super(id);
	}

//	Map<Integer, CompetitivenessAdjustingPa> compAdjustPas = new LinkedHashMap<>();


	public void initialise(ModelData mdata, RunInfo rinfo) {
		super.initialise(mdata, rinfo);

		rinfo.getSchedule().register(this);

		logger.info(this + "initiailsed");
		for (Region region : this.modelData.getRootRegionSet().getAllRegions()) {
			region.setHasCompetitivenessAdjustingInstitution();
		}
	}

//	/**
//	 * Multiplies given provision with service-specific subsidy factors, then multiplies this dot-product with
//	 * <code>overallEffect</code>.
//	 * 
//	 * @see org.volante.abm.institutions.Institution#adjustCompetitiveness(org.volante.abm.agent.fr.FunctionalRole,
//	 *      org.volante.abm.data.Cell, com.moseph.modelutils.fastdata.UnmodifiableNumberMap, double)
//	 */
//	@Override
//	public double adjustCompetitiveness(FunctionalRole agent, Cell location, UnmodifiableNumberMap<Service> provision,
//			double competitiveness) {
//		double comp = competitiveness;
//		for (CompetitivenessAdjustingPa capa : this.compAdjustPas.values()) {
//			comp = capa.adjustCompetitiveness(agent, location, provision, comp);
//			capa.reportRenewedActionPerformance();
//		}
//		return comp;
//	}

//
//	/**
//	 * Clears the set of added Pa before a new one is added.
//	 * 
//	 * @param compAdjustPa
//	 */
//	public void addCompAdjustPa(CompetitivenessAdjustingPa compAdjustPa) {
//		this.compAdjustPas.clear();
//		this.compAdjustPas.put(new Integer(this.rInfo.getSchedule().getCurrentTick()), compAdjustPa);
//		this.actionExpiry = this.rInfo.getSchedule().getCurrentTick() + this.actionRuntime - 1;
//	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void prePreTick() {
		logger.info(this + "prePreTick()"); //@TODO implement monitoring feature

//		this.compAdjustPas.remove(new Integer(this.rInfo.getSchedule().getCurrentTick() - this.actionRuntime));
	}

//	/**
//	 * @see org.volante.abm.agent.DecisionTriggerPrecheckingAgent#preCheckDecisionTriggers(java.util.Set)
//	 */
//	@Override
//    public Set<DecisionTrigger> preCheckDecisionTriggers(Set<DecisionTrigger> decisionTriggers) {
//		int tick = this.rInfo.getSchedule().getCurrentTick();
//		if (!tickChecker.check(tick) || !(this.allowMultipleActions || this.actionExpiry < tick)) {
//			decisionTriggers.clear();
//		}
//		return decisionTriggers;
//    }
}
