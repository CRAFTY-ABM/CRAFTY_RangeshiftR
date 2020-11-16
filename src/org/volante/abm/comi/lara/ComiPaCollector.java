/**
 * 
 */
package org.volante.abm.comi.lara;


import java.util.ArrayList;
import java.util.Collection;

import org.volante.abm.agent.bt.LaraBehaviouralComponent;
import org.volante.abm.comi.fr.ComiVariantProductionFR;
import org.volante.abm.decision.pa.CraftyPa;

import de.cesr.lara.components.eventbus.events.LaraEvent;
import de.cesr.lara.components.preprocessor.LaraBOCollector;
import de.cesr.lara.components.preprocessor.event.LPpBoCollectorEvent;
import de.cesr.lara.components.preprocessor.impl.LAbstractPpComp;

/**
 * @author Sascha Holzhauer
 *
 */
public class ComiPaCollector extends LAbstractPpComp<LaraBehaviouralComponent, CraftyPa<?>> implements
        LaraBOCollector<LaraBehaviouralComponent, CraftyPa<?>> {

	/**
	 * @see de.cesr.lara.components.eventbus.LaraInternalEventSubscriber#onInternalEvent(de.cesr.lara.components.eventbus.events.LaraEvent)
	 */
	@Override
	public void onInternalEvent(LaraEvent e) {
		LPpBoCollectorEvent event = castEvent(LPpBoCollectorEvent.class, e);

		Collection<CraftyPa<?>> pas = new ArrayList<>();

		// the event will only be published by agents of type A
		LaraBehaviouralComponent agent = ((LaraBehaviouralComponent) event.getAgent());

		for (CraftyPa<?> pa : agent.getLaraComp().getBOMemory().recallAllMostRecent()) {
			if (agent.getAgent().getFC().getFR() instanceof ComiVariantProductionFR
			        && ((ComiVariantProductionFR) agent.getAgent().getFC().getFR()).isOf()) {
				if (pa.getKey().startsWith("OF")) {
					pas.add(pa);
				}
			} else {
				if (pa.getKey().startsWith("Conv")) {
					pas.add(pa);
				}
			}
		}

		agent.getLaraComp().getDecisionData(event.getdConfig()).setBos(pas);
	}
}
