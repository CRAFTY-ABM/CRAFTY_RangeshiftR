/**
 * 
 */
package org.volante.abm.comi.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.LandUseAgent;
import org.volante.abm.agent.SocialAgent;
import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.comi.decision.pa.ComiOfAdoptionPa;
import org.volante.abm.data.Region;
import org.volante.abm.data.SocialRegionHelper;
import org.volante.abm.param.RandomPa;

import com.moseph.modelutils.Utilities;

import de.cesr.more.basic.edge.MoreEdge;


/**
 * @author Sascha Holzhauer
 *
 */
public class SocialInfluenceRegionHelper implements SocialRegionHelper {

	/**
     * Logger
     */
    static private Logger logger = Logger.getLogger(SocialInfluenceRegionHelper.class);
    
	@Element(required = false)
	protected boolean perAgent = false;

	@Element(required = false)
	protected int numRounds = 1;
	
	/**
	 * Social influence according to Kaufmann et al., Simulating the diffusion of organic farming practices in two New
	 * EU Member States, 2009
	 * 
	 * @see org.volante.abm.data.SocialRegionHelper#socialNetworkPerceived()
	 */
	@Override
	public void socialNetworkPerceived(Region region) {
	
		for (int i = 0; i < this.numRounds; i++) {

			if (perAgent) {
				for (LandUseAgent agent : region.getAgents()) {
					if (agent instanceof SocialAgent) {
						ComiOfAdoptionPa pa = (ComiOfAdoptionPa) ((LaraBehaviouralComponent) agent.getBC()).getLaraComp().getBOMemory()
				                .recall(ComiOfAdoptionPa.KEY);
						
						List<SocialAgent> neighbours = new ArrayList<>();
						for (SocialAgent neighbour : region.getNetwork().getPredecessors((SocialAgent) agent)) {
							neighbours.add(neighbour);
						}
						Utilities.shuffle(neighbours, region.getRandom().getURService(),
						        RandomPa.RANDOM_SEED_INIT_NETWORK.name());

						for (SocialAgent neighbour : neighbours) {
							pa.socialInfluence(neighbour);
						}
					} else {
						// <- LOGGING
						logger.warn("Agent " + agent.getID() + " is not of type SocialAgent!");
						// LOGGING ->
					}
				}
			} else {
				List<MoreEdge<SocialAgent>> edges = new ArrayList<>();
				edges.addAll(region.getNetwork().getEdgesCollection());
				Utilities.shuffle(edges, region.getRandom().getURService(), RandomPa.RANDOM_SEED_INIT_NETWORK.name());

				for (MoreEdge<SocialAgent> edge : edges) {

					ComiOfAdoptionPa pa =
					        (ComiOfAdoptionPa) ((LaraBehaviouralComponent) edge.getEnd().getBC()).getLaraComp()
					                .getBOMemory().recall(ComiOfAdoptionPa.KEY);
					pa.socialInfluence(edge.getStart());
					
				}
			}
		}
	}
}
