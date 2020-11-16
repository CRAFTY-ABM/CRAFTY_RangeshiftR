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
 */
package org.volante.abm.serialization;


import javax.swing.JFileChooser;

import org.apache.log4j.Logger;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.volante.abm.schedule.FinishAction;
import org.volante.abm.schedule.RunInfo;

import de.cesr.more.measures.util.MRService;


/**
 * Service class for R calculations
 * 
 * - schedules at initialisation stopping R.Engine at the end of simulation
 * 
 * @author Sascha Holzhauer
 */
public class RService implements RMainLoopCallbacks {

	/**
	 * Logger
	 */
	static private Logger	logger		= Logger.getLogger(RService.class);

	static private String[]		R_ARGS			= { "--no-save" };

	static private RService	instance	= null;

	/**
	 * Constructor
	 */
	private RService(RunInfo rInfo) {
		// Stop R engine:
		rInfo.getSchedule().register(new FinishAction() {
			@Override
			public void afterLastTick() {
				RService.endEngine();
			}
		});
	}

	/**
	 * Returns the the current {@link MRService} if existing and creates a new instance otherwise.
	 * 
	 * @return instance of MRService
	 */
	static public RService getInstance(RunInfo rInfo) {
		if (instance == null) {
			instance = new RService(rInfo);
		}
		return instance;
	}

	/**
	 * Stops REngine. Scheduled at initialisation for end of simulation.
	 * 
	 */
	static public void endEngine() {
		logger.info("End REngine...");
		Rengine re = Rengine.getMainEngine();
		if (re != null) {
			re.end();
		}
		instance = null;
	}

	/**
	 * Create a new REngine
	 * 
	 * @return the new REngine
	 */
	public static Rengine getRengine(RunInfo rInfo) {
		Rengine re = Rengine.getMainEngine();
		if (re == null) {
			logger.debug("REngine-Version: " + Rengine.getVersion());
			re = new Rengine(R_ARGS, false, getInstance(rInfo));

			// the engine creates R in a new thread, so we should wait until it's ready
			if (!re.waitForR()) {
				logger.error("Cannot load R");
				throw new IllegalStateException("Cannot load R");
			}
		}
		logger.debug("Returning Rengine");
		return re;
	}

	/**
	 * @see org.rosuda.JRI.RMainLoopCallbacks#rBusy(org.rosuda.JRI.Rengine, int)
	 */
	@Override
	public void rBusy(Rengine arg0, int which) {
		if (which == 1) {
			logger.info("R Engine works ...");
		}
		if (which == 0) {
			logger.info("... finished.");
		}
		assert false;
	}

	/**
	 * @see org.rosuda.JRI.RMainLoopCallbacks#rChooseFile(org.rosuda.JRI.Rengine, int)
	 */
	@Override
	public String rChooseFile(Rengine arg0, int arg1) {
		JFileChooser fileChooser = new JFileChooser();
		return fileChooser.getSelectedFile().getName();
	}

	/**
	 * @see org.rosuda.JRI.RMainLoopCallbacks#rFlushConsole(org.rosuda.JRI.Rengine)
	 */
	@Override
	public void rFlushConsole(Rengine arg0) {
		logger.warn("Flushed");
	}

	/**
	 * @see org.rosuda.JRI.RMainLoopCallbacks#rLoadHistory(org.rosuda.JRI.Rengine, java.lang.String)
	 */
	@Override
	public void rLoadHistory(Rengine arg0, String arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.rosuda.JRI.RMainLoopCallbacks#rReadConsole(org.rosuda.JRI.Rengine, java.lang.String,
	 *      int)
	 */
	@Override
	public String rReadConsole(Rengine arg0, String arg1, int arg2) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.rosuda.JRI.RMainLoopCallbacks#rSaveHistory(org.rosuda.JRI.Rengine, java.lang.String)
	 */
	@Override
	public void rSaveHistory(Rengine arg0, String arg1) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see org.rosuda.JRI.RMainLoopCallbacks#rShowMessage(org.rosuda.JRI.Rengine, java.lang.String)
	 */
	@Override
	public void rShowMessage(Rengine arg0, String message) {
		logger.warn(message);
	}

	/**
	 * @see org.rosuda.JRI.RMainLoopCallbacks#rWriteConsole(org.rosuda.JRI.Rengine,
	 *      java.lang.String, int)
	 */
	@Override
	public void rWriteConsole(Rengine arg0, String message, int level) {
		if (level == 0) {
			logger.info(message);
		} else if (level == 1) {
			logger.warn(message);
		}
	}
}
