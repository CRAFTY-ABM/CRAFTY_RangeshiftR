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
package org.volante.abm.update;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementMap;
import org.volante.abm.data.Capital;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.param.RandomPa;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;

import com.csvreader.CsvReader;
import com.google.common.collect.Table;
import com.moseph.modelutils.distribution.Distribution;
import com.moseph.modelutils.fastdata.DoubleMap;

public class CSVCapitalUpdater extends AbstractUpdater
{
	@Attribute(required=false)
	String							X_COL				= "X";

	@Attribute(required=false)
	String							Y_COL				= "Y";

	@Attribute(required=false)
	boolean yearInFilename = true;

	@Attribute(required=false)
	boolean reapplyPreviousFile = false;
	
	@Attribute(required=false)
	String filename = null;
	
	String previousFilename = null;
	
	@ElementMap(inline=true,key="year",attribute=true,entry="csvFile",required=false)
	Map<Integer,String> yearlyFilenames = new HashMap<Integer, String>();
	
	@ElementMap(inline=true,key="capital",attribute=true,entry="noise",required=false,value="distribution")
	Map<String, Distribution>		distributionsSerial	= new LinkedHashMap<String, Distribution>();
	
	Map<Capital, Distribution>		distributions		= new LinkedHashMap<Capital, Distribution>();
	Table<Integer, Integer, Cell> cellTable = null;

	/**
	 * Do the actual updating
	 */
	@Override
	public void prePreTick()
	{
		try {
			CsvReader file = getFileForYear();
 			if( file != null ) {
				applyFile( file );
			}
		} catch ( Exception e )
		{
			this.log.fatal( "Couldn't update Capitals: " + e.getMessage() );
			e.printStackTrace();
		}
	}

	/**
	 * If there's a file to be applied this year, then get it.
	 * Next, check to see if the year is in the filename, and there's a file that matches.
	 * Finally if we should re-apply the same file (e.g. if there is time-varying noise being added), return that.
	 * Otherwise, return null
	 * @return
	 * @throws IOException 
	 */
	CsvReader getFileForYear() throws IOException
	{
		ABMPersister p = info.getPersister();
		String fn = null;
		String yearly = yearlyFilenames.get( info.getSchedule().getCurrentTick() );
		if (yearly != null
				&& p.csvFileOK(getClass(), yearly, region.getPersisterContextExtra(), X_COL, Y_COL)) {
			fn = yearly;
		} else if (yearInFilename
				&& p.csvFileOK(getClass(), filename, region.getPersisterContextExtra(), X_COL, Y_COL)) {
			fn = filename;
		} else if( reapplyPreviousFile && previousFilename != null ) {
			fn = previousFilename;
		}
		
		this.log.debug("Read " + fn);

		
		if( fn != null )
		{
			previousFilename = fn;
			return p.getCSVReader(fn, region.getPersisterContextExtra());
		}
		
  	
		return null;
	}
	
	/**
	 * Use the csv file to set the capital levels for the cells
	 * @param file
	 * @throws IOException 
	 */
	void applyFile( CsvReader file ) throws IOException
	{
		//Assume we've got the CSV file, and we've read the headers in
		while( file.readRecord() ) //For each entry
		{
			//Try to get the cell
			Cell cell = region.getCell( Integer.parseInt( file.get(X_COL) ), Integer.parseInt( file.get( Y_COL ) ) );
			if( cell == null ) //Complain if we couldn't find it - implies there's data that doesn't line up!
			{
				log.warn("Update for unknown cell:" + file.get(X_COL) + ", " + file.get(Y_COL));
				continue; //Go to next line
			}
			DoubleMap<Capital> adjusted = data.capitalMap();
			cell.getBaseCapitals().copyInto(adjusted);

			for( Capital c : data.capitals ) //Set each capital in turn
			{
				String cap = file.get( c.getName());
				if( cap != null )  //It's possible the file doesn't have all baseCapitals in
				{
					double val = Double.parseDouble( cap );
					//Add noise if we've got it
					if( distributions.containsKey( c )) {
						val += distributions.get( c ).sample();
					}
					adjusted.putDouble(c, val);
				}
			}
			cell.setBaseCapitals(adjusted);
		}
	}
	
	/**
	 * Load in config stuff
	 */
	@Override
	public void initialise( ModelData data, RunInfo info, Region extent ) throws Exception
	{
		super.initialise( data, info, extent );
		//Create a map of the distributions by capital rather than capital name.
		for (Entry<String, Distribution> e : distributionsSerial.entrySet()) {
			e.getValue().init(extent.getRandom().getURService(), RandomPa.RANDOM_SEED_RUN.name());
			distributions.put( data.capitals.forName( e.getKey() ), e.getValue() );
		}
	}
}
