package org.volante.abm.visualisation;

import static org.volante.abm.example.SimpleService.*;

import javax.swing.JFrame;

import org.volante.abm.example.*;

import com.moseph.modelutils.curve.*;

public class CurveCompetitivenessDisplayTest extends BasicTestsUtils
{

	public static void main( String[] args ) throws Exception
	{
		new CurveCompetitivenessDisplayTest().runTest();
	}
	
	public void runTest() throws Exception
	{
		CurveCompetitivenessModel comp = new CurveCompetitivenessModel();
		comp.setCurve( HOUSING, new GeneralisedLogisticFunction( 10, 1.1, 0 ));
		comp.setCurve( TIMBER, new LinearFunction( 4, 2 ));
		comp.setCurve( FOOD, new LinearFunction( 3, 3 ));
		comp.setCurve( RECREATION, new LinearFunction( 2, 4 ));
		r1.setCompetitivenessModel( comp );
		comp.initialise( modelData, runInfo, r1 );
		RegionalDemandModel demand = new RegionalDemandModel();
		r1.setDemandModel( demand );
		demand.initialise( modelData, runInfo, r1 );
		
		
		CurveCompetitivenessDisplay disp = comp.getDisplay();
		disp.initialise( modelData, runInfo, r1 );
		disp.update();
		JFrame frame = new JFrame();
		frame.add( disp.getDisplay() );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.pack();
		frame.setVisible( true );
	}
}
