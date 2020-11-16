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
 * Created by Sascha Holzhauer on 26.03.2014
 */
package org.volante.abm.output;


import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;

import com.csvreader.CsvWriter;

import de.cesr.lara.components.eventbus.impl.LDcSpecificEventbus;
import de.cesr.lara.components.eventbus.impl.LEventbus;
import de.cesr.lara.components.model.impl.LModel;
import de.cesr.lara.toolbox.io.LEventbusStateCsvOutput;


/**
 * Does not consider \code{perRegion} (yet)!
 * 
 * @author Sascha Holzhauer
 * 
 */
public class EventbusStateOutputter extends AbstractOutputter {

	@Attribute(required = false, name = "tickPattern")
	String tickPattern = "";

	@Attribute(required = false)
	protected boolean perRegion = false;

	/**
	 * Recommended to set to \code{true} as there is no tick column output.
	 */
	@Attribute(required = false)
	protected boolean filePerTick = true;

	/**
	 * Use the simple class name instead of qualified one when set to true.
	 */
	@Attribute(required = false)
	protected boolean simpleEventClass = true;

	protected Map<Regions, CsvWriter> writers = new HashMap<Regions, CsvWriter>();

	/**
	 * @see org.volante.abm.output.Outputter#doOutput(org.volante.abm.data.Regions)
	 */
	@Override
	public void doOutput(Regions regions) {
		if (perRegion) {

			for (Region r : regions.getAllRegions()) {
				writeFile(r);
			}
		} else {
			writeFile(regions);
		}
	}

	/**
	 * @param r
	 */
	protected void writeFile(Regions regions) {
		try {
			String filename = filePerTick() ? tickFilename(regions) : filename(regions);
			if (filePerTick()) {
				startFile(filename, regions);

			} else if (writers.get(regions) == null) {
				startFile(filename, regions);
			}

			boolean first = true;
			for (Region r : regions.getAllRegions()) {
				LEventbus eventbus = LModel.getModel(r).getLEventbus();
				if (eventbus instanceof LDcSpecificEventbus) {
					LEventbusStateCsvOutput.outputEventbusState((LDcSpecificEventbus) eventbus, writers.get(regions),
							(r
							.getRinfo().getSchedule().getCurrentTick() == startYear
 || this.filePerTick) && first,
							simpleEventClass, !this.perRegion);
				} else {
					LEventbusStateCsvOutput.outputEventbusState(eventbus, writers.get(regions), (r.getRinfo()
							.getSchedule()
							.getCurrentTick() == startYear
 || this.filePerTick)
							&& first, simpleEventClass, !this.perRegion);
				}
				first = false;
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void startFile(String filename, Regions r) throws IOException {
		endFile(r);
		writers.put(r, new CsvWriter(filename));
	}

	public void endFile(Regions r) {
		if (writers.get(r) != null) {
			writers.get(r).close();
		}
		writers.remove(r);
	}

	@Override
	public void close() {
		Set<Regions> regions = new LinkedHashSet<>();
		for (Regions r : writers.keySet()) {
			regions.add(r);
		}
		for (Regions r : regions) {
			endFile(r);
		}
	}

	/**
	 * @return true if a separate file is output per tick
	 */
	public boolean filePerTick() {
		return filePerTick;
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getDefaultOutputName()
	 */
	@Override
	public String getDefaultOutputName() {
		return "LEventbusState";
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#getExtension()
	 */
	@Override
	public String getExtension() {
		return "csv";
	}

	@Override
	public String tickFilename(Regions r) {
		if (tickPattern.length() == 0) {
			tickPattern = outputs.tickPattern;
		}
		return outputs.getOutputFilename(getOutputName(), getExtension(), this.tickPattern, r);
	}
}
