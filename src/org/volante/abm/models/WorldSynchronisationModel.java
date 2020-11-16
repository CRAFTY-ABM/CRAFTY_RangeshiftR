/**
 * 
 */
package org.volante.abm.models;


import org.volante.abm.data.ModelData;
import org.volante.abm.data.RegionSet;
import org.volante.abm.data.Service;
import org.volante.abm.schedule.RunInfo;

import com.moseph.modelutils.fastdata.DoubleMap;


/**
 * @author Sascha Holzhauer
 *
 */
public interface WorldSynchronisationModel {

	public void initialise(ModelData data, RunInfo info);

	public void synchronizeNumOfCells(RegionSet regions);

	public void synchronizeDemand(RegionSet regions);

	public void synchronizeSupply(RegionSet regions);

	public DoubleMap<Service> getWorldDemand();

	public DoubleMap<Service> getWorldSupply();
}
