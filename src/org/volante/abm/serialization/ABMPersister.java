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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.simpleframework.xml.transform.Matcher;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Extent;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;

import com.csvreader.CsvReader;
import com.moseph.gis.raster.Raster;
import com.moseph.gis.raster.RasterWriter;
import com.moseph.modelutils.curve.LinearInterpolator;
import com.moseph.modelutils.serialisation.EasyPersister;

/**
 * Note: The Raster class is not well implemented. Calling {@link Raster#getNDATA()} without a
 * previous call to {@link Raster#setNDATA(String)} may cause a segmentation fault since the object
 * it returns has not been initialised.
 *
 * @author Dave Murray-Rust
 * @author Sascha Holzhauer
 *
 */
public class ABMPersister extends EasyPersister {
	
	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(ABMPersister.class);
	
	public static final String	REGION_CONTEXT_KEY	= "r";

	static ABMPersister	instance	= null;

	RunInfo				rInfo		= null;
	BatchModeParseFilter	filter		= null;

	private ABMPersister(BatchModeParseFilter filter) {
		super(filter);
		this.filter = filter;
	}

	private ABMPersister(BatchModeParseFilter filter, Matcher matcher) {
		super(filter, matcher);
		this.filter = filter;
	}

	public static ABMPersister getInstance() {
		if (instance == null) {
			instance = new ABMPersister(new BatchModeParseFilter());
		}
		return instance;
	}

	public static void reset() {
		instance = null;
	}

	public void regionsToRaster(String filename, Regions r, CellToDouble converter,
			boolean writeInts, String nDataString) throws Exception {
		this.regionsToRaster(filename, r, converter, writeInts, null, nDataString);
	}

	public void regionsToRaster(String filename, Regions r, CellToDouble converter,
			boolean writeInts, DecimalFormat format, String nDataString) throws Exception {
		
		// <- LOGGING
		logger.info("Regions to raster...");
		// LOGGING ->
		
		Extent e = r.getExtent();
		Raster raster = new Raster(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
		raster.setNDATA(nDataString);
		for (Cell c : r.getAllCells()) {
			raster.setXYValue(c.getX(), c.getY(), converter.apply(c));
		}

		RasterWriter writer = new RasterWriter();
		if (format != null) {
			writer.setCellFormat(format);
		} else if (writeInts) {
			writer.setCellFormat(RasterWriter.INT_FORMAT);
		}
		writer.writeRaster(filename, raster);
	}

	public void setRunInfo(RunInfo info) {
		this.rInfo = info;
		this.filter.setRunInfo(info);
	}

	@Override
	public Map<String, LinearInterpolator> csvVerticalToCurves(String csvFile, String xCol,
			Collection<String> columns, Map<String, String> extra) throws IOException {
		// TODO check if LinkedHashMap required
		Map<String, LinearInterpolator> map = new LinkedHashMap<String, LinearInterpolator>();
		CsvReader reader = getCSVReader(csvFile, extra);


		String xColHeader = xCol;
		Collection<String> columHeader = columns;

		if (!Arrays.asList(reader.getHeaders()).contains(xColHeader)) {
			logger.warn("The specified x column header (" + xColHeader
					+ ") is not present (" + csvFile
					+ ") . Using first column...");
			xColHeader = reader.getHeaders()[0];
		}

		if (xColHeader == null) {
			xColHeader = reader.getHeaders()[0];
		}

		if (columHeader == null || columHeader.size() == 0) {
			columHeader = new ArrayList<String>(Arrays.asList(reader.getHeaders()));
		}
		columHeader.remove(xCol);

		List<String> headers = Arrays.asList(reader.getHeaders());
		for (String s : columHeader) {
			if (!headers.contains(s)) {
				logger.error("The requested column (" + s + ") is not present (" + csvFile + ").");
			}
			map.put(s, new LinearInterpolator());
		}

		while (reader.readRecord() && reader.get(xColHeader).length() > 0) {
			double year = Double.parseDouble(reader.get(xColHeader));
			for (String s : columHeader) {
				map.get(s).addPoint(year, BatchRunParser.parseDouble(reader.get(s), rInfo));
			}
		}
		return map;
	}

	/**
	 * Does not accept the regional context key (usually 'r'). Pass the region name directly to
	 * particular methods using the extra parameter!
	 * 
	 * Override to prevent general setting of region context (which may cause faults when more than
	 * one region is simulated).
	 * 
	 * @see com.moseph.modelutils.serialisation.EasyPersister#setContext(java.lang.String,
	 *      java.lang.String)
	 */
	public void setContext(String key, String value)
	{
		if (key.equals(REGION_CONTEXT_KEY)) {
			throw new IllegalArgumentException("The regional context (" + REGION_CONTEXT_KEY
					+ ") may not be set via this method. "
					+ "Pass the region name directly to particular methods!");
		}
		super.setContext(key, value);
	}

	public void setBaseDir(String baseDir) {
		super.setBaseDir(baseDir);
		CsvBatchRunParser.reset();
	}
}
