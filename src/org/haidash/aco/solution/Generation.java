package org.haidash.aco.solution;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.haidash.aco.model.Cycle;
import org.haidash.aco.model.Pair;
import org.haidash.aco.model.SearchResult;


public class Generation {

	// private static final Logger LOGGER = Logger.getLogger(Generation.class);

	// global
	private final Set<List<Integer>> badPaths;
	private final Map<Integer, Cycle> cycles;
	private final Pair<Double, Double>[][] globalPheromones;

	// local
	private final Pair<Double, Double>[][] pheromones;
	private final int[][] nodeVisits;

	private SearchResult bestResult;

	public Generation(final Set<List<Integer>> badPaths, final Map<Integer, Cycle> cycles, final Pair<Double, Double>[][] globalPheromones) {

		// global
		this.badPaths = badPaths;
		this.cycles = cycles;
		this.globalPheromones = globalPheromones;

		// local
		this.nodeVisits = initMatrix();
		this.pheromones = initPheromones(globalPheromones);
	}

	private int[][] initMatrix() {

		final AcoProperties properties = AcoProperties.getInstance();
		final int numNodes = properties.getNumNodes();
		final int[][] matrix = new int[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				matrix[i][j] = 0;
			}
		}

		return matrix;
	}

	@SuppressWarnings("unchecked")
	private Pair<Double, Double>[][] initPheromones(final Pair<Double, Double>[][] globalPheromones) {

		final AcoProperties properties = AcoProperties.getInstance();
		final int numNodes = properties.getNumNodes();
		final Pair<Double, Double>[][] pheromones = new Pair[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				pheromones[i][j] = new Pair<Double, Double>(globalPheromones[i][j]);
			}
		}

		return pheromones;
	}

	public SearchResult start() {

		final AcoProperties properties = AcoProperties.getInstance();

		for (int i = 0; i < properties.getNumAnts(); i++) {

			final Ant ant = new Ant(badPaths, cycles, nodeVisits, pheromones);

			ant.updatePheromones(globalPheromones);

			final SearchResult result = ant.search();

			if (result != null && (bestResult == null || result.getTotalCost() < bestResult.getTotalCost())) {
				bestResult = result;
			}
		}

		return bestResult;
	}

}
