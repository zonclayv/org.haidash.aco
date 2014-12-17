package org.haidash.aco.solution.cycle;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.haidash.aco.model.Pair;
import org.haidash.aco.model.SearchResult;

public class Generation {

	// private final static Logger LOGGER = Logger.getLogger(Generation.class);

	// global
	private final int startNode;
	private final int targetNode;
	private final int numNodes;
	private final int maxFuel;
	private final int numAnts;
	private final int[][] nodesMap;
	private final int[] fuelLevels;
	private final int[] remainingsFuel;
	private final Set<List<Integer>> badPaths;
	private final Map<Integer, Cycle> cycles;
	private final Pair<Double, Double>[][] globalPheromones;

	// local
	private Pair<Double, Double>[][] pheromones;
	private final int[][] nodeVisits;

	public Generation(final int startNode,
			final int targetNode,
			final int numNodes,
			final int maxFuel,
			final int numAnts,
			final int[][] nodesMap,
			final int[] fuelLevels,
			final int[] remainingsFuel,
			final Set<List<Integer>> badPaths,
			final Map<Integer, Cycle> cycles,
			final Pair<Double, Double>[][] globalPheromones) {

		// global
		this.startNode = startNode;
		this.targetNode = targetNode;
		this.numNodes = numNodes;
		this.maxFuel = maxFuel;
		this.numAnts = numAnts;
		this.nodesMap = nodesMap;
		this.fuelLevels = fuelLevels;
		this.remainingsFuel = remainingsFuel;
		this.badPaths = badPaths;
		this.cycles = cycles;
		this.globalPheromones = globalPheromones;

		// local
		this.nodeVisits = initMatrix();
	}

	private int[][] initMatrix() {
		int[][] matrix = new int[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				matrix[i][j] = 0;
			}
		}

		return matrix;
	}

	@SuppressWarnings("unchecked")
	public void initPheromones(Pair<Double, Double>[][] globalPheromones) {

		pheromones = new Pair[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				pheromones[i][j] = new Pair<Double, Double>(globalPheromones[i][j]);
			}
		}
	}

	public SearchResult search() {

		SearchResult bestResult = null;

		for (int i = 0; i < numAnts; i++) {

			final Ant ant =
					new Ant(startNode,
							targetNode,
							numNodes,
							maxFuel,
							nodesMap,
							fuelLevels,
							remainingsFuel,
							badPaths,
							cycles,
							nodeVisits,
							pheromones);

			ant.updatePheromones(globalPheromones);

			final SearchResult searchResult = ant.search();

			if (searchResult == null) {
				continue;
			}

			if (bestResult == null || searchResult.getTotalCost() < bestResult.getTotalCost()) {
				bestResult = searchResult;
			}
		}

		return bestResult;
	}

}
