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
 * Created by Sascha Holzhauer on 21 Jul 2016
 */
package org.volante.abm.institutions.pa;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Service;
import org.volante.abm.decision.pa.CraftyPa;

import com.moseph.modelutils.fastdata.DoubleMap;

import de.cesr.lara.components.LaraBehaviouralOption;
import de.cesr.lara.components.LaraPreference;
import de.cesr.lara.components.agents.LaraAgent;
import de.cesr.lara.components.decision.LaraDecisionConfiguration;
import de.cesr.lara.toolbox.config.xml.LBoFactory;

/**
 * @author Sascha Holzhauer
 *
 */
public class RegionalConnectSubsidyPa extends RegionalSubsidyPa {

	/**
     * Logger
     */
    static private Logger logger = Logger.getLogger(RegionalConnectSubsidyPa.class);
    
	/**
	 * See {@link LBoFactory} why this is necessary.
	 * 
	 * @author Sascha Holzhauer
	 * 
	 */
	public static class RegionalConnectSubsidyPaFactory extends RegionalSubsidyPaFactory {

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

			return new RegionalConnectSubsidyPa(this.key, (LaraBehaviouralComponent) lbc, this.preferenceWeights,
			        subsidisedFrs, definedServiceSubsidies, overallEffect, this.potentialTakeoversWeight);
		}
	}

	
	/**
	 * @param key
	 * @param agent
	 * @param preferenceUtilities
	 * @param subsidisedFrs
	 * @param definedServiceSubsidies
	 * @param overallEffect
	 * @param potentialTakeoversWeight
	 */
    public RegionalConnectSubsidyPa(String key, LaraBehaviouralComponent agent,
            Map<LaraPreference, Double> preferenceUtilities, List<FunctionalRole> subsidisedFrs,
            DoubleMap<Service> definedServiceSubsidies, double overallEffect, double potentialTakeoversWeight) {
	    super(key, agent, preferenceUtilities, subsidisedFrs, definedServiceSubsidies, overallEffect, potentialTakeoversWeight);
    }

	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getModifiedBO(de.cesr.lara.components.agents.LaraAgent,
	 *      java.util.Map)
	 */
	@Override
	public CraftyPa<RegionalSubsidyPa> getModifiedBO(LaraBehaviouralComponent agent,
	        Map<LaraPreference, Double> preferenceUtilities) {
		return new RegionalConnectSubsidyPa(this.getKey(), agent, preferenceUtilities, this.subsidisedFrs,
		        this.definedServiceSubsidies, this.overallEffect, this.potentialTakeoversWeight);
	}
	
	/**
	 * @see de.cesr.lara.components.LaraBehaviouralOption#getSituationalUtilities(de.cesr.lara.components.decision.LaraDecisionConfiguration)
	 */
	@Override
	public Map<LaraPreference, Double> getSituationalUtilities(LaraDecisionConfiguration dConfig) {
		Map<LaraPreference, Double> utilities = super.getModifiableUtilities();

		// Leave initial value for GlobalSubsidyPaPreferences.COSTEFFICIENCY

		int sum = 0;
		
		for (Cell c : this.getAgent().getAgent().getRegion().getAllCells()) {
			for (Cell n : this.getAgent().getAgent().getRegion().getAdjacentCells(c)) {
				if (c.getOwner() != Agent.NOT_MANAGED && c.getOwner() != null &&
						this.subsidisedFrs.contains(c.getOwner().getFC().getFR())) {
					for (FunctionalRole fr : this.subsidisedFrs) {
						if (c.getOwner().getRegion().getCompetitiveness(c) < c.getOwner().getRegion()
						        .getCompetitiveness(fr, c)
						        // TODO consider all subsidised services
						        + this.definedServiceSubsidies.get(this.definedServiceSubsidies.getMax())
						        * overallEffect) {
							sum++;
						}
					}
					break;
				}
			}
		}

		utilities.put(
		        this.getAgent().getLaraComp().getLaraModel().getPrefRegistry()
		                .get(RegionalSubsidyPaPreferences.REGIONAL_DEMAND_MATCHING.getName()),
		        this.subsidisedFrs.size() > 0 ? (double) sum / this.subsidisedFrs.size() : 0);

		return utilities;

	}
}
