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

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.labelformatters.LabelFormatterNumber;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;

import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.data.Service;
import org.volante.abm.example.CurveCompetitivenessModel;
import org.volante.abm.models.CompetitivenessModel.CompetitivenessDisplay;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.curve.Curve;

public class CurveCompetitivenessDisplay extends AbstractDisplay implements CompetitivenessDisplay
{
	private static final long	serialVersionUID	= 1331487655171517592L;

	double width = 2;
	int numPoints = 100;
	CurveCompetitivenessModel	model				= null;
	Map<Service, Chart2D> charts = new HashMap<Service, Chart2D>();
	Box chartBox = new Box(BoxLayout.Y_AXIS);
	ModelData					data				= null;
	private Regions				region				= null;
	
	
	
 
	public CurveCompetitivenessDisplay( CurveCompetitivenessModel model ) 
	{
		this.model = model;
		add( chartBox );
	}

	public Chart2D getNewChart( Service c )
	{
		Chart2D chart = new Chart2D();
		chart.getAxisX().getAxisTitle().setTitle( null );
		chart.getAxisY().getAxisTitle().setTitle( null );
		IAxisLabelFormatter format = new LabelFormatterNumber( new DecimalFormat( "0.0E0"  ) );
		chart.getAxisX().setFormatter( format );
		chart.getAxisY().setFormatter( format );
		ITrace2D trace = new Trace2DSimple(c.getName() + " Utility for Residual Demand");
		trace.setColor( Color.red );
		trace.setStroke( new BasicStroke( 3.0f ) );
		chart.addTrace( trace );
		
		ITrace2D trace2 = new Trace2DSimple("Current");
		trace2.setColor( Color.green );
		trace2.setStroke( new BasicStroke( 3.0f ) );
		chart.addTrace( trace2 );
		
		chart.setPreferredSize( new Dimension(300,150) );
		chart.setUseAntialiasing( true );
		return chart;
	}
	
	public void updateChart( Curve c, Chart2D chart, Service s )
	{
		double current = region.getAllRegions().iterator().next().getDemandModel().getResidualDemand().get( s )/region.getNumCells();
		ITrace2D trace = chart.getTraces().first();
		trace.removeAllPoints();
		double cWidth = max( width, abs( current )/10 );
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double min = current - cWidth;
		double max = current + cWidth;
		for( double i = min; i < max; i+=abs( (max-min)/numPoints ) )
		{
			double val = c.sample( i );
			trace.addPoint( i, val );
			minY = min( minY, val );
			maxY = max( maxY, val );
		}
		ITrace2D trace2 = chart.getTraces().last();
		trace2.removeAllPoints();
		trace2.addPoint( current, minY );
		trace2.addPoint( current, maxY );
	}

	@Override
	public void update()
	{
		Map<Service, Curve> curves = model.getCurves();
		for( Service s : curves.keySet() )
		{
			if( ! charts.containsKey( s ))
			{
				charts.put( s, getNewChart( s ) );
				chartBox.add( charts.get(s));
				chartBox.invalidate();
			}
			updateChart( curves.get( s ), charts.get( s ), s );
		}
	}

	@Override
	public void initialise( ModelData data, RunInfo info, Regions region ) throws Exception
	{
		this.data = data;
		this.info = info;
		this.region = region;
	}
}
