/**
 * 
 */
package org.volante.abm.decision.trigger;


import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Service;
import org.volante.abm.example.GlobalBtRepository;
import org.volante.abm.output.GenericTableOutputter;
import org.volante.abm.param.BasicPa;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.schedule.WorldSyncSchedule;

import com.moseph.modelutils.distribution.Distribution;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.model.impl.LModel;
import de.cesr.parma.core.PmParameterManager;


/**
 * Considers random stream {@link RandomPa#RANDOM_SEED_RUN_INSTITUTIONS} when
 * <code>supplyDemandDiffFactorDistribution</code> is defined.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class UnmetDemandDT extends AbstractDelayedStartDecisionTrigger {

	public enum TableOutputterColumns {
		VALUE_REAL, VALUE_PERCEIVED, INSTITUTION;
	}

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(UnmetDemandDT.class);

	@ElementList(required = true, entry = "consideredService", inline = true)
	List<String> serialConsideredServices = new ArrayList<>();

	@Element(required = false)
	protected int triggerDelay = 0;

	protected List<Service> consideredServices = new ArrayList<>();

	/**
	 * A negative value will multiply demand supply gap and this thresholdFraction to allow triggering in case of
	 * oversupply.
	 */
	@Element(required = false)
	protected double thresholdFraction = 0.2;

	@Element(required = false)
	Distribution supplyDemandDiffFactorDistribution = null;

	@Element(required = false)
	protected String genericOutputterId = "UnmetDemandPerception";

	protected NumberFormat floatFormat;

	public void initialise(ModelData mData, RunInfo info) throws Exception {
		super.initialise(mData, info);

		for (String serialService : serialConsideredServices) {
			if (mData.services.forName(serialService) != null) {
				consideredServices.add(mData.services.forName(serialService));
			} else {
				logger.warn("The specified service (" + serialService
						+ ") for the subsidy is not defined in the model!");
			}
		}

		if (this.supplyDemandDiffFactorDistribution != null) {
			this.supplyDemandDiffFactorDistribution.init(GlobalBtRepository.getInstance().getPseudoRegion().getRandom()
			        .getURService(), RandomPa.RANDOM_SEED_RUN_INSTITUTIONS.name());
		}
		
		this.floatFormat = (NumberFormat) PmParameterManager.getParameter(BasicPa.FLOAT_POINT_FORMAT);
	}


	/**
	 * @see org.volante.abm.decision.trigger.DecisionTrigger#check(org.volante.abm.agent.Agent)
	 */
	@Override
	protected boolean checkHook(final Agent agent) {
		for (Service service : this.consideredServices) {
			// get total demand across regions (account for distributed regions)
			double demand =
			        ((WorldSyncSchedule) this.rInfo.getSchedule()).getWorldSyncModel().getWorldDemand().get(service);
			// get total supply across regions (account for distributed regions)
			double difference =
			        demand
			                - ((WorldSyncSchedule) this.rInfo.getSchedule()).getWorldSyncModel().getWorldSupply()
			                        .get(service);
			double perceived = difference;

			if (this.supplyDemandDiffFactorDistribution != null) {
				double factor = this.supplyDemandDiffFactorDistribution.sample();
				perceived = difference * factor;
				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("Noise factor applied to difference between supply and demand: " + factor);
				}
				// LOGGING ->

				if (GenericTableOutputter.hasGenericTableOutputter(this.genericOutputterId)) {
					Map<String, Object> datamap = new HashMap<>();
					datamap.put(TableOutputterColumns.VALUE_REAL.toString(), new Double(difference));
					datamap.put(TableOutputterColumns.VALUE_PERCEIVED.toString(), new Double(perceived));
					datamap.put(TableOutputterColumns.INSTITUTION.toString(), agent);
					GenericTableOutputter.getGenericTabelOutputter(this.genericOutputterId).setData(datamap,
					        agent.getRegion());
				}
			}

			// <- LOGGING
			logger.info("> " + service + ": " + perceived / demand + " (" + this.thresholdFraction + ")");
			// LOGGING ->

			int oversupplySwitch = this.thresholdFraction < 0 ? -1 : 1;

			if (perceived * oversupplySwitch >= demand * this.thresholdFraction * oversupplySwitch) {

				final LaraDecisionConfiguration dConfig =
				        LModel.getModel(agent.getRegion()).getDecisionConfigRegistry().get(this.dcId);
				final double perceivedFinal = perceived;

				if (this.triggerDelay > 0) {
					PreTickAction action = new PreTickAction() {
						int intialTick = rInfo.getSchedule().getCurrentTick();

						@Override
						public void preTick() {
							if (rInfo.getSchedule().getCurrentTick() == triggerDelay + intialTick) {
								((LaraBehaviouralComponent) agent.getBC()).subscribeOnce(dConfig, new InformedTrigger(
								        UnmetDemandDT.this, "Gap:" + perceivedFinal));
								rInfo.getSchedule().unregister(this);
							}
						}
					};
					this.rInfo.getSchedule().register(action);

				} else {
					((LaraBehaviouralComponent) agent.getBC()).subscribeOnce(dConfig, new InformedTrigger(this, "Gap:"
					        + perceived));
				}
				return true;
			}
		}
		return false;
	}

	public String toString() {
		return "UnmetDemandTrigger (" + this.consideredServices.toString() + ")";
	}
}

