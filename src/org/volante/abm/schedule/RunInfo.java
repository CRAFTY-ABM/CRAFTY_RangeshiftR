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
package org.volante.abm.schedule;


import org.volante.abm.output.Outputs;
import org.volante.abm.param.ParameterRepository;
import org.volante.abm.serialization.ABMPersister;


public class RunInfo {
	ABMPersister	persister		= ABMPersister.getInstance();

	{
		this.persister.setRunInfo(this);
	}

	Schedule		schedule		= new DefaultSchedule();
	Outputs			outputs			= new Outputs();
	String			scenario		= "";
	String			runID			= "";

	int				numRuns				= 0;
	int				currentRun			= 0;

	int				numRandomVariations	= 0;
	long			currentRandomSeed	= 0;

	String			csvParamBasedirCorrection	= "";
	String linksCsvBasedirCorrection = "";


	ParameterRepository	paramRepos			= new ParameterRepository();

	public RunInfo() {
	}

	public ABMPersister getPersister() {
		return persister;
	}

	public void setPersister(ABMPersister persister) {
		this.persister = persister;
		this.persister.setRunInfo(this);
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public Outputs getOutputs() {
		return outputs;
	}

	public void setOutputs(Outputs outputs) {
		this.outputs = outputs;
	}

	public String getScenario() {
		return scenario;
	}

	public void setScenario(String scenario) {
		this.scenario = scenario;
		persister.setContext("s", scenario);
	}

	public String getRunID() {
		return runID;
	}

	public void setRunID(String runID) {
		persister.setContext("i", runID);
		this.runID = runID;
	}

	/**
	 * @return the numRuns
	 */
	public int getNumRuns() {
		return numRuns;
	}

	/**
	 * @param numRuns
	 *        the numRuns to set
	 */
	public void setNumRuns(int numRuns) {
		this.numRuns = numRuns;
	}

	/**
	 * @return the currentRun
	 */
	public int getCurrentRun() {
		return currentRun;
	}

	/**
	 * @param currentRun
	 *        the currentRun to set (starting by 0)
	 */
	public void setCurrentRun(int currentRun) {
		this.currentRun = currentRun;
	}

	/**
	 * @return the numRandomVariations
	 */
	public int getNumRandomVariations() {
		return numRandomVariations;
	}

	/**
	 * @param numRandomVariations
	 *        the numRandomVariations to set
	 */
	public void setNumRandomVariations(int numRandomVariations) {
		this.numRandomVariations = numRandomVariations;
	}

	/**
	 * @return the currentRandomSeed
	 */
	public long getCurrentRandomSeed() {
		return currentRandomSeed;
	}

	/**
	 * @param currentRandomSeed
	 *        the currentRandomSeed to set
	 */
	public void setCurrentRandomSeed(long currentRandomSeed) {
		this.currentRandomSeed = currentRandomSeed;
	}

	/**
	 * @return the paramRepos
	 */
	public ParameterRepository getParamRepos() {
		return paramRepos;
	}

	public String getCsvParamBasedirCorrection() {
		return csvParamBasedirCorrection;
	}

	public void setCsvParamBasedirCorrection(String csvParamBasedirCorrection) {
		this.csvParamBasedirCorrection = csvParamBasedirCorrection;
	}

	public String getLinksCsvBasedirCorrection() {
		return linksCsvBasedirCorrection;
	}

	public void setLinksCsvBasedirCorrection(String linksCsvBasedirCorrection) {
		this.linksCsvBasedirCorrection = linksCsvBasedirCorrection;
	}
}
