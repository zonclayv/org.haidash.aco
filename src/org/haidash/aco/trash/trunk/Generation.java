package org.haidash.aco.trash.trunk;

import java.util.List;

import org.haidash.aco.trash.model.Pair;
import org.haidash.aco.trash.model.SearchResult;

public class Generation {

	// private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	private final AntColony instance;
	private final int start;

	private Pair<Double, Double>[][] pheromones;
	private int[][] nodeVisits;

	public Generation(final AntColony instance, final int start) {
		this.instance = instance;
		this.start = start;

		nodeVisits = initMatrix();
	}

	private int[][] initMatrix() {

		int numNodes = AntColony.numNodes;
		int[][] matrix = new int[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				matrix[i][j] = 0;
			}
		}

		return matrix;
	}

	public int getVisitCount(int from, int to) {
		return nodeVisits[from][to];
	}

	public void addVisit(int from, int to) {
		int visitsCount = nodeVisits[from][to];
		nodeVisits[from][to] = visitsCount + 1;

	}

	@SuppressWarnings("unchecked")
	private Pair<Double, Double>[][] initPheromones() {

		int numNodes = AntColony.numNodes;
		Pair<Double, Double>[][] pheromones = new Pair[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				pheromones[i][j] = new Pair<Double, Double>(instance.globalPheromones[i][j]);
			}
		}

		return pheromones;
	}

	public int[] getFuelLevels() {
		return instance.fuelLevel.clone();
	}

	public int getFuelCost(int from, int to) {
		return instance.matrix[from][to];
	}

	public int getRemainsFuel(int start) {
		return instance.getRemainsFuel(start);
	}

	public final double readTau(final int x, final int y) {
		final Pair<Double, Double> pair = pheromones[x][y];
		return pair.first / pair.second;
	}

	public SearchResult start() {

		pheromones = initPheromones();

		SearchResult bestSearchResult = null;

		for (int i = 0; i < AntColony.numAnts; i++) {

			final Ant ant = new Ant(this, start);
			final SearchResult searchResult = ant.search();

			if (searchResult == null) {
				continue;
			}

			if (bestSearchResult == null || searchResult.getTotalCost() < bestSearchResult.getTotalCost()) {
				bestSearchResult = searchResult;
			}
		}

		instance.updatePheromones(pheromones);

		return bestSearchResult;
	}

	public void updatePheromones(final boolean outOfFuel, final List<Integer> visited, final int totalCost) {

		int first = visited.get(0);
		double deltaTau = 0;

		if (totalCost != 0) {
			deltaTau = AntColony.Q / totalCost;
		}

		for (int i = 1; i < visited.size(); i++) {

			final Integer second = visited.get(i);

			final Pair<Double, Double> pairPheromones = pheromones[first][second];

			double pValue = pairPheromones.first;
			double nValue = pairPheromones.second;

			if (!outOfFuel) {
				pValue += deltaTau;
			} else {
				nValue += deltaTau;
			}

			pheromones[first][second] = new Pair<Double, Double>(pValue, nValue);

			first = second;
		}
	}
}
