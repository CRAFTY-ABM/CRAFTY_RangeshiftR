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
 * Created by Sascha Holzhauer on 18 Nov 2016
 */
package org.volante.abm.example;


import org.simpleframework.xml.Element;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.institutions.pa.GlobalCompFuncSubsidyPa.GlobalCompFuncSubsidyPaFactory;
import org.volante.abm.models.CompetitivenessModel;
import org.volante.abm.models.DemandModel;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;
import org.volante.abm.visualisation.CurveCompetitivenessDisplay;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * Indented for institutions that change (e.g. subsidise) agents' competitiveness on a per service basis and want to
 * adjust the competition functions rather than only adjusting the production that feeds the competition model.
 * 
 * Compare {@link GlobalCompFuncSubsidyPaFactory}.
 * 
 * TODO test
 * 
 * @author Sascha Holzhauer
 * 
 */
public class AdaptionCurveCompetitivenessModel extends CurveCompetitivenessModel {

	/**
	 * If not defined and null, the regions competition model will be considered.
	 */
	@Element(name = "parentModelFilename", required = false)
	protected String filename = null;

	protected CurveCompetitivenessModel baseModel;

	/**
	 * @see org.volante.abm.serialization.Initialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	@Override
    public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);

		if (this.linearCSV != null) {
			throw new IllegalStateException(
			        "The linear CSV file is not considered in the AdaptionCurveCompetitivenessModel!");
		}
		
    	// initialise base model:
		if (this.filename != null) {
			this.baseModel = ABMPersister.getInstance().readXML(CurveCompetitivenessModel.class, filename,
					this.region.getPersisterContextExtra());			
		} else {
			CompetitivenessModel compModel = extent.getCompetitionModelCopy();
			if (!(compModel instanceof CurveCompetitivenessModel)) {
				throw new IllegalStateException("This setting requires a CurveCompetitivenessModel configured at region " + this.region);
			}
			this.baseModel = (CurveCompetitivenessModel) compModel;
		}

		// exchange curves for defined services
		for (Service service : this.curves.keySet()) {
			this.baseModel.curves.put(service, this.curves.get(service));
		}
    }

	/**
	 * @see org.volante.abm.models.CompetitivenessModel#getCompetitiveness(org.volante.abm.models.DemandModel,
	 *      com.moseph.modelutils.fastdata.UnmodifiableNumberMap, org.volante.abm.data.Cell)
	 */
	@Override
	public double getCompetitiveness(DemandModel demand, UnmodifiableNumberMap<Service> supply, Cell cell) {
		return this.baseModel.getCompetitiveness(demand, supply, cell);
	}

	/**
	 * @see org.volante.abm.models.CompetitivenessModel#getCompetitiveness(org.volante.abm.models.DemandModel,
	 *      com.moseph.modelutils.fastdata.UnmodifiableNumberMap)
	 */
	@Override
	public double getCompetitiveness(DemandModel demand, UnmodifiableNumberMap<Service> supply) {
		return this.baseModel.getCompetitiveness(demand, supply);
	}

	/**
	 * @see org.volante.abm.models.CompetitivenessModel#addUpMarginalUtilities(com.moseph.modelutils.fastdata.UnmodifiableNumberMap,
	 *      com.moseph.modelutils.fastdata.UnmodifiableNumberMap)
	 */
	@Override
	public double addUpMarginalUtilities(UnmodifiableNumberMap<Service> demand, UnmodifiableNumberMap<Service> supply) {
		return this.baseModel.addUpMarginalUtilities(demand, supply);
	}

	/**
	 * @see org.volante.abm.models.CompetitivenessModel#getDisplay()
	 */
	@Override
	public CurveCompetitivenessDisplay getDisplay() {
		return this.baseModel.getDisplay();
	}
}
