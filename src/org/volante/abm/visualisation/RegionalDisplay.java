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
package org.volante.abm.visualisation;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;

public class RegionalDisplay extends AbstractDisplay
{
	private static final long	serialVersionUID	= -4302068451677804813L;

	Region						current				= null;
	Regions						regions				= null;
	
	ModelData					data				= null;
	RunInfo						info				= null;
	
	DoubleMapDisplay supply = new DoubleMapTextDisplay();
	DoubleMapDisplay demand = new DoubleMapTextDisplay();
	DoubleMapDisplay residual = new DoubleMapTextDisplay();


	@Override
	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.regions = region;
		current = regions.getAllRegions().iterator().next();
		setupDisplay();
	}
	
	@Override
	public void update() { setRegion( current ); }
	
	public void setRegion( Region r )
	{
		this.current = r;
		residual.setMap( r.getDemandModel().getResidualDemand().toMap() );
		demand.setMap( r.getDemandModel().getDemand().toMap() );
		supply.setMap( r.getDemandModel().getSupply().toMap() );
	}
	
	public void setupDisplay()
	{
		setLayout( new BoxLayout( this, BoxLayout.Y_AXIS  ));
		JComponent aggregate = new Box( BoxLayout.X_AXIS );
		aggregate.setPreferredSize( new Dimension(750,200) );
		aggregate.setMinimumSize( new Dimension(750,200) );
		add( aggregate );
		
		JComponent sDist = supply.getDisplay();
		sDist.setBorder( new TitledBorder( "Supply" ));
		aggregate.add( sDist );
		
		JComponent dDist = demand.getDisplay();
		dDist.setBorder( new TitledBorder( "Demand" ));
		aggregate.add( dDist );
		
		JComponent rDist = residual.getDisplay();
		rDist.setBorder( new TitledBorder( "Residual" ));
		aggregate.add( rDist );
		
		aggregate.setBorder( new TitledBorder( "Overall Supply and Demand" ) );
		aggregate.invalidate();
		setBorder(new TitledBorder( "Main" ));
	}
	
	@Override
	public JComponent getMainPanel()
	{
		JScrollPane pane = new JScrollPane( this );
		return pane;
	}

}
