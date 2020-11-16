/**
 * 
 */
package org.volante.abm.example.measures;


import java.util.HashSet;
import java.util.Set;

import org.volante.abm.agent.fr.FunctionalRole;
import org.volante.abm.data.Cell;
import org.volante.abm.data.Region;


/**
 * @author Sascha Holzhauer
 * 
 */
public class ConnectivityMeasure {

	/**
	 * Mean proportion of neighbouring cells under the same management (neighbouring cells defined as the 8 immediate
	 * neighbours of any given cell). Cells with fewer neighbours (those at the edge of the modelled world) are not
	 * included.
	 * 
	 * @param region
	 * @return mean of proportion of neighbours of same FR
	 */
	public static double getScore(Region region) {
		double sum = 0;
		int counter = 0;

		int cId;
		int sameNeighbours;

		for (Cell c : region.getAllCells()) {
			cId = c.getOwnersFrSerialID();
			sameNeighbours = 0;
			Set<Cell> neighbours = region.getAdjacentCells(c);
			if (neighbours.size() == 8) {
				for (Cell n : neighbours) {
					if (n.getOwnersFrSerialID() == cId) {
						sameNeighbours++;
					}
				}
				sum += sameNeighbours / 8.0;
				counter++;
			}
		}
		return sum / counter;
	}

	/**
	 * Mean proportion of neighbouring cells under the same management (neighbouring cells defined as the 8 immediate
	 * neighbours of any given cell). Cells with fewer neighbours (those at the edge of the modelled world) are not
	 * included.
	 * 
	 * @param region
	 * @param fr
	 * @return mean of proportion of neighbours of same FR
	 */
	public static double getScore(Region region, FunctionalRole fr) {
		double sum = 0;
		int counter = 0;

		int cId = fr.getSerialID();
		int sameNeighbours;

		for (Cell c : region.getAllCells()) {
			if (c.getOwnersFrSerialID() == cId) {
				sameNeighbours = 0;
				Set<Cell> neighbours = region.getAdjacentCells(c);
				if (neighbours.size() == 8) {
					for (Cell n : neighbours) {
						if (n.getOwnersFrSerialID() == cId) {
							sameNeighbours++;
						}
					}
					sum += sameNeighbours / 8.0;
					counter++;
				}
			}
		}
		return sum / counter;
	}

	/**
	 * Mean proportion of neighbouring cells under similar management (neighbouring cells defined as the 8 immediate
	 * neighbours of any given cell). Cells with fewer neighbours (those at the edge of the modelled world) are not
	 * included.
	 * 
	 * @param region
	 * @param frs
	 *        set of FRs regarded as similar
	 * @return mean of proportion of neighbours of same FR
	 */
	public static double getScore(Region region, Set<FunctionalRole> frs) {
		double sum = 0;
		int counter = 0;

		Set<Integer> cIds = new HashSet<>();
		for (FunctionalRole fr : frs) {
			cIds.add(fr.getSerialID());
		}

		int sameNeighbours;

		for (Cell c : region.getAllCells()) {
			if (cIds.contains(c.getOwnersFrSerialID())) {
				sameNeighbours = 0;
				Set<Cell> neighbours = region.getAdjacentCells(c);
				if (neighbours.size() == 8) {
					for (Cell n : neighbours) {
						if (cIds.contains(n.getOwnersFrSerialID())) {
							sameNeighbours++;
						}
					}
					sum += sameNeighbours / 8.0;
					counter++;
				}
			}
		}
		return sum / counter;
	}
}
