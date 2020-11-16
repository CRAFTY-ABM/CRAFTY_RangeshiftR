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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;


/**
 * Manages creation of output of a variety of types.
 * 
 * Uses substitution patterns to do filenames. There is one pattern for:
 * - outputDirectory
 * - files without time in their name (filePattern)
 * - files with the year in their name (yearlyPattern)
 * - files with the day and year in their name (dailyPattern)
 * 
 * Substitutions are done with % signs, with each letter being a different
 * variable:
 * - %o = output name
 * - %s = scenario name
 * - %i = run ID (i.e. number within a batch run, may be timestamp unless otherwise set)
 * - %u = timestamp (almost certainly unique)
 * - %y = current tick/year
 * - %r = current region
 * 
 * As an example, a file pattern of "%s-%o-%y" and an outputDirectory of "output/%s-%i"
 * might create a file: "output/A1-3/A1-parcels-2034.asc" (for a raster)
 * @author dmrust
 *
 */
public class Outputs implements GloballyInitialisable {
	@Attribute(required = false, name = "outputDirectory")
	String					outputDirectoryPattern	= "output/%w/%s/%i/%r";
	@Attribute(required = false)
	String					filePattern				= "%s-%i-%r-%o";
	@Attribute(required = false)
	String					tickPattern				= "%s-%i-%r-%o-%y";
	@Attribute(required = false)
	String					outputsFile				= "";
	@ElementList(inline = true, required = false, entry = "output")
	List<Outputter> outputs = new ArrayList<>();
	@Attribute(required = false)
	boolean					clearExistingFiles		= true;
	Logger					log						= Logger.getLogger(getClass());
	protected RunInfo		runInfo					= null;
	protected ModelData		modelData				= null;
	List<CloseableOutput> outputsToClose = new ArrayList<>();

	Thread					shutdownHookThread;

	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		runInfo = info;
		modelData = data;
		// Setup timestamp for output
		runInfo.getPersister().setContext("u", System.currentTimeMillis() + "");
		// If there's no runID set, then use a timestamp to ensure different directories
		if (runInfo.getRunID() == null || runInfo.getRunID().equals("")) {
			runInfo.getPersister().setContext("i", System.currentTimeMillis() + "");
		}

		if (outputsFile != null && outputsFile.length() > 0) {
			// TODO override persister method
			Outputs op = runInfo.getPersister().readXML(Outputs.class, outputsFile, null);
			outputs.addAll(op.outputs);
		}
		for (Outputter o : outputs) {
			log.info("Loading Output: " + o.getClass());
			o.setOutputManager(this);
			o.initialise(); // Outputs do their own scheduling in initialise();
			if (o instanceof GloballyInitialisable) {
				((GloballyInitialisable) o).initialise(data, info);
			}
			o.open();
		}
		runInfo.setOutputs(this);
		setupClosingOutputs();
	}

	public void doOutput(Regions r) {
		for (Outputter o : outputs) {
			if (this.runInfo.getSchedule().getCurrentTick() >= o.getStartYear()
					&& (this.runInfo.getSchedule().getCurrentTick() <= o.getEndYear())
					&& (this.runInfo.getSchedule().getCurrentTick() - o.getStartYear())
							% o.getEveryNYears() == 0) {
				// <- LOGGING
				log.info("Handle outputter " + o);
				// LOGGING ->

				o.doOutput(r);
			}
		}
	}

	public void finished() {
		for (Outputter o : outputs) {
			o.close();
		}
	}

	public void addOutput(Outputter out) {
		outputs.add(out);
		out.setOutputManager(this);
	}

	/*
	 * Convenience wrappers for getOutputFilename
	 */
	public String getOutputFilename(String output, String extension) {
		return getOutputFilename(output, extension, filePattern);
	}

	public String getOutputFilename(String output, String extension, String pattern) {
		return getOutputFilename(output, extension, pattern, null);
	}

	public String getOutputFilename(String output, String extension, Regions r) {
		return getOutputFilename(output, extension, filePattern, r);
	}

	/**
	 * Gets an output filename, and makes sure that the relevant directory exists
	 * 
	 * @param output
	 * @param extension
	 * @param pattern
	 * @param r
	 * @return output filename
	 */
	public String getOutputFilename(String output, String extension, String pattern, Regions r) {
		Map<String, String> extra = new HashMap<>();
		if (r != null) {
			extra.put("r", r.getID());
		}
		if (output != null) {
			extra.put("o", output);
		}
		
		// substitute sys vars:
		int firstMarker = 0;
		int secondMarker = -1;
		while (firstMarker >= 0) {
			firstMarker = outputDirectoryPattern.indexOf("$", secondMarker + 1);
			secondMarker = outputDirectoryPattern.indexOf("$", firstMarker + 1);

			if (firstMarker >= 0) {
				if (secondMarker < 0) {
					throw new IllegalStateException(
						"System Variables need to be surrounded by '$' ($VAR$)");
				} else {
					String sysvar = outputDirectoryPattern.substring(firstMarker + 1,
							secondMarker);
					String result = System.getenv(sysvar);
					outputDirectoryPattern = outputDirectoryPattern.replace('$' + sysvar + '$',
							result);
				}
			}
		}
		log.info("Final output directory: " + outputDirectoryPattern);

		String outputFile = runInfo.getPersister().ensureDirectoryExists( pattern, outputDirectoryPattern, false, extra );
		if( extension != null ) 
		{
			if( extension.startsWith( "." )) {
				outputFile = outputFile + extension;
			} else {
				outputFile = outputFile + "." + extension;
			}
		}
		return outputFile;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectoryPattern = outputDirectory;
	}

	public boolean isClearExistingFiles() {
		return clearExistingFiles;
	}

	public void setClearExistingFiles(boolean clearExistingFiles) {
		this.clearExistingFiles = clearExistingFiles;
	}

	public static interface CloseableOutput extends Outputter {
	} // Doesn't actually do anything, just lets us know we have to close it

	public void registerClosableOutput(CloseableOutput o) {
		outputsToClose.add(o);
	}

	void setupClosingOutputs() {
		this.shutdownHookThread = new Thread() {
			@Override
			public void run() {
				for (CloseableOutput c : outputsToClose) {
					c.close();
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(this.shutdownHookThread);
	}

	public void removeClosingOutputThreads() {
		log.info("Closing outputs and remove shutdown hook...");
		this.shutdownHookThread.run();
		Runtime.getRuntime().removeShutdownHook(this.shutdownHookThread);
	}
}
