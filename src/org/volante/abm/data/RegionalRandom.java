/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 09.04.2014
 */
package org.volante.abm.data;


import org.volante.abm.param.RandomPa;

import cern.jet.random.engine.MersenneTwister;
import de.cesr.parma.core.PmParameterManager;
import de.cesr.uranus.core.URandomService;
import de.cesr.uranus.core.UranusRandomService;


/**
 * @author Sascha Holzhauer
 *
 */
public class RegionalRandom {

	UranusRandomService	rservice	= null;
	Region				r			= null;
	PmParameterManager	pm			= null;

	public RegionalRandom(Region r) {
		this.r = r;
		this.pm = PmParameterManager.getInstance(r);
	}

	public UranusRandomService getURService() {
		if (this.rservice == null) {
			this.rservice = new URandomService((Integer) this.pm.getParam(RandomPa.RANDOM_SEED));

		}
		return this.rservice;
	}

	public void init() {
		if (!getURService().isGeneratorRegistered(RandomPa.RANDOM_SEED_INIT.name())) {
			getURService().registerGenerator(RandomPa.RANDOM_SEED_INIT.name(),
					new MersenneTwister(((Integer) pm.getParam(RandomPa.RANDOM_SEED_INIT))));
		}

		if (!getURService().isGeneratorRegistered(RandomPa.RANDOM_SEED_INIT_AGENTS.name())) {
			getURService().registerGenerator(RandomPa.RANDOM_SEED_INIT_AGENTS.name(),
					new MersenneTwister(((Integer) pm.getParam(RandomPa.RANDOM_SEED_INIT_AGENTS))));
		}

		if (!getURService().isGeneratorRegistered(RandomPa.RANDOM_SEED_RUN.name())) {
			getURService().registerGenerator(RandomPa.RANDOM_SEED_RUN.name(),
					new MersenneTwister(((Integer) pm.getParam(RandomPa.RANDOM_SEED_RUN))));
		}

		if (!getURService().isGeneratorRegistered(RandomPa.RANDOM_SEED_RUN_ALLOCATION.name())) {
			getURService().registerGenerator(RandomPa.RANDOM_SEED_RUN_ALLOCATION.name(),
					new MersenneTwister(
							((Integer) pm.getParam(RandomPa.RANDOM_SEED_RUN_ALLOCATION))));
		}

		if (!getURService().isGeneratorRegistered(RandomPa.RANDOM_SEED_RUN_INSTITUTIONS.name())) {
			getURService().registerGenerator(RandomPa.RANDOM_SEED_RUN_INSTITUTIONS.name(),
			        new MersenneTwister(((Integer) pm.getParam(RandomPa.RANDOM_SEED_RUN_INSTITUTIONS))));
		}

		if (!getURService().isGeneratorRegistered(RandomPa.RANDOM_SEED_RUN_GIVINGUP.name())) {
			getURService().registerGenerator(RandomPa.RANDOM_SEED_RUN_GIVINGUP.name(),
					new MersenneTwister(((Integer) pm.getParam(RandomPa.RANDOM_SEED_RUN_GIVINGUP))));
		}

		if (!getURService().isGeneratorRegistered(
				RandomPa.RANDOM_SEED_RUN_ADOPTION.name())) {
			getURService().registerGenerator(
					RandomPa.RANDOM_SEED_RUN_ADOPTION.name(),
					new MersenneTwister(((Integer) pm
							.getParam(RandomPa.RANDOM_SEED_RUN_ADOPTION))));
		}
	}
}
