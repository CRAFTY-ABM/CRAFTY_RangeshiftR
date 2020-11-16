/**
 * 
 */
package org.volante.abm.comi.decision.pa;


import java.util.Map;

import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.comi.decision.pa.ComiOfAdoptionPa.Properties;
import org.volante.abm.comi.fr.ComiVariantProductionFR;
import org.volante.abm.data.ModelData;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.schedule.RunInfo;

import de.cesr.lara.components.LaraBehaviouralOption;
import de.cesr.lara.components.LaraPerformableBo;
import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.agents.LaraAgent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.components.model.impl.LModel;
import de.cesr.lara.components.util.LaraPreferenceRegistry;
import de.cesr.lara.toolbox.config.xml.LBoFactory;


/**
 * @author Sascha Holzhauer
 *
 */
public class ComiSwitchBackPa extends CraftyPa<ComiSwitchBackPa> implements LaraPerformableBo {


	public static class ComiSwitchBackPaFactory extends LBoFactory {

		protected ModelData mdata;
		protected RunInfo rinfo;

		public LaraBehaviouralOption<?, ?> assembleBo(LaraAgent<?, ?> lbc, Object modelId) {
			return new ComiSwitchBackPa(this.key, (LaraBehaviouralComponent) lbc, this.preferenceWeights);
		}

	}

	protected LaraPreferenceRegistry prefReg = LModel.getModel(this.getAgent().getAgent().getRegion())
	        .getPrefRegistry();

	/**
	 * @param key
	 * @param agent
	 */
	public ComiSwitchBackPa(String key, LaraBehaviouralComponent agent,
 Map<LaraPreference, Double> preferenceUtilities) {
		super(key, agent, preferenceUtilities);
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
	 *      java.util.Map)
	 */
	@Override
	public CraftyPa<ComiSwitchBackPa> getModifiedBO(LaraBehaviouralComponent agent,
	        Map<LaraPreference, Double> preferenceUtilities) {
		return new ComiSwitchBackPa(this.getKey(), agent, preferenceUtilities);
	}

	/**
	 * Negates all utilities from ComiOfAdoptionPa.
	 * 
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getSituationalUtilities(de.cesr.lara.components.decision.LaraDecisionConfiguration)
	 */
	@Override
	public Map<LaraPreference, Double> getSituationalUtilities(LaraDecisionConfiguration dConfig) {
		Map<LaraPreference, Double> utilities = this.getModifiableUtilities();

		ComiOfAdoptionPa pa =
		        (ComiOfAdoptionPa) ((LaraBehaviouralComponent) this.getAgent().getAgent().getBC()).getLaraComp()
		                .getBOMemory().recall(ComiOfAdoptionPa.KEY);

		utilities.put(prefReg.get(ComiOfAdoptionPa.PREFNAME_ATTITUDE),
		        new Double(-1 * pa.getProperties().getProperty(Properties.ATTITUDE)));
		utilities.put(prefReg.get(ComiOfAdoptionPa.PREFNAME_SN),
		        new Double(-1 * pa.getProperties().getProperty(Properties.SUBJECTIVE_NORM)));
		utilities.put(prefReg.get(ComiOfAdoptionPa.PREFNAME_PBC), new Double(-1 * pa.getUpdatedPbc()));

		return utilities;
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
}
