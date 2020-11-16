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
 * Created by Sascha Holzhauer on 2 Dec 2014
 */
package org.volante.abm.institutions.innovation;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.institutions.innovation.repeat.CsvProductivityInnovationRepComp;
import org.volante.abm.institutions.innovation.repeat.InnovationRepComp;
import org.volante.abm.schedule.RunInfo;


/**
 * @author Sascha Holzhauer
 *
 */
public class RepeatingProductivityInnovation extends ProductivityInnovation implements
		RepeatingInnovation {

	@Element(name = "repComp", required = false)
	protected InnovationRepComp repComp = new CsvProductivityInnovationRepComp();

	/**
	 * @param identifier
	 */
	public RepeatingProductivityInnovation(@Attribute(name = "id") String identifier) {
		super(identifier);
	}

	public void initialise(ModelData mData, RunInfo rInfo, Region region) throws Exception {
		super.initialise(mData, rInfo, region);
		this.repComp.initialise(mData, rInfo, region);
	}

	/**
	 * @see org.volante.abm.institutions.innovation.RepeatingInnovation#getNewInnovation()
	 */
	@Override
	public RepeatingInnovation getNewInnovation() {
		RepeatingProductivityInnovation innovation = new RepeatingProductivityInnovation(
				"nothing");
		innovation.adoptionThreshold = adoptionThreshold;
		innovation.affectedServices = affectedServices;
		innovation.affectedAFTs = affectedAFTs;
		innovation.affectiveAFTs = affectiveAFTs;
		innovation.effectDiscountFactor = effectDiscountFactor;
		innovation.effectOnProductivityFactor = effectOnProductivityFactor;
		innovation.identifier = identifier + "_" + rInfo.getSchedule().getCurrentTick();
		innovation.lifeSpan = lifeSpan;
		innovation.trialThreshold = trialThreshold;
		innovation.repComp = repComp;
		innovation = this.getRepetitionComp().adjustRenewedInnovation(innovation);
		return innovation;
	}

	@Override
	public InnovationRepComp getRepetitionComp() {
		return this.repComp;
	}

	/**
	 * @return renewal interval
	 */
	public int getRenewalInterval() {
		return this.getRepetitionComp().getRenewalInterval();
	}
}
