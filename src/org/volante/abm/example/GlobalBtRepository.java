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
 * Created by Sascha Holzhauer on 18 Apr 2016
 */
package org.volante.abm.example;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.ElementMapUnion;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.PseudoRegion;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.FinishAction;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.serialization.BTList;
import org.volante.abm.serialization.BatchModeParseFilter;
import org.volante.abm.serialization.GloballyInitialisable;

import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.eventbus.events.LAgentDecideEvent;
import de.cesr.lara.components.eventbus.events.LAgentExecutionEvent;
import de.cesr.lara.components.eventbus.events.LAgentPreprocessEvent;
import de.cesr.lara.components.eventbus.events.LInternalModelInitializedEvent;
import de.cesr.lara.components.eventbus.events.LModelFinishEvent;
import de.cesr.lara.components.eventbus.events.LModelInstantiatedEvent;
import de.cesr.lara.components.eventbus.events.LModelStepEvent;
import de.cesr.lara.components.eventbus.events.LModelStepFinishedEvent;
import de.cesr.lara.components.eventbus.events.LaraEvent;
import de.cesr.lara.components.eventbus.impl.LDcSpecificEventbus;
import de.cesr.lara.components.eventbus.impl.LEventbus;
import de.cesr.lara.components.model.LaraModel;
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
 * Implements {@link LaraModel} and serves as ID to register the model. Also holds a pseudo region needed to register
 * BTs.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class GlobalBtRepository extends LAbstractModel implements GloballyInitialisable {

	protected static GlobalBtRepository instance = null;

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(GlobalBtRepository.class);

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


	ModelData mData = null;
	RunInfo rInfo = null;

	PseudoRegion pseudoRegion = new PseudoRegion();
	
	@Element(required = false)
	BTList bTypes = new BTList();

	@ElementList(required = false, inline = true, entry = "btFile")
	List<String> btFileList = new ArrayList<>();

	Map<String, BehaviouralType> behaviouralTypesByLabel = new LinkedHashMap<>();
	Map<Integer, BehaviouralType> behaviouralTypesBySerialId = new LinkedHashMap<>();

	public static GlobalBtRepository getInstance() {
		if (instance == null) {
			String message = "A GlobalBtRepository has not been instatiated. Check Scenario.xml for a configuration!";
			logger.error(message);
			throw new IllegalStateException(message);
		}
		return instance;
	}


	/**
	 * @see org.volante.abm.serialization.GloballyInitialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		instance = this;

		this.mData = data;
		this.rInfo = info;

		this.pseudoRegion.setID("PseudoRegion_" + info.getCurrentRun() + "-" + info.getCurrentRandomSeed());
		this.pseudoRegion.initialise(mData, rInfo, null);

		// init Lara Model

		// <- LOGGING
		logger.info(this + "> Initialise RegionalLaraModel...");
		// LOGGING ->

		this.rInfo = info;

		this.pm = PmParameterManager.getNewInstance(this.pseudoRegion);
		this.pm.setParam(LXmlConfigPa.XML_BASEPATH, info.getPersister().getBaseDir());

		for (Map.Entry<String, Object> param : params.entrySet()) {
			PmParameterDefinition p = PmParameterManager.parse(param.getKey());

			// <- LOGGING
			if (logger.isDebugEnabled()) {
				logger.debug(this + "> Read param: " + p + " / " + param.getValue());
			}
			// LOGGING ->

			if (Class.class.isAssignableFrom(p.getType()) && param.getValue() instanceof String) {
				try {
					pm.setParam(p, Class.forName(((String) param.getValue()).trim()));
				} catch (ClassNotFoundException exception) {
					logger.error(this + "> Could not find class " + param.getValue()
							+ " during ParMa parameter initialisation!");
					exception.printStackTrace();
				}
			} else {
				pm.setParam(p, param.getValue());
			}
		}

		this.basicInit(this.pseudoRegion);
		this.eventBus.subscribe(this, LInternalModelInitializedEvent.class);
		this.eventBus.publish(new LModelInstantiatedEvent());

		this.loadBehaviouralTypes();
		this.addBehaviouralTypes();
	}

	protected void loadBehaviouralTypes() throws Exception {
		for (String btFile : btFileList) {
			// <- LOGGING
			logger.info("Behavioural Types file: " + btFile);
			// LOGGING ->

			// we need to apply the LARA specific persister here because a
			// region-specific persister needs to
			// refer to the region-specific LaraPreferecenRegistry!
			Collection<String> btfiles = new HashSet<>();

			// if (btFileList.contains(",")) {

			btfiles.addAll(ABMPersister.splitTags(btFile));
			for (String btFileInner : btfiles) {
				bTypes.bTypes
						.addAll(LPersister.getPersister(this.pseudoRegion).readXML(BTList.class, btFileInner).bTypes);
			}
		}
		for (BehaviouralType bt : bTypes.bTypes) {
			logger.info("Initialise behavioural type: " + bt.getLabel());
			bt.initialise(this.mData, rInfo, this.pseudoRegion);
		}
	}

	protected void addBehaviouralTypes() {
		for (BehaviouralType type : bTypes.bTypes) {

			if (behaviouralTypesByLabel.containsKey(type.getLabel())) {
				logger.warn("New Behavioural Type overwrites existing one with label " + type.getLabel());
			}
			behaviouralTypesByLabel.put(type.getLabel(), type);

			if (behaviouralTypesBySerialId.containsKey(type.getSerialID())) {
				logger.warn("New Behavioural Type overwrites existing one with serial ID " + type.getSerialID());
			}
			behaviouralTypesBySerialId.put(type.getSerialID(), type);
		}
		this.pseudoRegion.addBehaviouralTypes(this.behaviouralTypesByLabel.values());
	}

	/**
	 * Needs to instantiate a {@link LDcSpecificEventbus}!
	 * 
	 * @see de.cesr.lara.components.model.impl.LAbstractModel#basicInit(java.lang.Object)
	 */
	@Override
	protected void basicInit(Object id) {
		this.id = id;
		LModel.resetModel(id);
		LModel.setNewModel(id, this);

		logger.info("Set new model at LModel for id " + id);

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
				GlobalBtRepository.this.eventBus.publish(new LModelStepEvent());
			}
		});

		this.rInfo.getSchedule().register(new FinishAction() {

			@Override
			public void afterLastTick() {
				GlobalBtRepository.this.eventBus.publish(new LModelFinishEvent());
			}
		});

		try {
			LXmlModelConfigurator.configure(this.pseudoRegion, pm);
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
			for (LaraDecisionConfiguration dConfig : this.getDecisionConfigRegistry().getAll()) {

				// <- LOGGING
				logger.info(this + "> Publish decision " + dConfig + "...");
				// LOGGING ->

				// preprocess
				eventBus.publish(new LAgentPreprocessEvent(dConfig));
				// decide
				eventBus.publish(new LAgentDecideEvent(dConfig));
				// execute
				eventBus.publish(new LAgentExecutionEvent(dConfig));
			}
			eventBus.publish(new LModelStepFinishedEvent());
		}
	}

	public Region getPseudoRegion() {
		return pseudoRegion;
	}

	public String toString() {
		return "GlobalBtRepository";
	}
}
