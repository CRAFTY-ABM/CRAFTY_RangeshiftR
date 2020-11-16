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
 * 
 * Created by Sascha Holzhauer on 3 Dec 2014
 */
package org.volante.abm.institutions;


import org.volante.abm.agent.Agent;
import org.volante.abm.agent.bt.InnovativeBC;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.institutions.innovation.Innovation;
import org.volante.abm.institutions.innovation.RepeatingInnovation;
import org.volante.abm.institutions.innovation.repeat.InnovationRepComp;
import org.volante.abm.schedule.PreTickAction;
import org.volante.abm.schedule.RunInfo;


/**
 * Spreads the innovation at <code>innovationReleaseTick</code> and in renewal
 * interval which is defined in the innovation's {@link InnovationRepComp}.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class RepeatingInnovativeInstitution extends InnovativeInstitution implements PreTickAction {

	/**
	 * @see org.volante.abm.institutions.InnovativeInstitution#initialise(org.volante.abm.data.ModelData,
	 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
	 */
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);
		info.getSchedule().register(this);
	}

	/**
	 * @see org.volante.abm.schedule.PreTickAction#preTick()
	 */
	@Override
	public void preTick() {
		if ((rInfo.getSchedule().getCurrentTick() - this.innovationReleaseTick) > 0
				&& (rInfo.getSchedule().getCurrentTick() - this.innovationReleaseTick)
						% ((RepeatingInnovation) this.innovation)
								.getRepetitionComp().getRenewalInterval() == 0) {
			for (Agent agent : this.region.getAgents()) {
				if (agent instanceof InnovativeBC) {
					this.innovation.outdate((InnovativeBC) agent);
				}
			}
			if (this.innovation instanceof RepeatingInnovation) {
				this.innovation = (Innovation) ((RepeatingInnovation) this.innovation)
						.getNewInnovation();
				spreadInnovation();
			}
		}
	}
}