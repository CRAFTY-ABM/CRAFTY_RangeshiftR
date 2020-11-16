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
package org.volante.abm.models.utils;

import java.util.HashMap;
import java.util.Map;

import org.volante.abm.data.Cell;

/**
 * The recorder stores the number of any changes in a cell which may be changes
 * of land uses and/or changes of agents (possibly without change in land use).
 * Therefore, the recorder stores more information that would be available when
 * analysing only land use changes of cells.
 * 
 * @author Sascha Holzhauer
 */
public interface CellVolatilityObserver {

	Map<Cell, Integer> cellVolatility = new HashMap<Cell, Integer>();


	/**
	 * @param cell
	 * @return volatility of this cell
	 */
	public Number getVolatility(Cell cell);

	/**
	 * Increases the given cell's volatility.
	 * 
	 * @param cell
	 */
	public void increaseVolatility(Cell cell);

	/**
	 * Reset stored volatilities.
	 */
	public void reset();
}
