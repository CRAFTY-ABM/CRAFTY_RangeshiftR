/**
 * 
 */
package org.volante.abm.comi.decision.pa;


import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.property.DoublePropertyProviderComp;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.comi.fr.ComiVariantProductionFR;
import org.volante.abm.comi.param.ComiParameters;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.example.AgentPropertyIds;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.distribution.Distribution;

import de.cesr.lara.components.LaraBehaviouralOption;
import de.cesr.lara.components.LaraPerformableBo;
import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.agents.LaraAgent;
import de.cesr.lara.components.agents.LaraAgentComponent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.model.impl.LModel;
import de.cesr.lara.components.util.LaraPreferenceRegistry;
import de.cesr.lara.toolbox.config.xml.LBoFactory;
import de.cesr.parma.core.PmParameterManager;


/**
 * Not immutable (which is not a problem as long as it is not passed to other agents).
 * 
 * TODO extract AbstractTpbPa
 * 
 * @author Sascha Holzhauer
 * 
 */
public class ComiOfAdoptionPa extends CraftyPa<ComiOfAdoptionPa> implements LaraPerformableBo {

	public static final String KEY = "ComiOfAdoptionPa";

	public static final String PREFNAME_ATTITUDE = "WAttitude";
	public static final String PREFNAME_SN = "WSubjectiveNorm";
	public static final String PREFNAME_PBC = "WPBC";

	public static enum Properties implements PropertyId {
		INTENTION,

		SUBJECTIVE_NORM, BEHAVIOURAL_CONTROL, ATTITUDE,

		UNCERTAINTY_SN, UNCERTAINY_BC, UNCERTAINTY_A;
	}

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(ComiOfAdoptionPa.class);

	public static class ComiOfAdoptionPaFactory extends LBoFactory implements Initialisable {

		protected ModelData mdata;
		protected RunInfo rinfo;

		protected DoublePropertyProviderComp properties = new DoublePropertyProviderComp();

		@Element(required = false)
		protected Distribution attitude = null;

		@Element(required = true)
		protected Distribution uncertainty_a = null;

		@Element(required = true)
		protected Distribution subjectiveNorm = null;

		@Element(required = true)
		protected Distribution uncertainty_sn = null;

		@Element(required = true)
		protected Distribution behaviouralControl = null;

		/**
		 * @see org.volante.abm.serialization.GloballyInitialisable#initialise(org.volante.abm.data.ModelData,
		 *      org.volante.abm.schedule.RunInfo)
		 */
		@Override
		public void initialise(ModelData data, RunInfo info, Region region) throws Exception {
			this.mdata = data;
			this.rinfo = info;

			this.attitude.init(region.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ADOPTION.name());
			this.uncertainty_a.init(region.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ADOPTION.name());
			this.subjectiveNorm.init(region.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ADOPTION.name());
			this.uncertainty_sn.init(region.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ADOPTION.name());
			this.behaviouralControl.init(region.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN_ADOPTION.name());

			this.properties.setProperty(Properties.ATTITUDE, this.attitude.sample());
			this.properties.setProperty(Properties.UNCERTAINTY_A, this.uncertainty_a.sample());

			this.properties.setProperty(Properties.SUBJECTIVE_NORM, this.subjectiveNorm.sample());
			this.properties.setProperty(Properties.UNCERTAINTY_SN, this.uncertainty_sn.sample());

			this.properties.setProperty(Properties.BEHAVIOURAL_CONTROL, this.behaviouralControl.sample());
		}

		public LaraBehaviouralOption<?, ?> assembleBo(LaraAgent<?, ?> lbc, Object modelId) {
			return new ComiOfAdoptionPa(this.key, (LaraBehaviouralComponent) lbc, this.preferenceWeights,
			        this.properties);
		}
	}

	protected DoublePropertyProviderComp properties;

	protected LaraPreferenceRegistry prefReg = LModel.getModel(this.getAgent().getAgent().getRegion())
	        .getPrefRegistry();

	/**
	 * @param key
	 * @param agent
	 * @param preferenceUtilities
	 * @param properties
	 */
	public ComiOfAdoptionPa(String key, LaraBehaviouralComponent agent,
	        Map<LaraPreference, Double> preferenceUtilities, DoublePropertyProviderComp properties) {
		super(key, agent, preferenceUtilities);
		this.properties = properties;
	}

	/**
	 * NOTE: Properties are passed instead of deep copied!
	 * 
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
	 *      java.util.Map)
	 */
	@Override
	public CraftyPa<ComiOfAdoptionPa> getModifiedBO(LaraBehaviouralComponent agent,
	        Map<LaraPreference, Double> preferenceUtilities) {
		return new ComiOfAdoptionPa(this.getKey(), agent, preferenceUtilities, this.properties);
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getSituationalUtilities(de.cesr.lara.components.decision.LaraDecisionConfiguration)
	 */
	@Override
	public Map<LaraPreference, Double> getSituationalUtilities(LaraDecisionConfiguration dConfig) {
		Map<LaraPreference, Double> utilities = this.getModifiableUtilities();

		utilities.put(prefReg.get(PREFNAME_ATTITUDE), new Double(properties.getProperty(Properties.ATTITUDE)));
		utilities.put(prefReg.get(PREFNAME_SN), new Double(properties.getProperty(Properties.SUBJECTIVE_NORM)));

		utilities.put(prefReg.get(PREFNAME_PBC), getUpdatedPbc());

		return utilities;
	}

	/**
	 * @return updated perceived behavioural control
	 */
    public double getUpdatedPbc() {
	    // update PBC (OF competition relative to conventional):
		double compTerm = 0.0;
		FunctionalRole fr = this.getAgent().getAgent().getFC().getFR();
		double competitiveness =
		        this.getAgent().getAgent().getRegion().getCompetitiveness(fr, this.getAgent().getAgent().getHomeCell());

		if (fr instanceof ComiVariantProductionFR) {
			compTerm =
			        competitiveness == 0 ? Double.POSITIVE_INFINITY : ((this.getAgent().getAgent()
			                .getProperty(AgentPropertyIds.COMPETITIVENESS) / competitiveness) - 1.0);
		}

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Competitiveness term for agent " + this.getAgent().getAgent() + ": " + compTerm);
		}
		// LOGGING ->

		return Math.max(1.0, properties.getProperty(Properties.BEHAVIOURAL_CONTROL) + compTerm);
    }

