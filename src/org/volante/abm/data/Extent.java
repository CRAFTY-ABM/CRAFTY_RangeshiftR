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
 */
package org.volante.abm.data;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import org.apache.log4j.Logger;


/**
 * Keeps track of the extent of an area (e.g. a region) which can be extended by
 * calling {@link #update(Cell)}, {@link #update(Extent)} or
 * {@link #update(int, int)}.
 * 
 * @author dmrust
 * @author Sascha Holzhauer
 * 
 */
public class Extent {

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(Extent.class);

	int minX = Integer.MAX_VALUE;
	int maxX = Integer.MIN_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxY = Integer.MIN_VALUE;
	int width = 0;
	int height = 0;

	public int getMinX() {
		return minX;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMaxY() {
		return maxY;
	}

	public void update(int x, int y) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("update: " + x + " - " + y);
		}
		// LOGGING ->

		minX = min(minX, x);
		maxX = max(maxX, x);
		minY = min(minY, y);
		maxY = max(maxY, y);
		updateHeightWidth();
	}

	public void update(Cell c) {
		update(c.x, c.y);
	}

	public void update(Extent e) {
		minX = min(minX, e.minX);
		maxX = max(maxX, e.maxX);
		minY = min(minY, e.minY);
		maxY = max(maxY, e.maxY);
		updateHeightWidth();
	}

	public void updateHeightWidth() {
		width = abs(maxX - minX) + 1;
		height = abs(maxY - minY) + 1;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Extent (" + minX + "," + minY + "),(" + maxX + "," + maxY + ")";
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Get the zero-indexed address (x value) of the cell for putting into
	 * arrays
	 * 
	 * @param x
	 * @return zero-indexed address (x value)
	 */
	public int xInd(int x) {
		return x - minX;
	}

	/**
	 * Get the zero-indexed address (y value) of the cell for putting into
	 * arrays
	 * 
	 * @param y
	 * @return zero-indexed address (y value)
	 */
	public int yInd(int y) {
		return y - minY;
	}

	/**
	 * Get the cell coordinates corresponding to the given address
	 * 
	 * @param ind
	 * @return cell coordinates
	 */
	public int indToX(int index) {
		return index + minX;
	}

	/**
	 * Get the cell coordinates corresponding to the given address
	 * 
	 * @param ind
	 * @return cell coordinates
	 */
	public int indToY(int index) {
		return index + minY;
	}
}
