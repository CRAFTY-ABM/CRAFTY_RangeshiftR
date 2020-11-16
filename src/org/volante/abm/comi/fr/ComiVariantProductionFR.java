/**
 * 
 */
package org.volante.abm.comi.fr;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.fr.VariantProductionFR;
import org.volante.abm.models.ProductionModel;

/**
 * @author Sascha Holzhauer
 *
 */
public class ComiVariantProductionFR extends VariantProductionFR {

	@Element(required = true)
	protected String alternativeFr;

	@Attribute(required = false)
	protected boolean isOf = false;

	public ComiVariantProductionFR(@Attribute(name = "label") String label,
	        @Element(name = "production") ProductionModel production) {
		super(label, production);
	}

	/**
	 * @param id
	 * @param serialId
	 * @param production
	 * @param givingUp
	 * @param givingIn
	 */
	public ComiVariantProductionFR(String id, int serialId, ProductionModel production, double givingUp, double givingIn) {
		super(id, serialId, production, givingUp, givingIn);
	}

	public String getAlternativeFrId() {
		return this.alternativeFr;
	}

	public boolean isOf() {
		return this.isOf;
	}
}
