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
package org.volante.abm.output;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.visualisation.AbstractDisplay;
import org.volante.abm.visualisation.Display;

public class DisplayVideoWriter extends AbstractVideoWriter 
{
	@Attribute(required=false)
	boolean includeSurroundings = true;
	
	@Element
	Display		display				= null;
	JComponent	toPaint				= null;
	
	@Element(required = false)
	Color bgColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	
	@Override
	public BufferedImage getImage( Regions r )
	{
		display.update();
		return ComponentImageCreator.createImage( toPaint );
	}
	

	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception
	{
		super.initialise(data, info);
		
		display.initialise(data, info, data.getRootRegionSet());

		if( output == null || output.equals("") ) {
			output = display.getTitle().replaceAll( "\\s", "" );
		}
		//Either just get the main panel, or get the whole display
		if( ! includeSurroundings && display instanceof AbstractDisplay) {
			toPaint = ((AbstractDisplay)display).getMainPanel();
		} else {
			toPaint = display.getDisplay();
		}
		toPaint.setPreferredSize( new Dimension( width, height ) );
		toPaint.setSize( new Dimension( width, height ) );

		toPaint.setBackground(bgColor);
		toPaint.setOpaque(false);
	}
}
