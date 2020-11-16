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
 * Created by Sascha Holzhauer on 18 Mar 2015
 */
package org.volante.abm.agent.fr;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.FunctionalRoleProductionObserver;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;
import org.volante.abm.models.ProductionModel;
import org.volante.abm.serialization.Initialisable;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;

/**
 * @author Sascha Holzhauer
 *
 */
public interface FunctionalRole extends Initialisable {

	public static final int UNKNOWN_SERIAL = -1;

	public Agent assignNewFunctionalComp(Agent agent);

	public FunctionalComponent getNewFunctionalComp();

	public String getLabel();

	public int getSerialID();

	public String getDescription();

	public ProductionModel getProduction();

	public boolean isInitialised();

	/**
	 * Get the amount of services this {@link FunctionalRole} is expected to
	 * supply, based on the (average) productivity.
	 * 
	 * @param cell
	 * @return number map of services
	 */
	public UnmodifiableNumberMap<Service> getExpectedSupply(Cell cell);

	public double getAllocationProbability();

	public double getMeanGivingUpThreshold();

	public double getMeanGivingInThreshold();

	public double getSampledGivingUpThreshold();

	public double getSampledGivingInThreshold();

	public void registerFunctionalRoleProductionObserver(FunctionalRoleProductionObserver observer);

	public void removeFunctionalRoleProductionObserver(FunctionalRoleProductionObserver observer);
}
