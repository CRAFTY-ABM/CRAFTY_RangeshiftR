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
 * Created by Sascha Holzhauer on 24 May 2016
 */
package org.volante.abm.lara;


import java.lang.reflect.InvocationTargetException;

import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.ModelData;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;
import org.volante.abm.serialization.Initialisable;

import de.cesr.lara.components.container.exceptions.LContainerFullException;
import de.cesr.lara.components.container.exceptions.LInvalidTimestampException;
import de.cesr.lara.toolbox.config.xml.LBoFactory;
import de.cesr.lara.toolbox.config.xml.LXmlAgentConfigurator;


/**
 * Initialises PA factories when they implement the interface {@link GloballyInitialisable}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CobraLaraXmlAgentConfigurator extends LXmlAgentConfigurator<LaraBehaviouralComponent, CraftyPa<?>>
        implements GloballyInitialisable {

	protected ModelData mdata;
	protected RunInfo rinfo;

	/**
	 * @see de.cesr.lara.toolbox.config.LaraAgentConfigurator#configure(de.cesr.lara.components.agents.LaraAgent)
	 */
	@Override
	public void configure(LaraBehaviouralComponent agent) {
		for (LBoFactory factory : boFactories) {
			try {
				if (factory instanceof GloballyInitialisable) {
					((GloballyInitialisable) factory).initialise(mdata, rinfo);
				} else if (factory instanceof Initialisable) {
					((Initialisable) factory).initialise(mdata, rinfo, agent.getAgent().getRegion());
				}

				agent.getLaraComp().getBOMemory().memorize((CraftyPa<?>) factory.assembleBo(agent, modelId));
			} catch (LContainerFullException exception) {
				exception.printStackTrace();
			} catch (LInvalidTimestampException exception) {
				exception.printStackTrace();
			} catch (InstantiationException exception) {
				exception.printStackTrace();
			} catch (IllegalAccessException exception) {
				exception.printStackTrace();
			} catch (IllegalArgumentException exception) {
				exception.printStackTrace();
			} catch (InvocationTargetException exception) {
				exception.printStackTrace();
			} catch (NoSuchMethodException exception) {
				exception.printStackTrace();
			} catch (SecurityException exception) {
				exception.printStackTrace();
			} catch (ClassNotFoundException exception) {
				exception.printStackTrace();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}

		agent.getLaraComp().addPreferenceWeights(preferenceWeights);
		agent.getLaraComp().setPreprocessor(ppConfigurator.getPreprocessor());

		if (this.postprocessor != null) {
			agent.getLaraComp().setPostProcessor(postprocessor);
		}
	}

	/**
	 * @see org.volante.abm.serialization.GloballyInitialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		this.mdata = data;
		this.rinfo = info;
	}
}
