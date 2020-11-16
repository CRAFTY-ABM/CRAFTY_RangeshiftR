/**
 * 
 */
package org.volante.abm.institutions;


import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.Agent;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.ABMPersister;

import com.google.common.collect.Table;


/**
 * @author Sascha Holzhauer
 *
 */
public class FrRestrictingInsitution extends AbstractInstitution {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(FrRestrictingInsitution.class);

	/**
	 * CSV file matrix of functional role serial IDs as column and row names. If the entry is > 0 a transition from the
	 * row FR to the column FR is interpreted as restricted.
	 */
	@Element(required = true)
	protected String csvFileRestrictedAllocations = "";

	@Element(required = false)
	protected String labelUnmanaged = "UNMANAGED";

	protected Table<String, String, Double> restrictedRoles;

	protected Set<FunctionalRole> frs = null;

	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);
		try {
			restrictedRoles = ABMPersister.getInstance().csvToDoubleTable(csvFileRestrictedAllocations, "FR", null);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}


	/**
	 * Checks configured restriction CSV file.
	 * 
	 * @param fr
	 * @param cell
	 * @return true if the given {@link FunctionalRole} is allowed to occupy the given cell.
	 */
	public boolean isAllowed(FunctionalRole fr, Cell cell) {
		String label2request =
		        (cell.getOwner().getFC().getFR().getLabel().equals(Agent.NOT_MANAGED_FR_ID) ? this.labelUnmanaged
		                : cell.getOwner().getFC().getFR().getLabel());
		if (!restrictedRoles.contains(label2request, fr.getLabel())) {
			// <- LOGGING
			logger.warn("Allowed Types Map does not contain an entry for " + label2request
			        + " > " + fr.getLabel() + "! Assuming 0.");
			// LOGGING ->
			return true;
		} else {
			return restrictedRoles.get(label2request, fr.getLabel()) <= 0;
		}
	}
}
