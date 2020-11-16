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
 * Created by Sascha Holzhauer on 14 May 2016
 */
package org.volante.abm.output.tablecolumns;


import org.volante.abm.agent.property.PropertyId;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.decision.pa.CraftyPa;
import org.volante.abm.decision.pa.PropertyProvidingPa;
import org.volante.abm.output.ActionCSVOutputter.TableEntry;
import org.volante.abm.output.TableColumn;
import org.volante.abm.schedule.RunInfo;

/**
 * @author Sascha Holzhauer
 *
 */
public class RestrictionNumber implements TableColumn<TableEntry> {

	public enum RestrictionNumberProperties implements PropertyId {
		RESTRICTION_NUMBER;
	}

	/**
	 * @see org.volante.abm.output.TableColumn#getHeader()
	 */
	@Override
	public String getHeader() {
		return "RestrictionsNumber";
	}

	/**
	 * @see org.volante.abm.output.TableColumn#getValue(java.lang.Object, org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Regions)
	 */
	@Override
	public String getValue(TableEntry t, ModelData data, RunInfo info, Regions r) {
		CraftyPa<?> pa = t.getPa();
		if (pa instanceof PropertyProvidingPa
				&& ((PropertyProvidingPa) pa).getProperties().containsKey(
						RestrictionNumberProperties.RESTRICTION_NUMBER)) {
			return ((Integer) ((PropertyProvidingPa) pa).getProperties().get(
					RestrictionNumberProperties.RESTRICTION_NUMBER)).toString();
		} else {
			return "NA";
		}
	}

}
