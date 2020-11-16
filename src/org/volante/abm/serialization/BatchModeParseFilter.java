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
 * Created by Sascha Holzhauer on 16 Sep 2014
 */
package org.volante.abm.serialization;


import org.apache.log4j.Logger;
import org.simpleframework.xml.filter.Filter;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class BatchModeParseFilter implements Filter {

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(BatchModeParseFilter.class);

	protected RunInfo	info;

	public void setRunInfo(RunInfo info) {
		this.info = info;
	}

	/**
	 * @see org.simpleframework.xml.filter.Filter#replace(java.lang.String)
	 */
	@Override
	public String replace(String arg0) {
		if (info == null) {
			logger.error("RunInfo has not been set!");
			throw new IllegalStateException("RunInfo has not been set!");
		}
		return BatchRunParser.parseString(arg0, info);
	}
}
