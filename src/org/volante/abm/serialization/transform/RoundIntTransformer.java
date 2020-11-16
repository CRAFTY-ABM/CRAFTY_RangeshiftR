/**
 * 
 */
package org.volante.abm.serialization.transform;


import org.simpleframework.xml.Attribute;


/**
 * @author Sascha Holzhauer
 *
 */
public class RoundIntTransformer implements IntTransformer {

	@Attribute(required = false)
	double	factor	= 1.0;

	/**
	 * @see org.volante.abm.serialization.transform.IntTransformer#transform(int)
	 */
	@Override
	public int transform(int number) {
		return (int) Math.round(number * factor);
	}
}
