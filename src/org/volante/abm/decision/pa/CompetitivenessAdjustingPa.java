/**
 * 
 */
package org.volante.abm.decision.pa;


import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Service;

import com.moseph.modelutils.fastdata.UnmodifiableNumberMap;


/**
 * @author Sascha Holzhauer
 * 
 */
public interface CompetitivenessAdjustingPa extends CraftyPaFeatures {
	/**
	 * When given an agent, a cell and the level of (potential) provision, adjusts the competitiveness level.
	 * 
	 * Must be able to deal with the agent being null if the cell is unoccupied.
	 * 
	 * @param agent
	 *        the agent the competitiveness is calculated for
	 * @param location
	 *        the cell
	 * @param provision
	 *        (potential) service provision of the given agent on the given cell
	 * @param competitiveness
	 *        unadjusted competitiveness
	 * @return adjusted competitiveness
	 */
	public double adjustCompetitiveness(
	        FunctionalRole agent, Cell location, UnmodifiableNumberMap<Service> provision, double competitiveness);
}
