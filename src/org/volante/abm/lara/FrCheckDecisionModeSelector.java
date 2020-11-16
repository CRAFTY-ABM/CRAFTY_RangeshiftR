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
 * Created by Sascha Holzhauer on 11 Mar 2015
 */
package org.volante.abm.lara;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.lara.decider.CapitalBasedImitatingFrDeciderFactory;

import de.cesr.lara.components.decision.LaraDeciderFactory;
import de.cesr.lara.components.decision.impl.LDeliberativeDeciderFactory;
import de.cesr.lara.components.decision.impl.LExplorationDeciderFactory;
import de.cesr.lara.components.decision.impl.LNoOptionDeciderFactory;
import de.cesr.lara.components.eventbus.events.LaraEvent;
import de.cesr.lara.components.eventbus.impl.LEventbus;
import de.cesr.lara.components.preprocessor.LaraDecisionModeSelector;
import de.cesr.lara.components.preprocessor.event.LPpBoCollectorEvent;
import de.cesr.lara.components.preprocessor.event.LPpBoPreselectorEvent;
import de.cesr.lara.components.preprocessor.event.LPpBoUtilityUpdaterEvent;
import de.cesr.lara.components.preprocessor.event.LPpModeSelectorEvent;
import de.cesr.lara.components.preprocessor.event.LPpPreferenceUpdaterEvent;
import de.cesr.lara.components.preprocessor.impl.LAbstractPpComp;


/**
 * 
 * Applies the consumat approach to select one of imitating/exploring/deliberative decision making
 * 
 * @author Sascha Holzhauer
 * 
 */
public class FrCheckDecisionModeSelector extends
		LAbstractPpComp<LaraBehaviouralComponent, CraftyPa<?>> implements
		LaraDecisionModeSelector<LaraBehaviouralComponent, CraftyPa<?>> {

	/**
	 * Logger
	 */
	static private Logger logger = Logger
			.getLogger(FrCheckDecisionModeSelector.class);

	@Attribute(name = "preselectPos", required = false)
	protected boolean preselectPos = false;

	@Attribute(name = "thresholdExperience", required = false)
	protected double thresholdExperience = 0.5;

	@Attribute(name = "thresholdCompetitiveness", required = false)
	protected double thresholdCompetitiveness = 0.5;


	@Element(name = "imitatingDeciderFactory", required = false)
	protected LaraDeciderFactory<LaraBehaviouralComponent, CraftyPa<?>> imitatingDeciderFactory = new CapitalBasedImitatingFrDeciderFactory();

	/**
	 * @see de.cesr.lara.components.eventbus.LaraInternalEventSubscriber#onInternalEvent(de.cesr.lara.components.eventbus.events.LaraEvent)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void onInternalEvent(LaraEvent e) {

		LPpModeSelectorEvent event = castEvent(LPpModeSelectorEvent.class, e);
		LaraBehaviouralComponent bComp = (LaraBehaviouralComponent) event
				.getAgent();
		
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(event.getAgent()
					+ "> competitiveness: "
					+ bComp.getAgent().getProperty(
							AgentPropertyIds.COMPETITIVENESS) + " (threshold: "
					+ this.thresholdCompetitiveness + ") / experience: "
					+ bComp.getAgent().getProperty(AgentPropertyIds.EXPERIENCE)
					+ "(threshold: " + this.thresholdExperience + ")");
		}
		// LOGGING ->

		if (bComp.getAgent().getProperty(AgentPropertyIds.COMPETITIVENESS) >= this.thresholdCompetitiveness) {
			if (bComp.getAgent().getProperty(AgentPropertyIds.EXPERIENCE) < this.thresholdExperience) {
				imitate(event);
			} else {
				// do not decide:
				((LaraBehaviouralComponent) event.getAgent())
						.getLaraComp()
						.getDecisionData(event.getdConfig())
						.setDeciderFactory(
								LNoOptionDeciderFactory.getFactory(event
										.getAgent().getClass()));
			}
		} else {
			if (bComp.getAgent().getProperty(AgentPropertyIds.EXPERIENCE) < this.thresholdExperience) {
				explore(event);
			} else {
				deliberate(event);
			}
		}
	}

	/**
	 * @param event
	 */
	protected void imitate(LPpModeSelectorEvent event) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(event.getAgent() + "> imitate");
		}
		// LOGGING ->

		((LaraBehaviouralComponent) event.getAgent()).getLaraComp()
				.getDecisionData(event.getdConfig())
				.setDeciderFactory(imitatingDeciderFactory);
	}

	/**
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	protected void explore(LPpModeSelectorEvent event) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(event.getAgent() + "> explore");
		}
		// LOGGING ->
		LaraBehaviouralComponent lbc = ((LaraBehaviouralComponent) event
				.getAgent());
		lbc.getLaraComp()
				.getDecisionData(event.getdConfig())
				.setDeciderFactory(
						LExplorationDeciderFactory.getFactory(event.getAgent()
								.getClass()));
		LEventbus eBus = LEventbus.getInstance(lbc);
		eBus.publish(new LPpBoCollectorEvent(lbc, event.getdConfig()));
		eBus.publish(new LPpBoPreselectorEvent(lbc, event.getdConfig()));
	}

	/**
	 * @param event
	 */
	@SuppressWarnings("unchecked")
	protected void deliberate(LPpModeSelectorEvent event) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug(event.getAgent() + "> decide deliberatively");
		}
		// LOGGING ->

		LaraBehaviouralComponent lbc = ((LaraBehaviouralComponent) event
				.getAgent());
		lbc.getLaraComp()
				.getDecisionData(event.getdConfig())
				.setDeciderFactory(
						LDeliberativeDeciderFactory.getFactory(event.getAgent()
								.getClass()));
		LEventbus eBus = LEventbus.getInstance(lbc);
		eBus.publish(new LPpBoCollectorEvent(lbc, event.getdConfig()));
		eBus.publish(new LPpBoPreselectorEvent(lbc, event.getdConfig()));
		eBus.publish(new LPpBoUtilityUpdaterEvent(lbc, event.getdConfig()));
		eBus.publish(new LPpPreferenceUpdaterEvent(lbc, event.getdConfig()));
	}
}
