/**
 * 
 */
package org.volante.abm.institutions;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.bt.BehaviouralComponent;
import org.volante.abm.agent.bt.BehaviouralType;
import org.volante.abm.agent.bt.PseudoBT;
import org.volante.abm.agent.fr.FunctionalComponent;
import org.volante.abm.agent.fr.LazyFR;
import org.volante.abm.agent.property.DoublePropertyProvider;
import org.volante.abm.agent.property.DoublePropertyProviderComp;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.agent.property.PropertyRegistry;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.trigger.DecisionTrigger;
import org.volante.abm.schedule.RunInfo;


/**
 * 
 * Implements the {@link Agent} interface for {@link Institution}s to allow them decision making. Allows definition of
 * property-value (double) pairs.
 * 
 * These institutions have a {@link BehaviouralComponent} of a certain {@link BehaviouralType} assigned. The component
 * comprises the {@link DecisionTrigger}s and {@link CraftyPa}s the institutions is able to perform.
 * 
 * @author Sascha Holzhauer
 * 
 */
public abstract class AbstractCobraInstitution extends AbstractInstitution implements InstitutionAgent {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(AbstractCobraInstitution.class);

	@Attribute(required = false)
	protected String id = "NN";

	@Element(required = false)
	protected String btLabel = null;

	@Element(required = false)
	protected String frLabel = LandUseAgent.NOT_MANAGED_FR_ID;

	protected BehaviouralComponent behaviouralComp = null;

	protected FunctionalComponent functionalComp = LazyFR.getInstance();

	protected Cell homecell = null;

	@ElementMap(inline = true, entry = "property", attribute = true, required = false, key = "name", valueType = Double.class)
	protected Map<String, Object> params = new HashMap<String, Object>();

	protected DoublePropertyProvider propertyProvider;


	public AbstractCobraInstitution(@Attribute(name = "id") String id) {
		this.id = id;
		this.propertyProvider = new DoublePropertyProviderComp();
	}

	@Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);

		for (Entry<String, Object> property : params.entrySet()) {
			if (PropertyRegistry.get(property.getKey()) != null) {
				if (property.getValue() instanceof Number) {
					this.propertyProvider.setProperty(PropertyRegistry.get(property.getKey()),
							(Double) property.getValue());
				}
			}
		}

		if (this.getFC().equals(LazyFR.getInstance()) && !frLabel.equals(Agent.NOT_MANAGED_FR_ID)) {
			if (this.region.getFunctionalRoleMapByLabel().containsKey(frLabel)) {
				this.region.getFunctionalRoleMapByLabel().get(frLabel).assignNewFunctionalComp(this);
			} else {
				LazyFR.getInstance().assignNewFunctionalComp(this);
				logger.warn("Requested FunctionalRole (" + frLabel + ") not found. Using " + Agent.NOT_MANAGED_FR_ID
				        + "!");
			}
		}

		if (this.getBC() == null) {
			if (this.region.getBehaviouralTypeMapByLabel().containsKey(btLabel)) {
				this.region.getBehaviouralTypeMapByLabel().get(btLabel).assignNewBehaviouralComp(this);
			} else {
				logger.warn("Couldn't find BehaviouralType by id: " + btLabel + ". Assignind PseudoBT.");
				new PseudoBT().assignNewBehaviouralComp(this);
			}
		}
	}

	/**
	 * @see org.volante.abm.agent.Agent#die()
	 */
	@Override
	public void die() {
		// nothing to do
	}

	/**
	 * @see org.volante.abm.agent.Agent#tickStartUpdate()
	 */
	@Override
	public void tickStartUpdate() {
		// <- LOGGING
		logger.info(this.id + "> Tick start update");
		// LOGGING ->
		this.behaviouralComp.triggerDecisions(this);
	}

	/**
	 * @see org.volante.abm.agent.Agent#tickEndUpdate()
	 */
	@Override
	public void tickEndUpdate() {
		// <- LOGGING
		logger.info(this.id + "> Tick end update");
		// LOGGING ->
	}

	/**
	 * @see de.cesr.more.basic.agent.MoreObservingNetworkAgent#receiveNotification(de.cesr.more.basic.agent.MoreObservingNetworkAgent.NetworkObservation,
	 *      java.lang.Object)
	 */
	@Override
	public void receiveNotification(de.cesr.more.basic.agent.MoreObservingNetworkAgent.NetworkObservation observation,
			Agent object) {
		// nothing to do
	}

	/************************************************
	 * GETTER & SETTER
	 */

	/**
	 * @see org.volante.abm.agent.Agent#getID()
	 */
	@Override
	public String getID() {
		return this.id;
	}

	/**
	 * @see org.volante.abm.agent.Agent#infoString()
	 */
	@Override
	public String infoString() {
		return this.id;
	}

	/**
	 * @see org.volante.abm.agent.Agent#setRegion(org.volante.abm.data.Region)
	 */
	@Override
	public void setRegion(Region r) {
		this.region = r;
	}

	/**
	 * @see org.volante.abm.agent.Agent#getRegion()
	 */
	@Override
	public Region getRegion() {
		return this.region;
	}

	/**
	 * @return BehaviouralComponent
	 * @see org.volante.abm.agent.Agent#getBC()
	 */
	public BehaviouralComponent getBC() {
		return this.behaviouralComp;
	}

	/**
	 * @see org.volante.abm.agent.Agent#setBC(org.volante.abm.agent.bt.BehaviouralComponent)
	 */
	public void setBC(BehaviouralComponent bt) {
		this.behaviouralComp = bt;
	}

	/**
	 * @see org.volante.abm.agent.Agent#getFC()
	 */
	@Override
	public FunctionalComponent getFC() {
		return this.functionalComp;
	}

	/**
	 * @see org.volante.abm.agent.Agent#setFC(org.volante.abm.agent.fr.FunctionalComponent)
	 */
	@Override
	public void setFC(FunctionalComponent fc) {
		this.functionalComp = fc;
	}

	public void setHomeCell(Cell homecell) {
		this.homecell = homecell;
	}

	/**
	 * Return simply the first cell of an iterator of cells.
	 * 
	 * @see org.volante.abm.agent.Agent#getHomeCell()
	 */
	public Cell getHomeCell() {
		return this.homecell;
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#isProvided(org.volante.abm.agent.property.PropertyId)
	 */
	public boolean isProvided(PropertyId property) {
		return this.propertyProvider.isProvided(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#getProperty(org.volante.abm.agent.property.PropertyId)
	 */
	public Double getProperty(PropertyId property) {
		return this.propertyProvider.getProperty(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#setProperty(org.volante.abm.agent.property.PropertyId,
	 *      double)
	 */
	public void setProperty(PropertyId propertyId, Double value) {
		this.propertyProvider.setProperty(propertyId, value);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#getProperty(org.volante.abm.agent.property.PropertyId)
	 */
	public Object getObjectProperty(PropertyId property) {
		return this.propertyProvider.getObjectProperty(property);
	}

	/**
	 * @see org.volante.abm.agent.property.DoublePropertyProvider#setProperty(org.volante.abm.agent.property.PropertyId,
	 *      double)
	 */
	public void setObjectProperty(PropertyId propertyId, Object value) {
		this.propertyProvider.setObjectProperty(propertyId, value);
	}
}
