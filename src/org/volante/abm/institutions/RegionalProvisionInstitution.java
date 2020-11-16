/**
 * 
 */
package org.volante.abm.institutions;


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
import org.volante.abm.data.Service;
import org.volante.abm.decision.pa.CompetitivenessAdjustingPa;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.institutions.global.TickChecker;
import org.volante.abm.schedule.PrePreTickAction;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * When {@link CompetitivenessAdjustingPa}s are performed they may register at this institution and are consulted to
 * adjust competitiveness.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class RegionalProvisionInstitution extends AbstractCobraInstitution implements PrePreTickAction,
        DecisionTriggerPrecheckingAgent {

	Map<Integer, CompetitivenessAdjustingPa> compAdjustPas = new LinkedHashMap<>();

	@Element(required = false)
	protected int actionRuntime = 1;

	@Attribute(required = false)
	protected boolean allowMultipleActions = false;

	@Element(required = false)
	protected boolean triggerDecisionAfterRuntime = false;

	@Element(required = false)
	protected TickChecker tickChecker = new TickChecker.DefaultTickChecker();

	protected int actionExpiry = Integer.MIN_VALUE;

	/**
	 * @param id
	 */
	public RegionalProvisionInstitution(@Attribute(name = "id") String id) {
		super(id);
	}

	/**
	 * @see org.volante.abm.institutions.AbstractCobraInstitution#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);
		info.getSchedule().register(this);
		extent.setHasCompetitivenessAdjustingInstitution();
	}

	@Override
	public double adjustCompetitiveness(FunctionalRole agent, Cell location, UnmodifiableNumberMap<Service> provision,
	        double competitiveness) {
		double comp = competitiveness;
		for (CompetitivenessAdjustingPa capa : this.compAdjustPas.values()) {
			comp = capa.adjustCompetitiveness(agent, location, provision, comp);
			capa.reportRenewedActionPerformance();
		}
		return comp;
	}

	/**
	 * 
	 * @param compAdjustPa
	 */
	public void addCompAdjustPa(CompetitivenessAdjustingPa compAdjustPa) {
		this.compAdjustPas.put(this.rInfo.getSchedule().getCurrentTick(), compAdjustPa);
		this.actionExpiry = this.rInfo.getSchedule().getCurrentTick() + this.actionRuntime - 1;
	}

	/**
	 * Clears the set of added Pa before a new one is added.
	 */
	public void clearCompAdjustPa() {
		this.compAdjustPas.clear();
	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void prePreTick() {
		this.compAdjustPas.remove(new Integer(this.rInfo.getSchedule().getCurrentTick() - this.actionRuntime));
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

	public int getActionRuntime() {
		return actionRuntime;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "RegionalProvisionInstitution (" + this.region + ")";
	}
}
