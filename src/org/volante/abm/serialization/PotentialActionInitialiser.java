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
package org.volante.abm.serialization;


import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.data.ModelData;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.schedule.RunInfo;

import de.cesr.lara.components.container.exceptions.LContainerFullException;
import de.cesr.lara.components.container.exceptions.LInvalidTimestampException;
import de.cesr.lara.toolbox.config.xml.LBoFactory;
import de.cesr.lara.toolbox.config.xml.LBoFactoryList;
import de.cesr.lara.toolbox.config.xml.LPersister;


/**
 * Tool to load and initialise {@link CraftyPa}s from XML files and memorise them at the given
 * {@link LaraBehaviouralComponent}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class PotentialActionInitialiser {

	static public void addPa(LaraBehaviouralComponent lbc, String pafilename, ModelData mdata, RunInfo rinfo)
	        throws Exception {

		Set<LBoFactory> boFactories = new HashSet<>();
		boFactories.addAll(LPersister.getPersister(lbc.getAgent().getRegion())
		        .readXML(LBoFactoryList.class, pafilename).getBoFactories());

		for (LBoFactory factory : boFactories) {
			try {
				if (factory instanceof GloballyInitialisable) {
					((GloballyInitialisable) factory).initialise(mdata, rinfo);
				}
				lbc.getLaraComp().getBOMemory()
				        .memorize((CraftyPa<?>) factory.assembleBo(lbc, lbc.getAgent().getRegion()));
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
			}
		}
	}

}
