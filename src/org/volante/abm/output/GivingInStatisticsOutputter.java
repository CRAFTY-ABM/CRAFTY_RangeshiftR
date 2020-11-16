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
 * Created by Sascha Holzhauer on 14 Aug 2015
 */
package org.volante.abm.output;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.HashBag;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Attribute;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.agent.fr.InstitutionalFR;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.models.utils.GivingInStatisticsMessenger;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;

import com.csvreader.CsvWriter;


/**
 * Records number of searches that end up with a certain number of trials (rows) for every region
 * (columns) and every AFT (column) within a region. Optionally per tick (rows).
 * 
 * The messenger only reports take overs that occur. Therefore, this class must check whether there
 * are only take overs for a specific AFT in a particular region.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class GivingInStatisticsOutputter extends TableOutputter<Integer> implements
		GloballyInitialisable, GivingInStatisticsObserver {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(GivingInStatisticsOutputter.class);

	@Attribute(required = false)
	boolean										addTick				= true;

	/**
	 * Only considered when <code>perRegion==false</code> (otherwise, regions are considered in
	 * column headers).
	 */
	@Attribute(required = false)
	boolean										addRegion			= true;

	Map<Region, Map<FunctionalRole, Bag<Integer>>> numSearchedCells = new HashMap<>();

	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		if (this.startYear <= info.getSchedule().getStartTick()) {
			logger.warn("This outputter's start year must be after the initial simulation year!");
		}

		for (Region r : data.getRootRegionSet().getAllRegions()) {
			if (r.getAllocationModel() instanceof GivingInStatisticsMessenger) {
				((GivingInStatisticsMessenger) r.getAllocationModel())
						.registerGivingInStatisticOberserver(this);
			}
		}
	}

	/**
	 * @see org.volante.abm.output.AbstractOutputter#setOutputManager(org.volante.abm.output.Outputs)
	 */
	@Override
	public void setOutputManager(Outputs outputs) {
		super.setOutputManager(outputs);

		if (addTick) {
			addColumn(new TickColumn<Integer>());
		}

		if (addRegion && perRegion) {
			addColumn(new RegionsColumn<Integer>());
		}

		addColumn(new NumberColumn());
	}

	public void initGivingInStatistic(Region region) {
		for (FunctionalRole fr : region.getFunctionalRoles()) {
			if (!(fr instanceof InstitutionalFR)) {
				addColumn(new SearchedCellsAftColumn(fr.getLabel(), fr, region));
			}
		}
	}

	@Override
	public Iterable<Integer> getData(Regions r) {
		Set<Integer> regionIntegers = new HashSet<>();
		for (Region region : r.getAllRegions()) {
			if (numSearchedCells.containsKey(region)) {
				for (FunctionalRole fr : region.getFunctionalRoles()) {
					if (!(fr instanceof InstitutionalFR) && numSearchedCells.get(region).containsKey(fr)) {
						for (Integer integer : numSearchedCells.get(region).get(fr)) {
							regionIntegers.add(integer);
						}
					}
				}
			}
		}
		return regionIntegers;
	}

	@Override
	public String getDefaultOutputName() {
		return "GivingInStatistics";
	}

	/**
	 * Filter out SearchedCellsAftColumn that are not among requested regions (important for
	 * region-specific files)
	 * 
	 * @see org.volante.abm.output.TableOutputter#startFile(java.lang.String,
	 *      org.volante.abm.data.Regions)
	 */
	public void startFile(String filename, Regions regions) throws IOException {
		endFile(regions);
		writers.put(regions, new CsvWriter(filename));

		Set<Region> regionsSet = new HashSet<>();
		for (Region reg : regions.getAllRegions()) {
			regionsSet.add(reg);
		}

		String[] headers = new String[columns.size()];

		int columnnum = -1;
		for (int i = 0; i < columns.size(); i++) {

			if (!(columns.get(i) instanceof SearchedCellsAftColumn)
					|| regionsSet.contains(((SearchedCellsAftColumn) columns.get(i)).getRegion())) {
				columnnum++;
				headers[columnnum] = columns.get(i).getHeader();
			}
		}

		String[] outputShort = new String[columnnum + 1];
		for (int i = 0; i <= columnnum; i++) {
			outputShort[i] = headers[i];
		}

		writers.get(regions).writeRecord(outputShort);
	}

	/**
	 * @see org.volante.abm.output.TableOutputter#writeData(java.lang.Iterable,
	 *      org.volante.abm.data.Regions)
	 */
	public void writeData(Iterable<Integer> data, Regions r) throws IOException {
		String[] output = new String[columns.size()];
		Set<Region> regions = new HashSet<>();
		for (Region reg : r.getAllRegions()) {
			regions.add(reg);
		}

		for (Integer d : data) {
			int columnnum = -1;
			for (int i = 0; i < columns.size(); i++) {
				// filter out SearchedCellsAftColumn that are not among requested regions (important
				// for region-specific files):
				if (!(columns.get(i) instanceof SearchedCellsAftColumn)
						|| regions.contains(((SearchedCellsAftColumn) columns.get(i)).getRegion())) {
					columnnum++;
					output[columnnum] = columns
							.get(i)
							.getValue(
									d,
									modelData,
									runInfo,
									(columns.get(i) instanceof SearchedCellsAftColumn ? ((SearchedCellsAftColumn) columns
											.get(i)).getRegion()
											: r));
				}
			}

			String[] outputShort = new String[columnnum + 1];
			for (int i = 0; i <= columnnum; i++) {
				outputShort[i] = output[i];
			}

			writers.get(r).writeRecord(outputShort);
			writers.get(r).flush();
		}

		// reset particular region:
		for (Region reg : r.getAllRegions()) {
			if (numSearchedCells.containsKey(reg)) {
				for (FunctionalRole pa : numSearchedCells.get(reg).keySet()) {
					numSearchedCells.get(reg).get(pa).clear();
				}
			}
		}
	}

	@Override
	public void setNumberSearchedCells(Region region, FunctionalRole pa, int number) {
		if (!this.numSearchedCells.containsKey(region)) {
			this.numSearchedCells.put(region, new HashMap<FunctionalRole, Bag<Integer>>());
		}
		if (!this.numSearchedCells.get(region).containsKey(pa)) {
			this.numSearchedCells.get(region).put(pa, new HashBag<Integer>());
		}
		this.numSearchedCells.get(region).get(pa).add(number);
	}

	public class NumberColumn implements TableColumn<Integer> {

		public NumberColumn() {
		}

		@Override
		public String getHeader() {
			return "Trials";
		}

		@Override
		public String getValue(Integer integer, ModelData data, RunInfo info,
				Regions rs) {
			return "" + integer;
		}
	}

	public class SearchedCellsAftColumn implements TableColumn<Integer> {
		String			aftName	= "";
		FunctionalRole fr;
		Region			region;

		public SearchedCellsAftColumn(String aftName, FunctionalRole fr, Region region) {
			this.aftName = aftName;
			this.fr = fr;
			this.region = region;
		}

		public Region getRegion() {
			return this.region;
		}

		@Override
		public String getHeader() {
			return ("" + this.aftName + (!GivingInStatisticsOutputter.this.perRegion ? "["
					+ region.getID() + "]" : ""));
		}

		@Override
		public String getValue(Integer integer, ModelData data, RunInfo info,
				Regions rs) {
			if (numSearchedCells.containsKey(this.region)
 && numSearchedCells.get(this.region).containsKey(fr)) {
				return ""
 + numSearchedCells.get(this.region).get(fr)
								.getCount(integer);
			} else {
				return "0";
			}
		}
	}
}
