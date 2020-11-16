/**
 * 
 */
package org.volante.abm.institutions;


import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Region;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.transform.IntTransformer;

/**
 * Reads protected land use for ticks from a CSV file and controls land use competition 
 * accordingly. The adjustment is performed at the beginning of each tick
 * (e.g. before perceiving social networks) @TODO or after?.   
 * 
 */


/**
 * @see org.volante.abm.institutions.AbstractInstitution#initialise(org.volante.abm.data.ModelData,
 *      org.volante.abm.schedule.RunInfo, org.volante.abm.data.Region)
 * 		CellCSVReader
 * 		CellRasterReader
 * 		CSVCapitalUpdater
 */

/**
 * @author Bumsuk Seo
 *
 */




public class LanduseControllingInstitution extends AbstractInstitution {

	/**
	 * Logger
	 */
	static private Logger logger = Logger.getLogger(LanduseControllingInstitution.class);

	/**
	 * Name of CSV file that contains per-tick land use changed allowed YN
	 */
	@Element(required = false)
	protected String csvFileProhibitedLanduse = null;
	
	/**
	 * Name of column in CSV file that specifies the year a row belongs to
	 */
 
	
	@Element(required = false)
	String xColumn = "x";
	@Element(required = false)
	String yColumn = "y";
	@Element(required = false)
	String prohibitedColumn = "Protected";
	
	@Element(required= false)
	String maskChar = "Y";
	
	 
	IntTransformer	xTransformer	= null;
	IntTransformer	yTransformer	= null;

    @Override
	public void initialise(ModelData data, RunInfo info, Region extent) throws Exception {
		super.initialise(data, info, extent);

		// <- LOGGING
		logger.info("Initialise " + this);
		// LOGGING ->
//		logger.info("Loading land use restriction CSV from " + csvFileProhibitedLanduse);



//		try {

//			ABMPersister persister = ABMPersister.getInstance();
//
//  
//			logger.info("Loading cell CSV from " + csvFileProhibitedLanduse);
// 
//			CsvReader reader = persister.getCSVReader(csvFileProhibitedLanduse, this.region.getPersisterContextExtra());
//
//			List<String> columns = Arrays.asList(reader.getHeaders());
//			
//			if (!columns.contains(prohibitedColumn)) { 
//				throw new IllegalStateException(
//				        "The land use controlling institution does not have " + prohibitedColumn +  " in the CSV file " + csvFileProhibitedLanduse);
//			}
//			
//			 
//			
//			while (reader.readRecord()) {
// 				if (logger.isDebugEnabled()) {
//					logger.debug("Read row " + reader.getCurrentRecord());
//				}
// 
//				int x = Integer.parseInt(reader.get(xColumn));
//				if (xTransformer != null) {
//					x = xTransformer.transform(x);
//				}
//
//				int y = Integer.parseInt(reader.get(yColumn));
//				if (yTransformer != null) {
//					y = yTransformer.transform(y);
//				}
//	 
//				boolean yn = reader.get(prohibitedColumn).equalsIgnoreCase(maskChar);
// 				logger.debug(yn);
//
//				Cell cell = region.getCell(x, y);
//				
//				cell.setFRmutable(yn);
//  
//			}


//			if (!columns.contains(prohibitedColumn)) { 
//		} catch (Exception exception) {
//			exception.printStackTrace();
//			logger.fatal("Land Use Controlling Institution failed: " + exception.toString());
//
//			System.exit(0);
//		}
 

	}

 
	 
	 
//	
//	//@Deprecated (use CSVLandUseUpdate instead)
//
//	/**
//	 * Do annual updating
//	 */
//	@Override
//	public void update()
//	{
//		super.update();
//		logger.info(this + "in update() @TODO apply new YN marker");
//
//		try {
////			CsvReader file = getFileForYear();
//// 			if( file != null ) {
////				applyFile( file );
////			}
//		} catch ( Exception e )
//		{
//			logger.fatal( "Couldn't update Capitals: " + e.getMessage() );
//			e.printStackTrace();
//		}
//	}
//	
//  
	

	
	/**
	 * Checks configured restriction CSV file.
	 * 
	 * @param fr
	 * @param cell
	 * @return true if it is chnage the FR of the given cell.
	 */
	@Override
	public boolean isAllowed(FunctionalRole fr, Cell cell) {
 
		boolean landuseallowed = cell.getFRmutable();

		if (landuseallowed) {
			// <- LOGGING
			logger.debug("Land use change allowed X" + cell.getX() + "Y" + cell.getY());
			// LOGGING ->
			return true;
		} else {
			logger.info("Land use change prohibited X" + cell.getX() + "Y" + cell.getY());

			return false;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ("Land Use Controlling Institution");
	}
}


 

