/**
 * 
 */
package org.volante.abm.example.socialInteraction;

import java.util.Arrays;

import org.volante.abm.agent.Agent;
import org.volante.abm.agent.NormalisableProductivityAgent;
import org.volante.abm.data.Region;
import org.volante.abm.data.Service;
import org.volante.abm.data.SocialRegionHelper;


/**
 * Normalisation can be switched off to make the helper usable for reporting purposes.
 * 
 * @author Sascha Holzhauer
 * 
 */
public class NormaliseProductivityRegionHelper implements SocialRegionHelper {

	protected Region region = null;
	protected boolean normalise = true;
	
	protected Service	service		= null;

	public NormaliseProductivityRegionHelper(Region region, Service service,
			boolean normalise) {
		this.region = region;
		this.service = service;
		this.normalise = normalise;

		changes = new double[region.getFunctionalRoles().size()];
	}

	public NormaliseProductivityRegionHelper(Region region, Service service) {
		this(region, service, true);
	}

	protected double[] changes;
	protected double[] lastChanges;

	public double[] getLastProductivityChange() {
		return lastChanges;
	}

	public void socialNetworkPerceived(Region region) {
		this.normaliseProductivity();
	}

	public void setChange(int aft, double change) {
		changes[aft] += change;
	}

	/**
	 * TODO test
	 */
	public void normaliseProductivity() {
		// calculate AFT numbers
		int[] aftNumbers = new int[region.getFunctionalRoles().size()];
		for (Agent a : region.getAgents()) {
			aftNumbers[a.getFC().getFR().getSerialID()]++;
		}

		// calculate shares
		for (int i = 0; i < aftNumbers.length; i++) {
			changes[i] = changes[i] / aftNumbers[i];
		}

		// set shares
		if (this.normalise) {
			for (Agent a : region.getAgents()) {
				if (a instanceof NormalisableProductivityAgent) {
					((NormalisableProductivityAgent) a)
.normaliseProductivity(
							this.service, changes[a.getFC().getFR()
									.getSerialID()]);
				}
			}
		}

		lastChanges = Arrays.copyOf(changes, changes.length);

		// reset
		for (int i = 0; i < aftNumbers.length; i++) {
			aftNumbers[i] = 0;
			changes[i] = 0.0;
		}
	}

	public void setNormalise(boolean normalise) {
		this.normalise = normalise;
	}
}
