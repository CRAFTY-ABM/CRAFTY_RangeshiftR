/**
 * 
 */
package org.volante.abm.comi.param;

import de.cesr.parma.core.PmParameterDefinition;

/**
 * @author Sascha Holzhauer
 *
 */
public enum ComiParameters implements PmParameterDefinition {
	
	MU(Double.class, 0.1);

	
	private Class<?>	type;
	private Object		defaultValue;
	
	/**
	 * 
	 */
	private ComiParameters(Class<?> type, Object defaultValue) {
			this.type = type;
			this.defaultValue = defaultValue;
		}
	
	/**
	 * @see de.cesr.parma.core.PmParameterDefinition#getType()
	 */
    @Override
    public Class<?> getType() {
	    return this.type;
    }

	/**
	 * @see de.cesr.parma.core.PmParameterDefinition#getDefaultValue()
	 */
    @Override
    public Object getDefaultValue() {
	    return this.defaultValue;
    }

}
