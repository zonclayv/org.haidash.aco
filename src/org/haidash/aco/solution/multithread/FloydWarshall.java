package org.haidash.aco.solution.multithread;

import java.util.ArrayList;
import java.util.List;

public class FloydWarshall {

	private int nodesNumber;
	private int maxFuel;
	private int targetNode;

	private int[][] matrix;
	private int[] fuelInNodes;

	private List<ArrayList<Integer>> costsToMove;
	private List<ArrayList<Integer>> fuel;
	private List<ArrayList<Integer>> prevNode;
	private int[] remainsFuel;

	public FloydWarshall(int nodesNumber, int[][] matrix, int[] fuelInNodes, int maxFuel, int targetNode) {
		this.nodesNumber = nodesNumber;
		this.maxFuel = maxFuel;
		this.matrix = matrix;
		this.fuelInNodes = fuelInNodes;
		this.targetNode = targetNode;
		calculate();
	}

	public int[] getRemainsFuel() {
		return remainsFuel;
	}

	public List<ArrayList<Integer>> getCostsToMove() {
		return costsToMove;
	}

	private void calculate() {
		costsToMove = new ArrayList<ArrayList<Integer>>();
		fuel = new ArrayList<ArrayList<Integer>>();
		remainsFuel = new int[nodesNumber];
		prevNode = new ArrayList<ArrayList<Integer>>();

		final int inf = Integer.MAX_VALUE;

		for (int i = 0; i < nodesNumber; i++) {
			List<Integer> line = new ArrayList<Integer>();
			for (int j = 0; j < nodesNumber; j++) {
				if (i != j) {
					line.add(inf);
				} else {
					line.add(0);
				}
			}

			costsToMove.add(new ArrayList<Integer>(line));
			fuel.add(new ArrayList<Integer>(line));
			remainsFuel[i] = inf;
		}

		for (int i = 0; i < nodesNumber; i++) {
			List<Integer> prev = new ArrayList<Integer>();
			for (int j = 0; j < nodesNumber; j++) {
				prev.add(i);
			}
			prevNode.add(new ArrayList<Integer>(prev));
		}
		for (int firstNode = 0; firstNode < nodesNumber; firstNode++) {
			for (int secondNode = 0; secondNode < nodesNumber; secondNode++) {

				int fuelCost = matrix[firstNode][secondNode];

				if (fuelCost <= 0) {
					continue;
				}

				if (fuelCost <= maxFuel) {
					costsToMove.get(firstNode).set(secondNode, fuelCost);
					costsToMove.get(secondNode).set(firstNode, fuelCost);
				}

				Integer fuelInFirstNode = fuelInNodes[firstNode];
				if (fuelInFirstNode > maxFuel) {
					fuel.get(firstNode).set(secondNode, maxFuel);
				} else {
					fuel.get(firstNode).set(secondNode, fuelInFirstNode);
				}

				Integer fuelInSecondNode = fuelInNodes[secondNode];
				if (fuelInSecondNode > maxFuel) {
					fuel.get(secondNode).set(firstNode, maxFuel);
				} else {
					fuel.get(secondNode).set(firstNode, fuelInSecondNode);
				}

			}

		}

		for (int k = 0; k < nodesNumber; ++k) {
			for (int i = 0; i < nodesNumber; ++i) {
				for (int j = 0; j < nodesNumber; ++j) {

					if ((costsToMove.get(i).get(k) < inf) && (costsToMove.get(k).get(j) < inf)) {

						if (costsToMove.get(i).get(j) > costsToMove.get(i).get(k) + costsToMove.get(k).get(j)) {
							prevNode.get(i).set(j, k);
						}

						costsToMove.get(i).set(j,
								Math.min(costsToMove.get(i).get(j), costsToMove.get(i).get(k) + costsToMove.get(k).get(j)));

					}

					if ((fuel.get(i).get(k) < inf) && (fuel.get(k).get(j) < inf)) {

						fuel.get(i).set(j, Math.min(fuel.get(i).get(j), fuel.get(i).get(k) + fuel.get(k).get(j)));

					}
				}
			}
		}

		int finish = targetNode;
		for (int i = 0; i < nodesNumber; ++i) {
			int start = i;
			int from;
			do {
				from = findPrevVertex(finish, start);
				if ((costsToMove.get(start).get(from) == inf) || (fuel.get(start).get(from) == inf)) {
					remainsFuel[i] = Math.min(remainsFuel[i], inf);

				} else {
					remainsFuel[i] = Math.min(remainsFuel[i], fuel.get(i).get(from) - costsToMove.get(i).get(from));
				}

				start = from;
			} while (finish != from);

		}

	}

	public int findPrevVertex(int start, int finish) {
		return prevNode.get(start).get(finish);
	}
}
