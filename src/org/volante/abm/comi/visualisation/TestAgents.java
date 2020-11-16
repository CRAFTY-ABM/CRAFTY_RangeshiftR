package org.volante.abm.comi.visualisation;

import java.awt.Color;

import org.volante.abm.visualisation.AgentTypeDisplay;

public class TestAgents extends AgentTypeDisplay
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8136199562481069547L;

	public TestAgents()
	{
		addAgent("EP", Color.red.darker());
		addAgent("Ext_AF", Color.orange.darker());
		addAgent("IA", Color.yellow.brighter());
		addAgent("Int_AF", Color.orange.brighter());
		addAgent("Int_Fa", Color.yellow.darker());
		addAgent("IP", Color.red.brighter());
		addAgent("MF", Color.green.brighter());
		addAgent("Min_man", Color.gray.brighter());
		addAgent("Mix_Fa", Color.pink.brighter());
		addAgent("Mix_For", Color.green.darker());
		addAgent("Mix_P", Color.magenta.brighter());
		addAgent("Multifun", Color.blue.brighter());
		addAgent("P-Ur", Color.blue.darker());	
		addAgent("UL", Color.gray.darker());	
		addAgent("UMF", Color.green.darker());	
		addAgent("Ur", Color.black.brighter());	
		addAgent("VEP", Color.magenta.darker());	
	}
}
