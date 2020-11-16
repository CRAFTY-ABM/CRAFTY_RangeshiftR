/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2015 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 12 Aug 2015
 */
package org.volante.abm.example;


import org.volante.abm.data.Region;
import org.volante.abm.param.RandomPa;

import com.moseph.modelutils.Utilities;
import com.moseph.modelutils.curve.Constant;
import com.moseph.modelutils.curve.Curve;


/**
 * Uses the maximum probability of that provided by probabilitycurve and (yet desired / remaining
 * indices).
 * 
 * @author Sascha Holzhauer
 * 
 */
public class IterativeCellSampler {
	
	protected int		position	= -1;
	protected int		desiredtotal;
	protected int yetdesired;
	protected int size;
	
	protected Curve			probabilitycurve	= new Constant(0.5);

	protected Region	region;
	
	public IterativeCellSampler(int size, int desired, Region region, Curve probabilitycurve) {
		this(size, desired, region);
		this.probabilitycurve = probabilitycurve;
	}

	public IterativeCellSampler(int size, int desired, Region region) {
		desiredtotal = desired;
		if (desired > size) {
			throw new IllegalArgumentException(
					"Desired number of samples may not exceed size of population!");
		}
		this.size = size;
		this.yetdesired = desired;
		this.region = region;
	}

	public int sample() {
		double prob = 0.0;
		do {
			position++;
			if (yetdesired == 0) {
				throw new IllegalStateException(
						"Number of requested samples seems to exceed number of desired samples");
			}

			prob = Math.max((double) yetdesired / (size - position),
					probabilitycurve.sample(position));
		} while (prob < Utilities.nextDouble(this.region.getRandom().getURService(),
				RandomPa.RANDOM_SEED_RUN_ALLOCATION.name()));
		this.yetdesired--;
		return position;
	}

	/**
	 * @return true if more samples can be requested
	 */
	public boolean hasMoreToSample() {
		return !(yetdesired == 0);
	}

	/**
	 * Get the number of samples already requested from this sampler
	 * 
	 * @return number of samples already requested from this sampler
	 */
	public int numSampled() {
		return this.desiredtotal - this.yetdesired;
	}
}