	/**
	 * @param neighbour
	 * 
	 */
	public void socialInfluence(SocialAgent neighbour) {

		LaraAgentComponent<LaraBehaviouralComponent, CraftyPa<?>> lcn =
		        ((LaraBehaviouralComponent) neighbour.getBC()).getLaraComp();
		DoublePropertyProviderComp neighbourProps =
		        ((ComiOfAdoptionPa) lcn.getBOMemory().recall(ComiOfAdoptionPa.KEY)).getProperties();

		double uIntentionNeighbour =
		        (neighbourProps.getProperty(Properties.UNCERTAINTY_A)
		                * lcn.getPreferenceWeight(prefReg.get(PREFNAME_ATTITUDE)) + neighbourProps
		                .getProperty(Properties.UNCERTAINTY_SN) * lcn.getPreferenceWeight(prefReg.get(PREFNAME_SN)))
		                / (lcn.getPreferenceWeight(prefReg.get(PREFNAME_ATTITUDE)) + lcn.getPreferenceWeight(prefReg
		                        .get(PREFNAME_SN)));

		double overlap =
		        Math.min(
		                neighbourProps.getProperty(Properties.INTENTION) + uIntentionNeighbour,
		                this.properties.getProperty(Properties.SUBJECTIVE_NORM)
		                        + this.properties.getProperty(Properties.UNCERTAINTY_SN))
		                - Math.max(
		                        neighbourProps.getProperty(Properties.INTENTION) - uIntentionNeighbour,
		                        this.properties.getProperty(Properties.SUBJECTIVE_NORM)
		                                - this.properties.getProperty(Properties.UNCERTAINTY_SN));

		double ra = (overlap / (2 * this.properties.getProperty(Properties.UNCERTAINTY_SN)) - 1);

		this.getProperties().setProperty(
		        Properties.SUBJECTIVE_NORM,
		        this.getProperties().getProperty(Properties.SUBJECTIVE_NORM)
		                + ((Double) PmParameterManager.getInstance(this.getAgent().getAgent().getRegion()).getParam(
		                        ComiParameters.MU))
		                * ra
		                * (neighbourProps.getProperty(Properties.INTENTION) - this.getProperties().getProperty(
		                        Properties.SUBJECTIVE_NORM)));

		this.getProperties().setProperty(
		        Properties.UNCERTAINTY_SN,
		        this.getProperties().getProperty(Properties.UNCERTAINTY_SN)
		                + ((Double) PmParameterManager.getInstance(this.getAgent().getAgent().getRegion()).getParam(
		                        ComiParameters.MU)) * ra
		                * (uIntentionNeighbour - this.getProperties().getProperty(Properties.UNCERTAINTY_SN)));

		calculateIntention();
	}

	/**
	 * 
	 */
    public void calculateIntention() {
	    this.getProperties().setProperty(
		        Properties.INTENTION,
		        this.getProperties().getProperty(Properties.ATTITUDE)
		                * ((LaraBehaviouralComponent) this.getAgent().getAgent().getBC()).getLaraComp()
		                        .getPreferenceWeight(prefReg.get(PREFNAME_ATTITUDE))
		                + this.getProperties().getProperty(Properties.SUBJECTIVE_NORM)
		                * ((LaraBehaviouralComponent) this.getAgent().getAgent().getBC()).getLaraComp()
		                        .getPreferenceWeight(prefReg.get(PREFNAME_SN))
		                + this.getProperties().getProperty(Properties.BEHAVIOURAL_CONTROL)
		                * ((LaraBehaviouralComponent) this.getAgent().getAgent().getBC()).getLaraComp()
		                        .getPreferenceWeight(prefReg.get(PREFNAME_PBC)));
    }

	/**
	 * Switches the FR from organic farming to conventional are the other way around depending on current FR.
	 * 
	 * @see de.cesr.lara.components.LaraPerformableBo#perform()
	 */
	public void perform() {
		this.getAgent()
		        .getAgent()
		        .setFC(this
		                .getAgent()
		                .getAgent()
		                .getRegion()
		                .getFunctionalRoleMapByLabel()
		                .get(((ComiVariantProductionFR) this.getAgent().getAgent().getFC().getFR())
		                        .getAlternativeFrId()).getNewFunctionalComp());
	}

	public DoublePropertyProviderComp getProperties() {
		return properties;
	}

}
