/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2016 School of GeoScience, University of Edinburgh, Edinburgh, UK
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
 * Created by Sascha Holzhauer on 18 Jul 2016
 */
package org.volante.abm.institutions.global;


import org.simpleframework.xml.Attribute;


/**
 * @author Sascha Holzhauer
 *
 */
public interface TickChecker {

	public static class DefaultTickChecker implements TickChecker {
		/**
		 * @see org.volante.abm.institutions.global.TickChecker#check(int)
		 */
	    @Override
	    public boolean check(int tick) {
	        return true;
	    }
	}

	public static class EvenTickChecker implements TickChecker {
		/**
		 * @see org.volante.abm.institutions.global.TickChecker#check(int)
		 */
	    @Override
	    public boolean check(int tick) {
	        return (tick) % 2 == 0;
	    }
	}

	public static class OddTickChecker implements TickChecker {
		/**
		 * @see org.volante.abm.institutions.global.TickChecker#check(int)
		 */
	    @Override
	    public boolean check(int tick) {
			// <- LOGGING
			GlobalSubsidisingInstitution.logger.info("OddTickChecker: check");
			// LOGGING ->
	        return (tick + 1) % 2 == 0;
	    }
	}

	public static class IntervalTickChecker implements TickChecker {

		@Attribute
		protected int interval = 1;

		/**
		 * The offset is added to the current tick before the modulo function is applied with the <code>interval</code>
		 * value
		 */
		@Attribute(required = false)
		protected int offset = 0;

		/**
		 * @see org.volante.abm.institutions.global.TickChecker#check(int)
		 */
		@Override
        public boolean check(int tick) {
			return (tick + this.offset) % this.interval == 0;
        }
	}

	public boolean check(int tick);
}
