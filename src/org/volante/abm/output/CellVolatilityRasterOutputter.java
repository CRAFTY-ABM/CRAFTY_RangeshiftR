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
 * Created by Sascha Holzhauer on 23 Jan 2015
 */
package org.volante.abm.output;

import org.simpleframework.xml.Element;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.models.AllocationModel;
import org.volante.abm.models.utils.CellVolatilityMessenger;
import org.volante.abm.models.utils.CellVolatilityObserver;
import org.volante.abm.models.utils.SimpleCellVolatilityRecorder;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;

/**
 * Uses Observer pattern: Registers at those {@link AllocationModel}s that
 * implement {@link CellVolatilityMessenger}. This is useful since the component
 * that knows about cell volatility is an exchangeable component and this way
 * not every {@link AllocationModel} is required to implement the service.
 * Furthermore, this way cell volatility only needs to be reported in case there
 * is a {@link CellVolatilityObserver} registered.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CellVolatilityRasterOutputter extends RasterOutputter implements GloballyInitialisable {

	/**
	 * Used to record cells volatility, i.e. how often land use and/or agent
	 * instances change.
	 */
	@Element(required = false)
	CellVolatilityObserver volatilityRecorder = new SimpleCellVolatilityRecorder();

	/**
	 * @see org.volante.abm.serialization.GloballyInitialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info)
			throws Exception {
		for (Region r : data.getRootRegionSet().getAllRegions()) {
			if (r.getAllocationModel() instanceof CellVolatilityMessenger) {
				((CellVolatilityMessenger) r.getAllocationModel())
						.registerCellVolatilityOberserver(this.volatilityRecorder);
			}
		}
	}

	/**
	 * @see org.volante.abm.serialization.CellToDouble#apply(org.volante.abm.data.Cell)
	 */
	@Override
	public double apply(Cell c) {
		return this.volatilityRecorder.getVolatility(c) != null ? this.volatilityRecorder
				.getVolatility(c).doubleValue() : 0.0;
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "CellVolatility";
	}
}
