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
package org.volante.abm.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.RegionSet;
import org.volante.abm.schedule.RunInfo;

import com.csvreader.CsvReader;

import mpi.MPI;


public class WorldLoader {

	/**
	 * Logger
	 */
	static private Logger	logger				= Logger.getLogger(WorldLoader.class);

	@ElementList(required=false,inline=true,entry="region")
	List<RegionLoader> loaders = new ArrayList<RegionLoader>();
	@ElementList(required=false,inline=true,entry="regionFile")
	List<String> regionFiles = new ArrayList<String>();
	@ElementList(required=false,inline=true,entry="regionCSV")
	List<String> regionCSV = new ArrayList<String>();

	@Attribute(required=false)
	String					pidColumn			= "pid";
	@Attribute(required = false)
	String idColumn = "ID";
	@Attribute(required=false)
	String competitionColumn = "Competition";
	@Attribute(required=false)
	String allocationColumn = "Allocation";

	@Attribute(required = false)
	String handleAmbulantAgentsColumn = "HandleAmbulant";

	@Attribute(required=false)
	String demandColumn = "Demand";

	@Attribute(required = false)
	String btColumn = "Behavioural Types";
	@Attribute(required=false)
	String frColumn = "Functional Roles";

	@Attribute(required=false)
	String institutionsColumn = "Institutions";
	@Attribute(required = false)
	String cellColumn = "Cell Initialisers";
	@Attribute(required=false)
	String socNetColumn = "Social Network";

	@Attribute(required=false)
	String initialisersColumn = "Initialisers";

	@Attribute(required = false)
	String agentColumn = "Agent Initialisers";

	@Attribute(required = false)
	String updatersColumn = "Updaters";

	@Attribute(required = false)
	String laraModelColumn = "LARA Model";

	ABMPersister persister = ABMPersister.getInstance();
	ModelData modelData = new ModelData();
	RunInfo					info				= null;

	public WorldLoader() {}
	public WorldLoader( ModelData data, ABMPersister persister )
	{
		this.modelData = data;
		this.persister = persister;
	}

	public void initialise( RunInfo info ) throws Exception
	{
		this.info = info;
		for( String l : regionFiles ) {
			// TODO override persister method
			loaders.add(persister.readXML(RegionLoader.class, l, null));
		}
		for( String c : regionCSV ) {
			loaders.addAll(allLoaders(BatchRunParser.parseString(c, info)));
		}
	}

	public RegionSet getWorld() throws Exception
	{
		RegionSet rs = new RegionSet();
		for( RegionLoader rl : loaders ) {

			Region r = loadRegion(rl);

			try {

				Class.forName("mpi.MPI");
				if (MPI.COMM_WORLD.Rank() == rl.getUid()) {

					logger.info("Run region " + r + " on rank " + MPI.COMM_WORLD.Rank());

				}
			} catch (NoClassDefFoundError ncde) {
				logger.error("NoClassDefFoundError: No MPI. Region " + r + " loaded.");

			} catch (UnsatisfiedLinkError ule) {
				logger.error("MPI is in classpath but not linked to shared libraries correctly (this message can be ignored if not running in parallel)!" + " No MPI. Region \" + r + \" loaded.\");");

			} catch (ClassNotFoundException cnfe) {
				logger.error("ClassNotFoundException: No MPI. Region " + r + " loaded.");

			} finally { 

				rs.addRegion(r);

			}
 

	}
	return rs;
}

Region loadRegion( RegionLoader l ) throws Exception
{

	try {

		l.setPersister( persister );
		l.setModelData( modelData );
		l.initialise( info );

	} catch (Exception exception) {
		logger.fatal("Loading region failed: " + exception.toString() + exception.getStackTrace());
		// exception.printStackTrace();
		System.exit(0);
	}

	return l.getRegion();
}

Set<RegionLoader> allLoaders( String csvFile ) throws IOException
{
	Set<RegionLoader> loaders = new LinkedHashSet<RegionLoader>();
	// TODO override persister method
	CsvReader reader = persister.getCSVReader(csvFile, null);

	while( reader.readRecord() ) {
		if (reader.getColumnCount() <= 1) {
			logger.warn("There was no column detected in your CSV world XML file " + csvFile
					+ ". It is most" +
					"likely that columns are not separated by column!");
		}
		loaders.add( loaderFromCSV( reader ));
	}
	return loaders;
}

RegionLoader loaderFromCSV( CsvReader reader ) throws IOException
{
	RegionLoader rl = new RegionLoader(
			((reader.getIndex(pidColumn) == -1) ? "-1" : BatchRunParser.parseString(
					reader.get(pidColumn), info)),
			BatchRunParser.parseString(reader.get(idColumn), info),
			BatchRunParser.parseString(reader.get(competitionColumn), info),
			BatchRunParser.parseString(reader.get(allocationColumn), info),
			Boolean.parseBoolean(reader.get(handleAmbulantAgentsColumn)),
			BatchRunParser.parseString(reader.get(demandColumn), info),
			BatchRunParser.parseString(reader.get(btColumn), info), 
			BatchRunParser.parseString(reader.get(frColumn), info),
			BatchRunParser.parseString(reader.get(cellColumn), info),
			null,
			BatchRunParser.parseString(reader.get(socNetColumn), info),
			BatchRunParser.parseString(reader.get(institutionsColumn), info),
			BatchRunParser.parseString(
					reader.get(updatersColumn), info),
			BatchRunParser.parseString(reader.get(laraModelColumn), info));

	if (!reader.get(this.initialisersColumn).equals("")) {
		rl.initialiserFiles.add(reader.get(this.initialisersColumn));
	}
	return rl;
}

public void setModelData( ModelData data ) { this.modelData = data; }
}
