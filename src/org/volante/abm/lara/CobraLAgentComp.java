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
 * Created by Sascha Holzhauer on 25 May 2015
 */
package org.volante.abm.lara;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.ModelData;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.output.ActionReporter;

import de.cesr.lara.components.agents.impl.LDefaultAgentComp;
import de.cesr.lara.components.decision.LaraDecider;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.decision.LaraScoreReportingDecider;
import de.cesr.lara.components.environment.LaraEnvironment;
import de.cesr.lara.components.eventbus.events.LAgentDecideEvent;
import de.cesr.lara.components.eventbus.events.LAgentExecutionEvent;
import de.cesr.lara.components.eventbus.events.LAgentPostprocessEvent;
import de.cesr.lara.components.eventbus.events.LAgentPreprocessEvent;
import de.cesr.lara.components.eventbus.events.LaraEvent;
import de.cesr.lara.components.eventbus.impl.LDcSpecificEventbus;
import de.cesr.lara.components.eventbus.impl.LEventbus;
import de.cesr.lara.components.model.LaraModel;
import de.cesr.parma.core.PmParameterManager;

/**
 * Prevents the agent component from subscribing to LAgent* events (since these
 * subscriptions are performed on demand).
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CobraLAgentComp extends
 LDefaultAgentComp<LaraBehaviouralComponent, CraftyPa<?>> implements
		CobraLaraAgentComponent {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(CobraLAgentComp.class);

	Collection<ActionReporter> paReporters = new HashSet<>();

	ModelData mdata = null;

	/**
	 * Storage of {@link DecisionTrigger}s is required to report {@link CraftyPa}.
	 */
	protected Map<LaraDecisionConfiguration, DecisionTrigger> decisionTriggers = new HashMap<>();

	/**
	 * @param lbc
	 * @param env
	 */
	public CobraLAgentComp(LaraBehaviouralComponent lbc, LaraEnvironment env) {
		super(lbc, env);
		this.mdata = lbc.getAgent().getRegion().getModelData();
	}

	/**
	 * @param model
	 * @param lbc
	 * @param env
	 */
	public CobraLAgentComp(LaraModel model, LaraBehaviouralComponent lbc,
			LaraEnvironment env) {
		super(model, lbc, env);
		this.mdata = lbc.getAgent().getRegion().getModelData();
	}

	@Override
	public void setLaraModel(LaraModel lmodel) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.info("Set new id object (renew eventbus and subscirbe events subsequently)");
		}
		// LOGGING ->

		this.lmodel = lmodel;
		this.eventBus = this.lmodel.getLEventbus();

	}

	public void subscribeOnce(LaraDecisionConfiguration dc) {
		this.subscribeOnce(dc, null);
	}

	/**
	 * @param dc
	 */
	public void subscribeOnce(LaraDecisionConfiguration dc, DecisionTrigger trigger) {

		if (trigger != null) {
			decisionTriggers.put(dc, trigger);
		}

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(this.agent + "> Subscribe once for " + dc);
		}
		// LOGGING ->

		LDcSpecificEventbus eb = (LDcSpecificEventbus) this.eventBus;

		eb.subscribeOnce(this, LAgentPreprocessEvent.class, dc);
		eb.subscribeOnce(this, LAgentDecideEvent.class, dc);
		eb.subscribeOnce(this, LAgentPostprocessEvent.class, dc);
		eb.subscribeOnce(this, LAgentExecutionEvent.class, dc);
	}

	public void onInternalEvent(LaraEvent event) {
		// Create eventbus instance here to account for region-specific
		// parameter manager (which is not known to the preprocessor where the
		// eventbus is usually initiated):
		LEventbus.getNewInstance(this.agent, PmParameterManager
				.getInstance(this.agent.getAgent().getRegion()));
		super.onInternalEvent(event);
	}

	/**
	 * @see de.cesr.lara.components.agents.impl.LDefaultAgentComp#decide(de.cesr.lara.components.decision.LaraDecisionConfiguration)
	 */
	public void decide(LaraDecisionConfiguration decisionConfig) {
		// <- LOGGING
	    if (logger.isDebugEnabled()) {
	        logger.debug(this.decisionComponentsInfo());
	    }
	    // LOGGING ->

		super.decide(decisionConfig);
	}

	protected void perform(LaraEvent event) {
		super.perform(event);

		// inform PaReporter:
		LaraDecisionConfiguration dConfig = ((LAgentExecutionEvent) event).getDecisionConfiguration();
		
		
		LaraDecider<CraftyPa<?>> decider = this.getDecisionData(((LAgentExecutionEvent) event)
				.getDecisionConfiguration()).getDecider();
		if (decider.getNumSelectableBOs() > 0) {
			CraftyPa<?> pa = decider.getSelectedBo();

			// report selected PAs
			for (ActionReporter paReporter : paReporters) {
				paReporter.setActionInfos(
						this.agent.getAgent(),
						decisionTriggers.get(dConfig),
						dConfig,
				        this.getDecisionData(((LAgentExecutionEvent) event).getDecisionConfiguration()),
						pa,
						decider instanceof LaraScoreReportingDecider ? ((LaraScoreReportingDecider<CraftyPa<?>>) decider)
				                .getScore(pa) : Double.NaN, true);
			}
			
			// report not selected PAs
			for (CraftyPa<?> paction : decider.getSelectableBos()) {
				if (paction != pa) {
					for (ActionReporter paReporter : paReporters) {
						paReporter.setActionInfos(
								this.agent.getAgent(),
								decisionTriggers.get(dConfig),
								dConfig,
						        this.getDecisionData(((LAgentExecutionEvent) event).getDecisionConfiguration()),
								paction,
								decider instanceof LaraScoreReportingDecider ? ((LaraScoreReportingDecider<CraftyPa<?>>) decider)
										.getScore(paction) : Double.NaN, false);
					}
				}
			}
		}
	}

	public void reportActionPerformance(CraftyPa<?> pa) {
		for (ActionReporter paReporter : paReporters) {
			paReporter.setActionInfos(this.agent.getAgent(), pa);
		}
	}

	public void addPaReporter(ActionReporter paReporter) {
		this.paReporters.add(paReporter);
	}

	public boolean removePaReporter(ActionReporter paReporter) {
		return this.paReporters.remove(paReporter);
	}

	public ModelData getModelData() {
		return this.mdata;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " (" + this.agent + ")";
	}
}
