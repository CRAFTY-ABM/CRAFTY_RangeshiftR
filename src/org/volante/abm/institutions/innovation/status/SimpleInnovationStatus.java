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
 * Created by Sascha Holzhauer on 06.03.2014
 */
package org.volante.abm.institutions.innovation.status;

/**
 * @author Sascha Holzhauer
 *
 */
public class SimpleInnovationStatus implements InnovationStatus {

	protected InnovationState	state					= InnovationStates.UNAWARE;

	boolean						networkChanged			= true;

	boolean neighbourShareChanged = true;

	double	neighbourShare	= Double.NaN;

	/**
	 * @return the adoptedNeighbourShare
	 */
	@Override
	public double getNeighbourShare() {
		return neighbourShare;
	}

	/**
	 * Status neighbourShareChanged becomes only true when the neighbourShare
	 * value changes (assumed that it does not matter to the agent if agents
	 * adopt when at the same time the same number of agents rejects.
	 * 
	 * @param adoptedNeighbourShare
	 *            the adoptedNeighbourShare to set
	 */
	@Override
	public void setNeighbourShare(double adoptedNeighbourShare) {
		if (this.neighbourShare != adoptedNeighbourShare) {
			this.neighbourShareChanged = true;
		}
		this.neighbourShare = adoptedNeighbourShare;
	}


	public boolean hasNetworkChanged() {
		return networkChanged;
	}

	public void setNetworkChanged(boolean networkChanged) {
		this.networkChanged = networkChanged;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.status.InnovationStatus#getState()
	 */
	@Override
	public InnovationState getState() {
		return this.state;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.status.InnovationStatus#aware()
	 */
	@Override
	public void aware() {
		this.state = InnovationStates.AWARE;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.status.InnovationStatus#trial()
	 */
	@Override
	public void trial() {
		this.state = InnovationStates.TRIAL;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.status.InnovationStatus#adopt()
	 */
	@Override
	public void adopt() {
		this.state = InnovationStates.ADOPTED;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.status.InnovationStatus#reject()
	 */
	@Override
	public void reject() {
		this.state = InnovationStates.REJECTED;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.status.InnovationStatus#hasNeighbourShareChanged()
	 */
	@Override
	public boolean hasNeighbourShareChanged() {
		return this.neighbourShareChanged;
	}

	/**
	 * @see org.volante.abm.institutions.innovation.status.InnovationStatus#resetNeighbourShareChanged()
	 */
	@Override
	public void resetNeighbourShareChanged() {
		this.neighbourShareChanged = false;
	}
}
