package org.haidash.aco.solution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.haidash.aco.solution.model.Cycle;
import org.haidash.aco.solution.model.Pair;
import org.haidash.aco.solution.model.SearchResult;

public class AntColony {

	private static final Logger LOGGER = Logger.getLogger(AntColony.class);

	private final Pair<Double, Double>[][] globalPheromones;
	private final Map<Integer, Cycle> cycles;
	private final Set<List<Integer>> badPaths;

	private SearchResult searchResult;

	public AntColony() {
		this.cycles = new HashMap<Integer, Cycle>();
		this.badPaths = new HashSet<List<Integer>>();
		this.globalPheromones = initGlobalPheromones();
	}

	@SuppressWarnings("unchecked")
	private Pair<Double, Double>[][] initGlobalPheromones() {

		final AcoProperties properties = AcoProperties.getInstance();
		final int numNodes = properties.getNumNodes();
		final Pair<Double, Double>[][] result = new Pair[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				result[i][j] = new Pair<Double, Double>(new Pair<Double, Double>(1.0, 1.0));
			}
		}

		return result;
	}

	public void start() {

		final AcoProperties properties = AcoProperties.getInstance();

		LOGGER.info("PROCESS START '" + properties.getStartNode() + "' -> '" + properties.getTargetNode() + "'...");

		final long startTime = System.currentTimeMillis();

		for (int i = 0; i < properties.getNumGeneration(); i++) {

			final Generation generation = new Generation(badPaths, cycles, globalPheromones);
			searchResult = generation.start();

			updatePheromones();
		}

		final long finishTime = System.currentTimeMillis() - startTime;

		LOGGER.info("PROCESS FINISH (" + finishTime + "ms):");

		if (searchResult == null) {
			LOGGER.info("Path not found");
		} else {
			LOGGER.info("Best path: " + searchResult.getTotalCost());
			LOGGER.info(searchResult.getVisited().toString());
		}
	}

	public SearchResult getSearchResult() {
		return searchResult;
	}

	private void updatePheromones() {

		final AcoProperties properties = AcoProperties.getInstance();
		final int numNodes = properties.getNumNodes();

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {

				final Pair<Double, Double> pheromon = globalPheromones[i][j];

				double pValue = pheromon.first;
				double nValue = pheromon.second;

				pValue *= 1.0 - AcoProperties.PHEROMONE_PERSISTENCE;

				if (pValue < 1.0) {
					pValue = 1.0;
				}

				nValue *= 1.0 - AcoProperties.PHEROMONE_PERSISTENCE;

				if (nValue < 1.0) {
					nValue = 1.0;
				}

				globalPheromones[i][j] = new Pair<Double, Double>(pValue, nValue);
			}
		}
	}
}
