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
 * Created by Sascha Holzhauer on 31 Oct 2015
 */
package org.volante.abm.output;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.models.utils.CellVolatilityMessenger;
import org.volante.abm.models.utils.CellVolatilityObserver;
import org.volante.abm.models.utils.SimpleCellVolatilityRecorder;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;


/**
 * @see CellVolatilityRasterOutputter
 * 
 * @author Sascha Holzhauer
 * 
 */
public class AggregateCellVolatilityCSVOutputter extends AggregateCSVOutputter implements GloballyInitialisable {

	/**
	 * Used to record cells volatility, i.e. how often land use and/or agent instances change.
	 */
	@Element(required = false)
	CellVolatilityObserver volatilityRecorder = new SimpleCellVolatilityRecorder();

	@Attribute(required = false)
	boolean addNumChangedCells = true;

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "AggregateCellVolatility";
	}

	/**
	 * @see org.volante.abm.serialization.GloballyInitialisable#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo)
	 */
	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		for (Region r : data.getRootRegionSet().getAllRegions()) {
			if (r.getAllocationModel() instanceof CellVolatilityMessenger) {
				((CellVolatilityMessenger) r.getAllocationModel())
						.registerCellVolatilityOberserver(this.volatilityRecorder);
			}
		}
	}

	@Override
	public void setOutputManager(Outputs outputs) {
		super.setOutputManager(outputs);
		addColumn(new CellVolatilityColumn());

		if (addNumChangedCells) {
			addColumn(new VolatileCellsColumn());
		}
	}

	public class CellVolatilityColumn implements TableColumn<Region> {

		/**
		 * @see org.volante.abm.output.TableColumn#getHeader()
		 */
		@Override
		public String getHeader() {
			return "CellVolatility";
		}

		/**
		 * Sums every cell's volatility
		 * 
		 * @see org.volante.abm.output.TableColumn#getValue(java.lang.Object, org.volante.abm.data.ModelData,
		 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
		 */
		@Override
		public String getValue(Region r, ModelData data, RunInfo info, Regions rs) {
			double sum = 0;
			for (Cell c : r.getAllCells()) {
				sum +=
						volatilityRecorder.getVolatility(c) != null ? volatilityRecorder.getVolatility(c).doubleValue()
								: 0.0;
			}
			return doubleFmt.format(sum);
		}
	}

	public class VolatileCellsColumn implements TableColumn<Region> {

		/**
		 * @see org.volante.abm.output.TableColumn#getHeader()
		 */
		@Override
		public String getHeader() {
			return "NumVolatileCells";
		}

		/**
		 * Sums every cell's volatility
		 * 
		 * @see org.volante.abm.output.TableColumn#getValue(java.lang.Object, org.volante.abm.data.ModelData,
		 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
		 */
		@Override
		public String getValue(Region r, ModelData data, RunInfo info, Regions rs) {
			int sum = 0;
			for (Cell c : r.getAllCells()) {
				if (volatilityRecorder.getVolatility(c) != null
						&& volatilityRecorder.getVolatility(c).doubleValue() > 0.0) {
					sum++;
				}
			}
			return "" + sum;
		}
	}
}