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
package org.volante.abm.output;


import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;


public abstract class AbstractOutputter implements Outputter {
	protected Logger		log			= Logger.getLogger(getClass());
	protected Outputs		outputs		= null;
	@Attribute(required = false)
	private String			outputName	= "";
	@Attribute(required = false)
	String					extension	= "csv";
	protected boolean		disabled	= false;
	@Attribute(required = false)
	protected int			everyNYears	= 1;
	@Attribute(required = false)
	protected int			startYear	= 0;
	@Attribute(required = false)
	protected int			endYear		= Integer.MAX_VALUE;
	protected RunInfo		runInfo		= null;
	protected ModelData		modelData	= null;
	protected ABMPersister	persister	= null;

	@Override
	public void initialise() throws Exception {
		if (disable()) {
			disabled = true;
			return;
		}
	}

	/**
	 * Callback to start a file if one is required. This is good for e.g. csv files which start a
	 * header and append to the same file each time writeRecord() is called but it is not needed for
	 * shapefiles where each writeRecord() creates its own file
	 */
	@Override
	public void open() {
	}

	@Override
	public void close() {
	}

	@Override
	public void setOutputManager(Outputs outputs) {
		this.outputs = outputs;
		this.runInfo = outputs.runInfo;
		this.modelData = outputs.modelData;
		this.persister = runInfo.getPersister();
	}

	public abstract String getDefaultOutputName();

	public String getOutputName() {
		if (outputName == null || outputName.length() == 0) {
			return getDefaultOutputName();
		}
		return outputName;
	}

	public void setOutputName(String outputName) {
		this.outputName = outputName;
	}

	public String getExtension() {
		return extension;
	}

	public String filename(Regions r) {
		return outputs.getOutputFilename(getOutputName(), getExtension(), r);
	}

	public String tickFilename(Regions r) {
		return outputs.getOutputFilename(getOutputName(), getExtension(), outputs.tickPattern, r);
	}

	public boolean disable() {
		return false;
	}

	/**
	 * @see org.volante.abm.output.Outputter#getStartYear()
	 */
	@Override
	public int getStartYear() {
		return this.startYear;
	}

	/**
	 * @see org.volante.abm.output.Outputter#getEndYear()
	 */
	@Override
	public int getEndYear() {
		return this.endYear;
	}

	/**
	 * @see org.volante.abm.output.Outputter#getEveryNYears()
	 */
	@Override
	public int getEveryNYears() {
		return this.everyNYears;
	}
}
