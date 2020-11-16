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
 * Created by Sascha Holzhauer on 15 Nov 2015
 */
package org.volante.abm.output;

import java.awt.Color;

import org.simpleframework.xml.Attribute;

/**
 * @author Sascha Holzhauer
 *
 */
public class SimpleColour extends Color {

	@Attribute(required = true)
	protected float red;

	@Attribute(required = true)
	protected float green;

	@Attribute(required = true)
	protected float blue;

	@Attribute(required = false)
	protected float alpha;

	/**
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public SimpleColour(@Attribute(name = "red") float r, @Attribute(name = "green") float g,
			@Attribute(name = "blue") float b, @Attribute(name = "alpha") float a) {
		super(r, g, b, a);
	}

	/**
	 * @param r
	 * @param g
	 * @param b
	 * @param a
	 */
	public SimpleColour(@Attribute(name = "red") float r, @Attribute(name = "green") float g,
			@Attribute(name = "blue") float b) {
		super(r, g, b, 1.0f);
	}
}
