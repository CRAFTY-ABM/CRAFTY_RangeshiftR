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
 * Created by Sascha Holzhauer on 07.05.2014
 */
package org.volante.abm.output;


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.param.ParameterRepository;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;

import com.csvreader.CsvWriter;


/**
 * Simple parameter outputter that return parameter values from {@link ParameterRepository} and
 * calls its {@link #toString()} method.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class ParameterCSVOutputter extends TableOutputter<Region> implements GloballyInitialisable {

	@Attribute(required = false)
	String			doubleFormat	= "0.000";

	@Attribute(required = false)
	String			navalue			= "NA";

	DecimalFormat	doubleFmt		= null;


	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		everyNYears = Integer.MAX_VALUE;
		DecimalFormatSymbols decimalSymbols = new DecimalFormat()
				.getDecimalFormatSymbols();
		decimalSymbols.setDecimalSeparator('.');
		doubleFmt = new DecimalFormat(doubleFormat, decimalSymbols);

		addColumn(new RegionColumn());
		
		HashSet<String> processedColumns = new HashSet<>();
		for (Region r : data.getRootRegionSet().getAllRegions()) {
			if (info.getParamRepos().containsKey(r)) {
				for (String paramname : info.getParamRepos().getRegionParameters(r).keySet()) {
					if (!processedColumns.contains(paramname)) {
						addColumn(new ParameterColumn(paramname));
					} else {
						processedColumns.add(paramname);
					}
				}
			}
		}
	}
	
	/**
	 * Override start to initialise an appending file output stream.
	 * 
	 * @see org.volante.abm.output.TableOutputter#startFile(java.lang.String,
	 *      org.volante.abm.data.Regions)
	 */
	public void startFile(String filename, Regions r) throws IOException {
		endFile(r);
		writers.put(r, new CsvWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filename, true))), ','));
		String[] headers = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++) {
			headers[i] = columns.get(i).getHeader();
		}
		writers.get(r).writeRecord(headers);
	}

	@Override
	public boolean filePerTick() {
		return false;
	}
	
	@Override
	public Iterable<Region> getData(Regions r) {
		return r.getAllRegions();
	}

	@Override
	public String getDefaultOutputName() {
		return "ParameterCSVOutputter";
	}

	public static class RegionColumn implements TableColumn<Region> {
		@Override
		public String getHeader() {
			return "Region";
		}

		@Override
		public String getValue(Region r, ModelData data, RunInfo info, Regions rs) {
			return r.getID();
		}
	}

	public class ParameterColumn implements TableColumn<Region> {
		String	paramName	= "";

		public ParameterColumn(String paramName) {
			this.paramName = paramName;
		}

		@Override
		public String getHeader() {
			return paramName;
		}

		@Override
		public String getValue(Region r, ModelData data, RunInfo info, Regions rs) {
			if (info.getParamRepos().getRegionParameters(r).containsKey(paramName)) {
				return info.getParamRepos().getRegionParameters(r).get(paramName).toString();
			} else {
				return navalue;
			}
		}
	}
}
