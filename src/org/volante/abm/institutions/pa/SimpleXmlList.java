/**
 * 
 */
package org.volante.abm.institutions.pa;


import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;


/**
 * @author Sascha Holzhauer
 * 
 */
public class SimpleXmlList extends ArrayList<String> {

	@ElementList(name = "list", entry = "listentry", inline = true, type = String.class)
	protected List<String> entries;

	public SimpleXmlList(@ElementList(name = "list", entry = "listentry", inline = true) List<String> list) {
		super(list);
	}
}
