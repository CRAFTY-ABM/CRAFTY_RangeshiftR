/**
 *
 */
package org.volante.abm.schedule;


import mpi.MPI;
import mpi.MPIException;

import org.apache.log4j.Logger;


/**
 * @author Sascha Holzhauer
 *
 */
public class MpiUtilities {

	static public final int	MPI_TAG_DEMAND_SEND			= 10;
	static public final int	MPI_TAG_DEMAND_BROADCAST	= 11;

	static public final int	MPI_TAG_SUPPLY_SEND			= 20;
	static public final int	MPI_TAG_SUPPLY_BROADCAST	= 21;

	/**
	 * Logger
	 */
	static private Logger	logger	= Logger.getLogger(MpiUtilities.class);

	public static void sendRegionDemand(double demand) {
		double[] message = new double[1];
		message[0] = demand;
		try {
			MPI.COMM_WORLD.Ssend(message, 0, message.length, MPI.DOUBLE, 0, MPI_TAG_DEMAND_SEND);
		} catch (MPIException exception) {
			logger.error("Error during MPI Send Demand: " + exception.getMessage());
			exception.printStackTrace();
		}
	}

	public static double collectRegionsDemand() {
		double[] message = new double[1];
		try {
			MPI.COMM_WORLD.Recv(message, 0, 1, MPI.DOUBLE, 0, MPI_TAG_DEMAND_SEND);
		} catch (MPIException exception) {
			exception.printStackTrace();
		}
		return message[0];
	}

	public static void broadcastWorldDemand(double demand) {
		double[] message = new double[1];
		message[0] = demand;
		try {
			MPI.COMM_WORLD.Bcast(message, 0, 1, MPI.DOUBLE, MPI_TAG_DEMAND_BROADCAST);
		} catch (MPIException exception) {
			logger.error("Error during MPI Broadcast Demand: " + exception.getMessage());
			exception.printStackTrace();
		}
	}

	public static int distributeNumOfCells(int numRegionalCells) {
		int[] result = new int[1];
		int[] message = new int[1];
		message[0] = numRegionalCells;

		try {
			// <- LOGGING
			logger.info("Distributing number of cells on node " + MPI.COMM_WORLD.Rank());
			// LOGGING ->

			MPI.COMM_WORLD.Allreduce(message, 0, result, 0, 1, MPI.INT,
					MPI.SUM);
		} catch (MPIException exception) {
			logger.error("Error during MPI distributing number of cells: " + exception.getMessage());
			exception.printStackTrace();
		}
		return result[0];
	}

	public static double[] distributeWorldDemand(double[] demand) {
		double[] result = null;
		try {
			// <- LOGGING
			logger.info("Distributing world demand on node " + MPI.COMM_WORLD.Rank());
			// LOGGING ->

			result = new double[demand.length];

			MPI.COMM_WORLD.Allreduce(demand, 0, result, 0, demand.length, MPI.DOUBLE,
					MPI.SUM);
		} catch (MPIException exception) {
			logger.error("Error during MPI distributing demand: " + exception.getMessage());
			exception.printStackTrace();
		}
		return result;
	}

	public static double receiveWorldDemand() {
		// TODO gather?
		return 0.0;
	}

	public static void sendRegionSupply(double supply) {
		double[] message = new double[1];
		message[0] = supply;
		try {
			MPI.COMM_WORLD.Ssend(message, 0, message.length, MPI.DOUBLE, 0, MPI_TAG_SUPPLY_SEND);
		} catch (MPIException exception) {
			logger.error("Error during MPI Send Supply: " + exception.getMessage());
			exception.printStackTrace();
		}
	}

	public static double getWorldSupply() {
		double[] message = new double[1];
		try {
			MPI.COMM_WORLD.Recv(message, 0, 1, MPI.DOUBLE, 0, MPI_TAG_SUPPLY_SEND);
		} catch (MPIException exception) {
			exception.printStackTrace();
		}
		return message[0];
	}

	public static void broadcastWorldSupply(double supply) {
		double[] message = new double[1];
		message[0] = supply;
		try {
			MPI.COMM_WORLD.Bcast(message, 0, 1, MPI.DOUBLE, MPI_TAG_SUPPLY_BROADCAST);
		} catch (MPIException exception) {
			logger.error("Error during MPI Broadcast Demand: " + exception.getMessage());
			exception.printStackTrace();
		}
	}

	public static double[] distributeWorldSupply(double[] supply) {
		double[] result = null;
		try {
			// <- LOGGING
			logger.info("Distributing world supply on node " + MPI.COMM_WORLD.Rank());
			// LOGGING ->

			result = new double[supply.length];

			MPI.COMM_WORLD.Allreduce(supply, 0, result, 0, supply.length, MPI.DOUBLE,
					MPI.SUM);
		} catch (MPIException exception) {
			logger.error("Error during MPI distributing supply: " + exception.getMessage());
			exception.printStackTrace();
		}
		return result;
	}

	public static double receiveWorldSupply() {
		return 0.0;
	}
}
