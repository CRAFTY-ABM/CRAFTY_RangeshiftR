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
 * Created by Sascha Holzhauer on 6 Mar 2015
 */
package org.volante.abm.lara;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.ElementMapUnion;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.FinishAction;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.BatchModeParseFilter;

import de.cesr.lara.components.agents.impl.LAbstractAgent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.eventbus.events.LAgentDecideEvent;
import de.cesr.lara.components.eventbus.events.LAgentExecutionEvent;
import de.cesr.lara.components.eventbus.events.LAgentPostExecutionEvent;
import de.cesr.lara.components.eventbus.events.LAgentPostprocessEvent;
import de.cesr.lara.components.eventbus.events.LAgentPreprocessEvent;
import de.cesr.lara.components.eventbus.events.LInternalModelInitializedEvent;
import de.cesr.lara.components.eventbus.events.LModelFinishEvent;
import de.cesr.lara.components.eventbus.events.LModelInstantiatedEvent;
import de.cesr.lara.components.eventbus.events.LModelStepEvent;
import de.cesr.lara.components.eventbus.events.LModelStepFinishedEvent;
import de.cesr.lara.components.eventbus.events.LaraEvent;
import de.cesr.lara.components.eventbus.impl.LDcSpecificEventbus;
import de.cesr.lara.components.eventbus.impl.LEventbus;
import de.cesr.lara.components.model.impl.LAbstractModel;
import de.cesr.lara.components.model.impl.LModel;
import de.cesr.lara.components.util.impl.LDecisionConfigRegistry;
import de.cesr.lara.components.util.impl.LPreferenceRegistry;
import de.cesr.lara.toolbox.config.xml.LPersister;
import de.cesr.lara.toolbox.config.xml.LXmlModelConfigurator;
import de.cesr.lara.toolbox.param.LXmlConfigPa;
import de.cesr.parma.core.PmParameterDefinition;
import de.cesr.parma.core.PmParameterManager;

/**
 * @author Sascha Holzhauer
 *
 */
public class RegionalLaraModel extends LAbstractModel {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(RegionalLaraModel.class);

	@ElementMapUnion({
			@ElementMap(inline = true, entry = "Integer", attribute = true, required = false, key = "param", valueType = Integer.class),
			@ElementMap(inline = true, entry = "Double", attribute = true, required = false, key = "param", valueType = Double.class),
			@ElementMap(inline = true, entry = "Float", attribute = true, required = false, key = "param", valueType = Float.class),
			@ElementMap(inline = true, entry = "Long", attribute = true, required = false, key = "param", valueType = Long.class),
			@ElementMap(inline = true, entry = "Character", attribute = true, required = false, key = "param", valueType = Character.class),
			@ElementMap(inline = true, entry = "Boolean", attribute = true, required = false, key = "param", valueType = Boolean.class),
			@ElementMap(inline = true, entry = "String", attribute = true, required = false, key = "param", valueType = String.class) })
	Map<String, Object> params = new HashMap<String, Object>();

	PmParameterManager pm;

	protected RunInfo rInfo = null;
	protected Region region = null;


	public RegionalLaraModel() {
	}

	/**
	 * CRAFTY specific initialisation
	 * 
	 * @param data
	 * @param info
	 * @param region
	 */
	public void initialise(ModelData data, RunInfo info, Region region) {

		this.region = region;

		// <- LOGGING
		logger.info(this + "> Initialise RegionalLaraModel...");
		// LOGGING ->

		this.rInfo = info;

		this.pm = PmParameterManager.getInstance(region);
		this.pm.setParam(LXmlConfigPa.XML_BASEPATH, info
				.getPersister().getBaseDir());

		for (Map.Entry<String, Object> param : params.entrySet()) {
			PmParameterDefinition p = PmParameterManager.parse(param.getKey());

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug(this + "> Read param: " + p + " / "
						+ param.getValue());
			}
			// LOGGING ->

			if (Class.class.isAssignableFrom(p.getType())
					&& param.getValue() instanceof String) {
				try {
					pm.setParam(p,
							Class.forName(((String) param.getValue()).trim()));
				} catch (ClassNotFoundException exception) {
					logger.error(this + "> Could not find class "
							+ param.getValue()
							+ " during ParMa parameter initialisation!");
					exception.printStackTrace();
				}
			} else {
				pm.setParam(p, param.getValue());
			}
		}

		this.basicInit(region);
		this.eventBus.subscribe(this, LInternalModelInitializedEvent.class);

		this.eventBus.publish(new LModelInstantiatedEvent());
	}

	/**
	 * Needs to instantiate a {@link LDcSpecificEventbus}!
	 * 
	 * @see de.cesr.lara.components.model.impl.LAbstractModel#basicInit(java.lang.Object)
	 */
	@Override
	protected void basicInit(Object id) {
		this.id = id;
		LAbstractAgent.resetCounter();
		LModel.resetModel(id);
		LModel.setNewModel(id, this);

		eventBus = LEventbus.getDcSpecificInstance(id);
		eventBus.subscribe(this, LModelInstantiatedEvent.class);
		eventBus.subscribe(this, LModelStepEvent.class);

		prefRegistry = new LPreferenceRegistry();
		dConfigRegistry = new LDecisionConfigRegistry();

		// create new LARA persister with CRAFTY CoBRA specific filter:
		BatchModeParseFilter filter = new BatchModeParseFilter();
		filter.setRunInfo(this.rInfo);
		LPersister.getPersister(filter, id);
	}

	/**
	 * @see de.cesr.lara.components.model.impl.LAbstractModel#init()
	 */
	public void init() {
		this.rInfo.getSchedule().register(new PreTickAction() {

			@Override
			public void preTick() {
				RegionalLaraModel.this.getEventbus()
						.publish(new LModelStepEvent());
			}
		});

		this.rInfo.getSchedule().register(new FinishAction() {

			@Override
			public void afterLastTick() {
				RegionalLaraModel.this.getEventbus()
						.publish(new LModelFinishEvent());
			}
		});

		try {
			LXmlModelConfigurator.configure(region, pm);
		} catch (Exception exception) {
			logger.error("Error during configuratin of LaraModel.");
			exception.printStackTrace();
		}
	}

	/**
	 * @see de.cesr.lara.components.eventbus.LaraEventSubscriber#onEvent(de.cesr.lara.components.eventbus.events.LaraEvent)
	 */
	@Override
	public <T extends LaraEvent> void onEvent(T event) {
		if (event instanceof LInternalModelInitializedEvent) {
			// <- LOGGING
			logger.info(this + "> Lara Model initialised.");
			// LOGGING ->
		}
		if (event instanceof LModelStepEvent) {
			for (LaraDecisionConfiguration dConfig : this
					.getDecisionConfigRegistry().getAll()) {

				// <- LOGGING
				logger.info(this + "> Publish decision " + dConfig + "...");
				// LOGGING ->

				// preprocess
				eventBus.publish(new LAgentPreprocessEvent(dConfig));
				// decide
				eventBus.publish(new LAgentDecideEvent(dConfig));
				// post process
				eventBus.publish(new LAgentPostprocessEvent(dConfig));
				// execute
				eventBus.publish(new LAgentExecutionEvent(dConfig));

				// tidy up
				eventBus.publish(new LAgentPostExecutionEvent(dConfig));
			}
			eventBus.publish(new LModelStepFinishedEvent());
		}
	}

	protected LEventbus getEventbus() {
		return this.eventBus;
	}

	public String toString() {
		return "RegionalLaraModel (" + this.region + ")";
	}
}
