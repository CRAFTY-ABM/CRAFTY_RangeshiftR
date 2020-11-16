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
 * Created by Sascha Holzhauer on 19 Jul 2016
 */
package org.volante.abm.institutions.global;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.DecisionTriggerPrecheckingAgent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.data.Service;
import org.volante.abm.decision.pa.CompetitivenessAdjustingPa;
import org.volante.abm.decision.pa.RestrictingLandUsePa;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * @author Sascha Holzhauer
 *
 */
public class GenericGlobalInstitution extends AbstractCobraGlobalInstitution implements PreTickAction,
        DecisionTriggerPrecheckingAgent {


	@Element(required = false)
	protected int actionRuntime = 1;

	@Attribute(required = false)
	protected boolean allowMultipleActions = false;

	@Element(required = false)
	protected boolean triggerDecisionAfterRuntime = false;

	protected boolean reported = false;

	/**
	 * The institution only applies decision triggers when the {@link TickChecker} evaluates to true.
	 */
	@Element(required = false)
	protected TickChecker tickChecker = new TickChecker.DefaultTickChecker();

	protected int actionExpiry = Integer.MIN_VALUE;

	// / Additional
	protected Map<FunctionalRole, Set<FunctionalRole>> relevantFrGroups = null;

	protected Map<Integer, CompetitivenessAdjustingPa> compAdjustPas = new LinkedHashMap<>();
	protected Map<Integer, RestrictingLandUsePa> restrictingPas = new LinkedHashMap<>();

	/**
	 * @param id
	 */
	public GenericGlobalInstitution(@Attribute(name = "id") String id) {
		super(id);
	}

	public void initialise(ModelData mdata, RunInfo rinfo) {
		super.initialise(mdata, rinfo);

		rinfo.getSchedule().register(this);

		for (Region region : this.modelData.getRootRegionSet().getAllRegions()) {
			region.setHasCompetitivenessAdjustingInstitution();
		}
	}

	/**
	 * @see org.volante.abm.institutions.global.GlobalInstitution#getRegionSet()
	 */
	@Override
	public Regions getRegionSet() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.volante.abm.institutions.AbstractInstitution#isAllowed(org.volante.abm.agent.fr.FunctionalRole,
	 *      org.volante.abm.data.Cell)
	 */
	@Override
	public boolean isAllowed(FunctionalRole fr, Cell location) {
		boolean allow = true;
		for (RestrictingLandUsePa restrictingpa : this.restrictingPas.values()) {
			if (!restrictingpa.isAllowed(fr, location)) {
				allow = false;
				break;
			}
			if (!reported) {
				restrictingpa.reportRenewedActionPerformance();
				this.reported = true;
			}
		}
		return allow;
	}

	/**
	 * Multiplies given provision with service-specific subsidy factors, then multiplies this dot-product with
	 * <code>overallEffect</code>.
	 * 
	 * @see org.volante.abm.institutions.Institution#adjustCompetitiveness(org.volante.abm.agent.fr.FunctionalRole,
	 *      org.volante.abm.data.Cell, com.moseph.modelutils.fastdata.UnmodifiableNumberMap, double)
	 */
	@Override
	public double adjustCompetitiveness(FunctionalRole agent, Cell location, UnmodifiableNumberMap<Service> provision,
	        double competitiveness) {
		double comp = competitiveness;
		for (CompetitivenessAdjustingPa capa : this.compAdjustPas.values()) {
			comp = capa.adjustCompetitiveness(agent, location, provision, comp);
			if (!reported) {
				capa.reportRenewedActionPerformance();
				this.reported = true;
			}
		}
		return comp;
	}

	/**
	 * 
	 * @param restrictingPa
	 */
	public void addRestrictingLandUsePa(RestrictingLandUsePa restrictingPa) {

		this.restrictingPas.put(new Integer(this.rInfo.getSchedule().getCurrentTick()), restrictingPa);
		this.actionExpiry = this.rInfo.getSchedule().getCurrentTick() + this.actionRuntime - 1;
	}

	/**
	 * 
	 * @param compAdjustPa
	 */
	public void addCompAdjustPa(CompetitivenessAdjustingPa compAdjustPa) {
		this.compAdjustPas.put(new Integer(this.rInfo.getSchedule().getCurrentTick()), compAdjustPa);
		this.actionExpiry = this.rInfo.getSchedule().getCurrentTick() + this.actionRuntime - 1;
	}

	/**
	 * Clears the set of added Pa before a new one is added.
	 */
	public void clearRestrictingLandUsePas() {
		this.restrictingPas.clear();
	}

	/**
	 * Clears the set of added Pa before a new one is added.
	 */
	public void clearCompAdjustPas() {
		this.compAdjustPas.clear();
	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void preTick() {
		this.compAdjustPas.remove(new Integer(this.rInfo.getSchedule().getCurrentTick() - this.actionRuntime));
		this.restrictingPas.remove(new Integer(this.rInfo.getSchedule().getCurrentTick() - this.actionRuntime));
		this.reported = false;
	}

	/**
	 * @see org.volante.abm.agent.DecisionTriggerPrecheckingAgent#preCheckDecisionTriggers(java.util.Set)
	 */
	@Override
	public Set<DecisionTrigger> preCheckDecisionTriggers(Set<DecisionTrigger> decisionTriggers) {
		int tick = this.rInfo.getSchedule().getCurrentTick();
		if (!tickChecker.check(tick) || !(this.allowMultipleActions || this.actionExpiry < tick)) {
			decisionTriggers.clear();
		}
		return decisionTriggers;
	}
}
