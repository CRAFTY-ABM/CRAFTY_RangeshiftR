/**
 * 
 */
package org.volante.abm.schedule;

import org.volante.abm.models.WorldSynchronisationModel;

/**
 * @author Sascha Holzhauer
 *
 */
public interface WorldSyncSchedule extends Schedule {

	public void setWorldSyncModel(WorldSynchronisationModel worldSyncModel);

	public WorldSynchronisationModel getWorldSyncModel();
}
