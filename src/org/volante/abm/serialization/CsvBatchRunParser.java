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
 * Created by Sascha Holzhauer on 15 Sep 2014
 */
package org.volante.abm.serialization;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.volante.abm.schedule.RunInfo;

import com.csvreader.CsvReader;


/**
 * NOTE: Caching is save in this static class since data identifiers are filenames which are unique
 * in the entire simulation.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class CsvBatchRunParser {

	/**
	 * Logger
	 */
	static private Logger											logger			= Logger.getLogger(CsvBatchRunParser.class);

	static final String												LINKS_TABLE			= "/Links.csv";
	static final String												LINKS_TABLE_DEFAULT	= "DefaultPath";
	static final String												LINKS_TABLE_ID		= "ID";
	static final String												LINKS_TABLE_VALUE	= "Value";

	protected static Map<String, Map<String, Map<Integer, String>>>	cachedCsvData	= new HashMap<String, Map<String, Map<Integer, String>>>();
	protected static Map<String, Map<String, String>>				cachedLinksData		= null;
	protected static Map<String, String>							firstColumns	= new HashMap<String, String>();

	/**
	 * @param text
	 * @param rInfo
	 * @return parsed value
	 */
	public static double parseDouble(String text, RunInfo rInfo) {
		return Double.parseDouble(getValue(text, rInfo));
	}

	/**
	 * @param text
	 * @param rInfo
	 * @return parsed value
	 */
	public static int parseInt(String text, RunInfo rInfo) {
		return Integer.parseInt(getValue(text, rInfo));
	}

	/**
	 * @param text
	 * @param rInfo
	 * @return parsed value
	 */
	public static String parseString(String text, RunInfo rInfo) {
		return getValue(text, rInfo);
	}

	/**
	 * @param text
	 * @param rInfo
	 * @return parsed value
	 */
	protected static String getValue(String text, RunInfo rInfo) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Parse expression: " + text);
		}
		// LOGGING ->

		String preText = text.substring(0, text.indexOf("@"));

		if (!text.contains(")")) {
			logger.error("Text to parse (" + text
					+ ") does not contain closing parenthesis!");
			throw new IllegalStateException("Text to parse (" + text
					+ ") does not contain closing parenthesis!");
		}

		int closing = getClosingParenthesisIndex(text.toCharArray(), text.indexOf("@") + 2);
		String text2parse = text.substring(text.indexOf("@") + 2, closing);
		String postText = text.substring(closing + 1, text.length());
		
		String parsed = null;

		if (text.contains("@@") && text.indexOf("@") == text.indexOf("@@")) {
			parsed = CsvBatchRunParser.parseLink(
					text.substring(text.indexOf("@") + 3, text.indexOf(")")), rInfo);
		} else {
			String[] textParsed = text2parse.split(",");

			if (textParsed.length < 2) {
				logger.error("Text to parse (" + text + ") does not contain ','!");
			}

			String filename = textParsed[0].trim();
			String secondFilename = null;

			if (filename.contains("~")) {
				secondFilename = filename.split("~")[1].trim();
				filename = filename.split("~")[0].trim();
			}

			// recursive parsing:
			filename = BatchRunParser.parseString(filename, rInfo);

			filename = rInfo.getCsvParamBasedirCorrection() + filename;

			String colName = textParsed[1].trim();

			Map<String, Map<Integer, String>> fileMap = readCsvFile(filename, rInfo);
			Integer run = rInfo.getCurrentRun();

			if (secondFilename != null) {
				// recursive parsing:
				secondFilename = BatchRunParser.parseString(secondFilename, rInfo);
				secondFilename = rInfo.getCsvParamBasedirCorrection() + secondFilename;

				Map<String, Map<Integer, String>> fileMapSec = readCsvFile(secondFilename, rInfo);
				checkCsvData(secondFilename, colName, fileMapSec, null);
				String idCol = firstColumns.get(secondFilename);

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("\tID (run): " + run);
					logger.debug("\t1st Colum: " + idCol);
					logger.debug("\tID (2nd): " + fileMap.get(idCol).get(run));
					logger.debug("\t2nd Colum: " + colName);
				}
				// LOGGING ->

				checkCsvData(filename, idCol, fileMap, run);

				String returnValue = preText
						+ fileMapSec.get(colName)
								.get(Integer.parseInt(fileMap.get(idCol).get(run)))
						+ postText;

				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("\tReturn value: " + returnValue);
				}
				// LOGGING ->

				ModelRunner.clog(colName, returnValue + " (" + textParsed[0].trim() + ")");
				return returnValue;
			} else {
				// <- LOGGING
				if (logger.isDebugEnabled()) {
					logger.debug("\tID (run): " + run);
					logger.debug("\t2nd Colum: " + colName);
				}
				// LOGGING ->

				checkCsvData(filename, colName, fileMap, run);

				parsed = fileMap.get(colName).get(run);

				ModelRunner.clog(colName, preText + parsed
						+ postText + " ("
						+ textParsed[0].trim() + ")");
			}
		}
		String returnValue = preText + parsed
				+ postText;

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("\tReturn value: " + returnValue);
		}
		// LOGGING ->

		return returnValue;
	}

	static String parseLink(String text, RunInfo rInfo) {
		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("Parse Link: " + text);
		}
		// LOGGING ->

		String[] textParsed = text.split(";");
		String defaultPath = textParsed[0].trim();

		String id = null;
		if (textParsed.length > 1) {
			id = textParsed[1].trim();
		}

		Map<String, Map<String, String>> fileMap = readLinksFile(rInfo);

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("\tDefaultPath: " + defaultPath);
			logger.debug("\tID: " + id);
		}
		// LOGGING ->

		String value;
		if (checkLinkData(defaultPath, id)) {
			value = BatchRunParser.parseString(fileMap.get(defaultPath).get(id), rInfo);
		} else {
			value = BatchRunParser.parseString(defaultPath, rInfo);
		}

		ModelRunner.clog(defaultPath, value + " ("
					+ textParsed[0].trim() + ")");

		// <- LOGGING
		if (logger.isDebugEnabled()) {
			logger.debug("\tReturn value: " + value);
		}
		// LOGGING ->

		return value;
	}

	protected static Map<String, Map<Integer, String>> readCsvFile(String filename, RunInfo rInfo) {
		if (!cachedCsvData.containsKey(filename)) {
			Map<String, Map<Integer, String>> fileMap = new HashMap<String, Map<Integer, String>>();
			CsvReader reader;
			try {
				// TODO override persister method
				reader = rInfo.getPersister().getCSVReader(filename, null);
				firstColumns.put(filename, reader.getHeader(0));

				for (String col : reader.getHeaders()) {
					fileMap.put(col, new HashMap<Integer, String>());
				}

				while (reader.readRecord()) {
					int run = 0;
					try {
						run = Integer.parseInt(reader.get(0));
					} catch (NumberFormatException e) {
						logger.error("CSV parameter file >" + filename
								+ "< has not a first column parsable to integer");
						throw new IllegalStateException("CSV parameter file >" + filename
								+ "< has not a first column parsable to integer");
					}
					for (int i = 0; i < reader.getColumnCount(); i++) {
						fileMap.get(reader.getHeaders()[i]).put(new Integer(run), reader.get(i));
					}
				}
			} catch (FileNotFoundException exception) {
				throw new IllegalStateException("The file " + rInfo.getPersister().getFullPath(filename, null)
				        + " was not found!");
			} catch (IOException exception) {
				exception.printStackTrace();
			}

			cachedCsvData.put(filename, fileMap);
		}
		return cachedCsvData.get(filename);
	}

	protected static Map<String, Map<String, String>> readLinksFile(RunInfo rInfo) {
		String filename = rInfo.getLinksCsvBasedirCorrection() + LINKS_TABLE;
		if (cachedLinksData == null) {
			cachedLinksData = new HashMap<String, Map<String, String>>();
			CsvReader reader;
			try {
				reader = rInfo.getPersister().getCSVReader(filename, null);
				String defaultPath;
				String id;
				
				while (reader.readRecord()) {
					defaultPath = reader.get(LINKS_TABLE_DEFAULT);
					id = reader.get(LINKS_TABLE_ID);

					if (!cachedLinksData.containsKey(defaultPath)) {
						cachedLinksData.put(defaultPath, new HashMap<String, String>());
					}
					cachedLinksData.get(defaultPath).put(id == "" ? null : id,
							reader.get(LINKS_TABLE_VALUE));
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		return cachedLinksData;
	}

	/**
	 * @param filename
	 * @param colName
	 * @param fileMap
	 * @param run
	 */
	protected static void checkCsvData(String filename, String colName,
			Map<String, Map<Integer, String>> fileMap, Integer run) {
		if (!fileMap.containsKey(colName)) {
			logger.error("CSV parameter file >" + filename + "< does not contain column >"
					+ colName + "<!");
			throw new IllegalStateException("CSV parameter file >" + filename
					+ "< does not contain column >" +
					colName + "<!");
		} else if (run != null && !fileMap.get(colName).containsKey(run)) {
			logger.error("CSV parameter file >" + filename + "< does not contain run >" + run
					+ "<!");
			throw new IllegalStateException("CSV parameter file >" + filename
					+ "< does not contain run >" +
					run + "<!");
		}
	}

	/**
	 * @param defaultPath
	 * @param id
	 */
	protected static boolean checkLinkData(String defaultPath, String id) {
		if (!cachedLinksData.containsKey(defaultPath)) {
			logger.info("CSV Link file does not contain key >" + defaultPath + "<. Using default!");
			return false;
		} else if (!cachedLinksData.get(defaultPath).containsKey(id)) {
			logger.info("CSV Link file does not contain id >" +
					id + "< for defaultPath >" + defaultPath + "<. Using default!");
			return false;
		} else
			return true;
	}

	/**
	 * Empties cached CSV data
	 */
	protected static void reset() {
		// <- LOGGING
		logger.info("Reset CsvBatchRunParser.");
		// LOGGING ->
		cachedCsvData = new HashMap<String, Map<String, Map<Integer, String>>>();
		firstColumns = new HashMap<String, String>();
		cachedLinksData = null;
	}

	/**
	 * @param text
	 * @param openPos
	 * @return index of closing parenthesis
	 */
	protected static int getClosingParenthesisIndex(char[] text, int openPos) {
		int closePos = openPos;
		int counter = 1;
		while (counter > 0) {
			char c = text[++closePos];
			if (c == '(') {
				counter++;
			}
			else if (c == ')') {
				counter--;
			}
		}
		return closePos;
	}
}
