/**
 * 
 */
package org.volante.abm.institutions.pa;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.decision.pa.CompetitivenessAdjustingPa;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.example.CurveCompetitivenessModel;
import org.volante.abm.institutions.RegionalProvisionInstitution;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.utils.ProductionWeightReporter;

import com.moseph.modelutils.curve.Curve;
import com.moseph.modelutils.curve.LinearFunction;
import com.moseph.modelutils.fastdata.DoubleMap;
import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

import de.cesr.lara.components.LaraBehaviouralOption;
import de.cesr.lara.components.LaraPerformableBo;
import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.agents.LaraAgent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.toolbox.config.xml.LBoFactory;


/**
 * @author Sascha Holzhauer
 * 
 */
public class RegionalCompFuncSubsidyPa extends CraftyPa<RegionalCompFuncSubsidyPa> implements LaraPerformableBo,
        CompetitivenessAdjustingPa {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(RegionalSubsidyPa.RegionalSubsidyPaFactory.class);

	public enum RegionalSubsidyPaPreferences {

		COSTEFFICIENCY("CostEfficiency"),

		REGIONAL_DEMAND_MATCHING("RegionalDemandMatching"),

		SOCIAL_APPROVAL("SocialApproval");

		RegionalSubsidyPaPreferences(String name) {
			this.name = name;
		}

		protected String name;

		public String getName() {
			return this.name;
		}
	}

	/**
	 * See {@link LBoFactory} why this is necessary.
	 * 
	 * @author Sascha Holzhauer
	 * 
	 */
	public static class RegionalCompFuncSubsidyPaFactory extends LBoFactory {

		/**
		 * The value set may contain usual FR labels or RegEx patterns eg. "NC_*"
		 */
		@ElementList(entry = "subsidisedFr", inline = true, required = false)
		protected List<String> serialSubsidisedFrs = new ArrayList<>();

		/**
		 * Weight for the proportion of cells that potentially can be taken over if these subsidies were in place.
		 * Should be in [0,1].
		 */
		@Element(required = false)
		protected double potentialTakeoversWeight = 0.0;

		/**
		 * Factor the service provision is multiplied with.
		 */
		@ElementMap(inline = true, required = false, entry = "serviceSubsidyFactor", attribute = true, key = "service")
		Map<String, Double> serialServiceSubsidies = new HashMap<String, Double>();

		@Element(required = false)
		protected double overallEffect = 1.0;

		/**
		 * @see de.cesr.lara.toolbox.config.xml.LBoFactory#assembleBo(de.cesr.lara.components.agents.LaraAgent,
		 *      java.lang.Object)
		 */
		public LaraBehaviouralOption<?, ?> assembleBo(LaraAgent<?, ?> lbc, Object modelId)
		        throws InstantiationException, IllegalAccessException, IllegalArgumentException,
		        InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {

			ModelData mdata = ((LaraBehaviouralComponent) lbc).getAgent().getRegion().getModelData();
			List<FunctionalRole> subsidisedFrs = new ArrayList<>();
			DoubleMap<Service> definedServiceSubsidies = mdata.serviceMap().duplicate();

			for (Entry<String, Double> e : serialServiceSubsidies.entrySet()) {
				if (mdata.services.contains(e.getKey())) {
					definedServiceSubsidies.put(mdata.services.forName(e.getKey()), e.getValue());
				} else {
					logger.warn("The specified service (" + e.getKey()
					        + ") for the subsidy is not defined in the model!");
				}
			}

			for (String frLabel : serialSubsidisedFrs) {
				if (!((LaraBehaviouralComponent) lbc).getAgent().getRegion().getFunctionalRoleMapByLabel()
				        .containsKey(frLabel)) {
					throw new IllegalStateException("The defined FR (" + frLabel + ") for subsidies is not present!");
				}
				subsidisedFrs.add(((LaraBehaviouralComponent) lbc).getAgent().getRegion().getFunctionalRoleMapByLabel()
				        .get(frLabel));
			}

			return new RegionalCompFuncSubsidyPa(this.key, (LaraBehaviouralComponent) lbc, this.preferenceWeights,
			        subsidisedFrs, definedServiceSubsidies, overallEffect, this.potentialTakeoversWeight);
		}
	}

	DoubleMap<Service> definedServiceSubsidies = null;
	protected List<FunctionalRole> subsidisedFrs;
	protected double overallEffect = 1.0;

	protected double potentialTakeoversWeight = 0.0;

	protected Map<Region, CompetitivenessModel> compModesl = new HashMap<>();

	/**
	 * @param key
	 * @param agent
	 * @param preferenceUtilities
	 * @param subsidisedFrs
	 * @param definedServiceSubsidies
	 * @param overallEffect
	 */
	public RegionalCompFuncSubsidyPa(String key, LaraBehaviouralComponent agent,
	        Map<LaraPreference, Double> preferenceUtilities, List<FunctionalRole> subsidisedFrs,
	        DoubleMap<Service> definedServiceSubsidies, double overallEffect, double potentialTakeoversWeight) {
		super(key, agent, preferenceUtilities);

		this.definedServiceSubsidies = definedServiceSubsidies;
		this.subsidisedFrs = subsidisedFrs;
		this.overallEffect = overallEffect;
		this.potentialTakeoversWeight = potentialTakeoversWeight;
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
	 *      java.util.Map)
	 */
	@Override
	public CraftyPa<RegionalCompFuncSubsidyPa> getModifiedBO(
	        LaraBehaviouralComponent agent, Map<LaraPreference, Double> preferenceUtilities) {
		return new RegionalCompFuncSubsidyPa(this.getKey(), agent, preferenceUtilities, this.subsidisedFrs,
		        this.definedServiceSubsidies, this.overallEffect, this.potentialTakeoversWeight);
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getSituationalUtilities(de.cesr.lara.components.decision.LaraDecisionConfiguration)
	 */
	@Override
	public Map<LaraPreference, Double> getSituationalUtilities(LaraDecisionConfiguration dConfig) {
		Map<LaraPreference, Double> utilities = super.getModifiableUtilities();

		// Leave initial value for GlobalSubsidyPaPreferences.COSTEFFICIENCY

		double sum = 0.0;

		DoubleMap<Service> demand = this.getAgent().getAgent().getRegion().getDemandModel().getDemand();
		DoubleMap<Service> supply = this.getAgent().getAgent().getRegion().getDemandModel().getSupply();

		int totalpcounter = 0;

		double maxgap = 0;
		for (Service service : this.getAgent().getAgent().getRegion().getModelData().services) {
			maxgap =
			        Math.max(maxgap, demand.get(service) <= 0.0 ? 0.0 : (demand.get(service) - supply.get(service))
			                / demand.get(service));
		}
		for (Service service : this.getAgent().getAgent().getRegion().getModelData().services) {
			// percental supply gap (relative to demand)
			if (this.definedServiceSubsidies.get(service) > 0) {
				double gap =
				        demand.get(service) <= 0.0 ? 0.0 : (demand.get(service) - supply.get(service))
				                / (demand.get(service) * maxgap);

				// sum of production weights of subsidised FRs
				double production = 0.0;
				double allproduction = 0;
				double thisproduction = 0;
				for (FunctionalRole fr : this.getAgent().getAgent().getRegion().getFunctionalRoles()) {
					if (fr.getProduction() instanceof ProductionWeightReporter) {
						thisproduction =
						        ((ProductionWeightReporter) fr.getProduction()).getProductionWeights().getDouble(
						                service);
						if (this.subsidisedFrs.contains(fr)) {
							production += thisproduction;
						}
						allproduction += thisproduction;
					}
				}

				// get potential production
				int pcounter = 0;
				for (Cell c : this.getAgent().getAgent().getRegion().getAllCells()) {
					if (!this.subsidisedFrs.contains(c.getOwner().getFC().getFR())) {
						for (FunctionalRole fr : this.subsidisedFrs) {
							if (c.getOwner() == Agent.NOT_MANAGED
							        || c.getOwner() == null
							        || c.getOwner().getRegion().getCompetitiveness(c) < c.getOwner().getRegion()
							                .getCompetitiveness(fr, c)
							                // TODO consider all subsidised services here (instead of outer loop)!
							                + this.definedServiceSubsidies.get(service) * overallEffect) {
								pcounter++;
								break;
							}
						}
					}
				}

				totalpcounter += pcounter;
				sum +=
				        (double) pcounter / this.getAgent().getAgent().getRegion().getNumCells()
				                * this.potentialTakeoversWeight
				                + allproduction > 0 ? (1 - this.potentialTakeoversWeight) * gap
				                * this.definedServiceSubsidies.get(service) * overallEffect
				                * production / allproduction : 0;
			}
		}
		utilities.put(
		        this.getAgent().getLaraComp().getLaraModel().getPrefRegistry()
		                .get(RegionalSubsidyPaPreferences.REGIONAL_DEMAND_MATCHING.getName()),
		        this.subsidisedFrs.size() > 0 ? sum / this.subsidisedFrs.size() : 0);

		if (this.subsidisedFrs.size() > 0) {
			LaraPreference pref =
			        this.getAgent().getLaraComp().getLaraModel().getPrefRegistry()
			                .get(RegionalSubsidyPaPreferences.SOCIAL_APPROVAL.getName());

			utilities.put(
			        pref,
			        utilities.get(pref)
			                * Math.min(1.0, (double) totalpcounter
			                        / this.getAgent().getAgent().getRegion().getNumCells()));
		}
		return utilities;

	}

	/**
	 * @see de.cesr.lara.components.LaraPerformableBo#perform()
	 */
	@Override
	public void perform() {
		if (this.getAgent().getAgent() instanceof RegionalProvisionInstitution) {
			this.initialTick = Integer.MIN_VALUE;
			((RegionalProvisionInstitution) this.getAgent().getAgent()).addCompAdjustPa(this);
		} else {
			throw new IllegalStateException("This BO's associated agent should be a RegionalProvisionInstitution!");
		}
	}

	/**
	 * @see org.volante.abm.decision.pa.CompetitivenessAdjustingPa#adjustCompetitiveness(org.volante.abm.agent.fr.FunctionalRole,
	 *      org.volante.abm.data.Cell, com.moseph.modelutils.fastdata.UnmodifiableNumberMap, double)
	 */
	@Override
	public double adjustCompetitiveness(
	        FunctionalRole agent, Cell location, UnmodifiableNumberMap<Service> provision, double competitiveness) {

		if (!this.compModesl.containsKey(location.getRegion())) {
			CompetitivenessModel cmodel = location.getRegion().getCompetitionModelCopy();
			if (cmodel instanceof CurveCompetitivenessModel) {
				CurveCompetitivenessModel ccmodel = (CurveCompetitivenessModel) cmodel;
				for (Service s : definedServiceSubsidies.getKeys()) {
					Curve newCurve = ccmodel.getCurves().get(s);
					if (newCurve instanceof LinearFunction) {
						ccmodel.setCurve(s, ((LinearFunction) newCurve)
						        .getDeepCopy(definedServiceSubsidies.get(s) * this.overallEffect, 0.0));
					}
				}
			} else {
				throw new IllegalStateException("To apply RegionalCompFuncSubsidyPa, region's (" + location.getRegion()
				        + ") competitiveness" + " model must be of type CurveCompetitivenessModel!");
			}
			this.compModesl.put(location.getRegion(), cmodel);
		}
		return this.compModesl.get(location.getRegion()).getCompetitiveness(location.getRegion().getDemandModel(),
		        provision);
	}

	protected double getFrProductionFraction(FunctionalRole fr, Region region, Service service) {
		double[] production = new double[region.getFunctionalRoles().size()];
		for (Cell c : region.getCells()) {
			production[c.getOwnersFrSerialID()] += c.getSupply().getDouble(service);
		}
		double productionSum = 0.0;
		for (int i = 0; i < production.length; i++) {
			productionSum += production[i];
		}
		return production[fr.getSerialID()] / productionSum;
	}
}
