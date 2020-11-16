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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;

import com.csvreader.CsvReader;
import com.moseph.modelutils.fastdata.Indexed;
import com.moseph.modelutils.fastdata.Named;
import com.moseph.modelutils.fastdata.NamedArrayIndexSet;
import com.moseph.modelutils.fastdata.NamedIndexSet;


public abstract class NamedIndexLoader<S extends Named & Indexed> implements DataTypeLoader<S> {
	@Attribute(name = "file")
	String	file		= "";
	@Attribute(name = "indexed", required = false)
	boolean	indexed		= true;
	@Attribute(name = "nameColumn", required = false)
	String	nameColumn	= "Name";
	@Attribute(name = "indexColumn", required = false)
	String	indexColumn	= "Index";

	@Override
	public NamedIndexSet<S> getDataTypes(ABMPersister persister) throws IOException {
		// TODO override persister method
		CsvReader reader = persister.getCSVReader(file, null);
		int index = 0;
		List<S> datatypes = new ArrayList<S>();
		while (reader.readRecord()) {
			int ind = indexed ? Integer.parseInt(reader.get(indexColumn)) : index;
			index++;
			datatypes.add(getType(reader.get(nameColumn), ind));
		}
		return new NamedArrayIndexSet<S>(datatypes);
	}

	abstract S getType(String name, int index);
}
