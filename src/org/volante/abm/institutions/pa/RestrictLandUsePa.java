/**
 * 
 */
package org.volante.abm.institutions.pa;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.data.Cell;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.pa.PropertyProvidingPa;
import org.volante.abm.decision.pa.RestrictingLandUsePa;
import org.volante.abm.example.CellPropertyIds;
import org.volante.abm.example.measures.ConnectivityMeasure;
import org.volante.abm.institutions.global.GenericGlobalInstitution;
import org.volante.abm.output.tablecolumns.RestrictionNumber.RestrictionNumberProperties;
import org.volante.abm.param.RandomPa;

import de.cesr.lara.components.LaraBehaviouralOption;
import de.cesr.lara.components.LaraPerformableBo;
import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.agents.LaraAgent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.toolbox.config.xml.LBoFactory;


/**
 * 
 * @author Sascha Holzhauer
 * 
 */
public class RestrictLandUsePa extends CraftyPa<RestrictLandUsePa> implements RestrictingLandUsePa, LaraPerformableBo,
        PropertyProvidingPa {

	/**
	 * See {@link LBoFactory} why this is necessary.
	 * 
	 * @author Sascha Holzhauer
	 * 
	 */
	public static class RestrictLandUsePaFactory extends LBoFactory {

		/**
		 * The value set may contain usual FR labels or RegEx patterns eg. "NC_*"
		 */
		@ElementMap(entry = "relevantFrSet", key = "fr", attribute = true, inline = true, valueType = org.volante.abm.institutions.pa.SimpleXmlList.class, required = false)
		protected Map<String, List<String>> serialRelevantFrGroups = new LinkedHashMap<>();

		/**
		 * Restriction of an FR will take place with this probability (default: 1.0)
		 */
		@Element(required = false)
		protected double restrictionProbability = 1.0;

		@Element(required = false)
		protected double similarNeighboursRequired = 1;

		@Element(required = false)
		protected int minimumNeighbourhoodToCheck = 6;

		/**
		 * @see de.cesr.lara.toolbox.config.xml.LBoFactory#assembleBo(de.cesr.lara.components.agents.LaraAgent,
		 *      java.lang.Object)
		 */
		public LaraBehaviouralOption<?, ?> assembleBo(LaraAgent<?, ?> lbc, Object modelId)
		        throws InstantiationException, IllegalAccessException, IllegalArgumentException,
		        InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {

			return new RestrictLandUsePa(this.key, (LaraBehaviouralComponent) lbc, this.preferenceWeights,
			        serialRelevantFrGroups, restrictionProbability, similarNeighboursRequired,
			        minimumNeighbourhoodToCheck);
		}
	}

	public enum RestrictLandUsePaPreferences {

		COSTEFFICIENCY("Costefficiency"),

		DEMAND_MATCHING("Demandmatching"),

		LU_CONNECTIVITY("LUConnectivity"),

		SOCIAL_APPROVAL("SocialApproval");

		RestrictLandUsePaPreferences(String name) {
			this.name = name;
		}

		protected String name;

		public String getName() {
			return this.name;
		}
	}

	protected Map<FunctionalRole, Set<FunctionalRole>> relevantFrGroups;

	protected Map<PropertyId, Object> actionProperties = new HashMap<>();

	protected double restrictionProbability = 1.0;

	protected double similarNeighboursRequired = 1;

	protected int minimumNeighbourhoodToCheck = 6;

	/**
	 * @param key
	 * @param comp
	 * @param preferenceUtilities
	 * @param serialRelevantFrGroups
	 * @param restrictionProbability
	 * @param similarNeighboursRequired
	 * @param minimumNeighbourhoodToCheck
	 */
	public RestrictLandUsePa(String key, LaraBehaviouralComponent comp,
	        Map<LaraPreference, Double> preferenceUtilities, Map<String, List<String>> serialRelevantFrGroups,
	        double restrictionProbability, double similarNeighboursRequired, int minimumNeighbourhoodToCheck) {
		super(key, comp, preferenceUtilities);

		this.relevantFrGroups = new LinkedHashMap<>();
		this.restrictionProbability = restrictionProbability;
		this.similarNeighboursRequired = similarNeighboursRequired;
		this.minimumNeighbourhoodToCheck = minimumNeighbourhoodToCheck;

		for (Entry<String, List<String>> mapentry : serialRelevantFrGroups.entrySet()) {
			Set<FunctionalRole> frSet = new HashSet<>();
			for (String fr : mapentry.getValue()) {
				for (Entry<String, FunctionalRole> entry : comp.getAgent().getRegion().getFunctionalRoleMapByLabel()
				        .entrySet()) {
					if (entry.getKey().matches(fr)) {
						frSet.add(entry.getValue());
					}
				}
			}
			this.relevantFrGroups.put(comp.getAgent().getRegion().getFunctionalRoleMapByLabel().get(mapentry.getKey()),
			        frSet);
		}

	}

	/**
	 * Only indented for {@link #getModifiedBO(LaraBehaviouralComponent, Map)}.
	 * 
	 * @param comp
	 * @param preferenceUtilities
	 * @param relevantFrGroups
	 * @param key
	 */
	protected RestrictLandUsePa(LaraBehaviouralComponent comp, Map<LaraPreference, Double> preferenceUtilities,
	        Map<FunctionalRole, Set<FunctionalRole>> relevantFrGroups, String key) {
		super(key, comp, preferenceUtilities);
		this.relevantFrGroups = relevantFrGroups;
	}

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
	 *      java.util.Map)
	 */
	@Override
	public RestrictLandUsePa getModifiedBO(
	        LaraBehaviouralComponent comp, Map<LaraPreference, Double> preferenceUtilities) {
		return new RestrictLandUsePa(comp, preferenceUtilities, this.relevantFrGroups, this.getKey());
	}

	@Override
	public Map<LaraPreference, Double> getSituationalUtilities(LaraDecisionConfiguration dBuilder) {
		Map<LaraPreference, Double> utilities = super.getModifiableUtilities();

		// TODO
		utilities.put(
		        this.getAgent().getLaraComp().getLaraModel().getPrefRegistry()
		                .get(RestrictLandUsePaPreferences.COSTEFFICIENCY.getName()), 1.0);
		utilities.put(
		        this.getAgent().getLaraComp().getLaraModel().getPrefRegistry()
		                .get(RestrictLandUsePaPreferences.DEMAND_MATCHING.getName()), 1.0);
		utilities.put(
		        this.getAgent().getLaraComp().getLaraModel().getPrefRegistry()
		                .get(RestrictLandUsePaPreferences.SOCIAL_APPROVAL.getName()), 1.0);

		double sum = 0.0;

		// TODO this does not consider any true potential
		for (Entry<FunctionalRole, Set<FunctionalRole>> entry : this.relevantFrGroups.entrySet()) {
			sum += 1 - ConnectivityMeasure.getScore(this.getAgent().getAgent().getRegion(), entry.getValue());
		}
		utilities.put(
		        this.getAgent().getLaraComp().getLaraModel().getPrefRegistry()
		                .get(RestrictLandUsePaPreferences.LU_CONNECTIVITY.getName()),
		        this.relevantFrGroups.isEmpty() ? 1.0 : sum / this.relevantFrGroups.size());
		return utilities;
	}

	/**
	 * @see de.cesr.lara.components.LaraPerformableBo#perform()
	 */
	@Override
	public void perform() {
		((GenericGlobalInstitution) this.getAgent().getAgent()).addRestrictingLandUsePa(this);
	}

	/**
	 * @see org.volante.abm.decision.pa.PropertyProvidingPa#getProperties()
	 */
	@Override
	public Map<PropertyId, Object> getProperties() {
		return this.actionProperties;
	}

	/**
	 * @see org.volante.abm.decision.pa.RestrictingLandUsePa#isAllowed(org.volante.abm.agent.fr.FunctionalRole,
	 *      org.volante.abm.data.Cell)
	 */
	@Override
	public boolean isAllowed(FunctionalRole fr, Cell location) {
		if (relevantFrGroups != null && relevantFrGroups.containsKey(fr)) {
			Set<Cell> neighbours = location.getRegion().getAdjacentCells(location);
			if (neighbours.size() == this.minimumNeighbourhoodToCheck) {
				int similarNeighbours = 0;
				for (Cell n : neighbours) {
					if (this.relevantFrGroups.get(fr).contains(n.getOwner().getFC().getFR())) {
						similarNeighbours++;
					}
				}
				if (similarNeighbours >= this.similarNeighboursRequired) {
					return true;
				} else {
					if (restrictionProbability > location.getRegion().getRandom().getURService()
					        .nextDouble(RandomPa.RANDOM_SEED_RUN.name())) {
						location.setObjectProperty(CellPropertyIds.RESTRICTED, fr.getSerialID());

						Map<PropertyId, Object> properties = this.getProperties();
						int number =
						        properties.containsKey(RestrictionNumberProperties.RESTRICTION_NUMBER) ? ((Integer) properties
						                .get(RestrictionNumberProperties.RESTRICTION_NUMBER)).intValue() : 0;
						properties.put(RestrictionNumberProperties.RESTRICTION_NUMBER, ++number);

						return false;
					}
				}
			}
		}
		return false;
	}
}
