/**
 * 
 */
package org.volante.abm.decision.trigger;


import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.data.Service;
import org.volante.abm.schedule.PreTickAction;

import com.moseph.modelutils.fastdata.DoubleMap;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.model.impl.LModel;


/**
 * @author Sascha Holzhauer
 * 
 */
public class RegionalUnmetDemandDT extends UnmetDemandDT {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(RegionalUnmetDemandDT.class);

	public enum RegionalUnmetDemandProperties implements PropertyId {
		REGIONAL_DEMAND, REGIONAL_SUPPLY;
	}

	public boolean check(final Agent agent) {
		if (this.checkFormal(agent)) {
			// get (potentially biased) demand figures from institutional agent
			DoubleMap<Service> regionalDemand;
			if (agent.isProvided(RegionalUnmetDemandProperties.REGIONAL_DEMAND)) {
				// supply = (DoubleMap) agent.getProperty(RegionalUnmetDemandProperties.REGIONAL_SUPPLY);
				regionalDemand = (DoubleMap) agent.getObjectProperty(RegionalUnmetDemandProperties.REGIONAL_DEMAND);
			} else {
				regionalDemand = agent.getRegion().getDemandModel().getDemand();
			}

			// get (potentially biased) supply figures from institutional agent
			DoubleMap<Service> regionalSupply;
			if (agent.isProvided(RegionalUnmetDemandProperties.REGIONAL_SUPPLY)) {
				regionalSupply = (DoubleMap) agent.getObjectProperty(RegionalUnmetDemandProperties.REGIONAL_SUPPLY);
			} else {
				regionalSupply = agent.getRegion().getDemandModel().getSupply();
			}

			for (Service service : consideredServices) {
				// compare with regional demand
				double factor = 1.0;
				if (this.supplyDemandDiffFactorDistribution != null) {
					factor = this.supplyDemandDiffFactorDistribution.sample();
					// <- LOGGING
					if (logger.isDebugEnabled()) {
						logger.debug("Noise factor applied to difference between supply and demand: " + factor);
					}
					// LOGGING ->
				}

				double perceived = (regionalDemand.get(service) - regionalSupply.get(service)) * factor;

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug(this.id + " (" + this.dcId + ") > " + service + ": "
					        + this.floatFormat.format(perceived / regionalDemand.get(service))
 + " (Threshold: "
					        + this.floatFormat.format(this.thresholdFraction) + ")");
				}
				// LOGGING ->

				int oversupplySwitch = this.thresholdFraction < 0 ? -1 : 1;

				if (perceived * oversupplySwitch >= regionalDemand.get(service) * thresholdFraction * oversupplySwitch) {

					final LaraDecisionConfiguration dConfig =
					        LModel.getModel(agent.getRegion()).getDecisionConfigRegistry().get(this.dcId);
					final double perceivedFinal = perceived;

					if (this.triggerDelay > 0) {
						PreTickAction action = new PreTickAction() {
							int intialTick = rInfo.getSchedule().getCurrentTick();

							@Override
							public void preTick() {
								if (rInfo.getSchedule().getCurrentTick() == triggerDelay + intialTick) {
									((LaraBehaviouralComponent) agent.getBC()).subscribeOnce(dConfig,
									        new InformedTrigger(RegionalUnmetDemandDT.this, "Gap:" + perceivedFinal));
									rInfo.getSchedule().unregister(this);
								}
							}
						};
						this.rInfo.getSchedule().register(action);

					} else {
						((LaraBehaviouralComponent) agent.getBC()).subscribeOnce(dConfig, new InformedTrigger(this,
						        "Gap:" + this.floatFormat.format(perceived)));
					}

					return true;
				}
			}
		}
		return false;
	}
}
