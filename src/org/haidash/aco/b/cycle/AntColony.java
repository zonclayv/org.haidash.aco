package org.haidash.aco.b.cycle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;
import org.haidash.aco.model.FloydWarshall;
import org.haidash.aco.model.Pair;
import org.haidash.aco.model.SearchAlgorithm;
import org.haidash.aco.model.SearchResult;

public class AntColony implements SearchAlgorithm {

	private final Logger logger = Logger.getLogger(AntColony.class);

	public static final double ALPHA = 0.1;
	public static final double BETA = 0.1;
	public static final double Q = 0.0001d;
	public static final double PHEROMONE_PERSISTENCE = 0.3d;
	public static final double INITIAL_PHEROMONES = 0.8d;

	private int startNode;
	private int targetNode;
	private int numNodes;
	private int maxFuel;
	private int numGeneration;
	private int numAnts;

	private int[][] nodesMap;
	private int[] fuelLevels;
	private Pair<Double, Double>[][] globalPheromones;
	private int[] remainsFuel;

	private final Map<Integer, Cycle> cycles;
	private final Set<List<Integer>> badPaths;

	public AntColony() {
		this.cycles = new HashMap<Integer, Cycle>();
		this.badPaths = new HashSet<List<Integer>>();
	}

	@SuppressWarnings("unchecked")
	private Pair<Double, Double>[][] initGlobalPheromones() {

		Pair<Double, Double>[][] result = new Pair[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				result[i][j] = new Pair<Double, Double>(new Pair<Double, Double>(1.0, 1.0));
			}
		}

		return result;
	}

	@Override
	public void initializeValue(final File file) throws IOException {

		try (Scanner text = new Scanner(new FileReader(file))) {

			numNodes = text.nextInt();

			fuelLevels = new int[numNodes];

			for (int i = 0; i < numNodes; i++) {
				fuelLevels[i] = text.nextInt();
			}

			final int numEdges = text.nextInt();

			nodesMap = new int[numNodes][numNodes];

			for (int i = 0; i < numNodes; i++) {
				for (int j = 0; j < numNodes; j++) {
					nodesMap[i][j] = -1;
				}
			}

			for (int i = 0; i < numEdges; i++) {
				final int start = text.nextInt();
				final int finish = text.nextInt();

				nodesMap[start][finish] = text.nextInt();
			}

			maxFuel = text.nextInt();
			startNode = text.nextInt();
			targetNode = text.nextInt();

		}

		numAnts = numNodes / 2;
		numGeneration = numNodes / 2;

		globalPheromones = initGlobalPheromones();
		remainsFuel = FloydWarshall.getRemainsFuel(numNodes, nodesMap, fuelLevels, maxFuel, targetNode);
	}

	@Override
	public SearchResult search() {

		logger.info("PROCESS START '" + startNode + "' -> '" + targetNode + "'...");

		long startTime = System.currentTimeMillis();

		SearchResult bestResult = null;

		for (int i = 0; i < numGeneration; i++) {

			final Generation generation =
					new Generation(startNode,
							targetNode,
							numNodes,
							maxFuel,
							numAnts,
							nodesMap,
							fuelLevels,
							remainsFuel,
							badPaths,
							cycles,
							globalPheromones);

			generation.initPheromones(globalPheromones);

			final SearchResult route = generation.search();

			updatePheromones();

			if (route == null) {
				continue;
			}

			if (bestResult == null || route.getTotalCost() < bestResult.getTotalCost()) {
				bestResult = route;
			}
		}

		logger.info("PROCESS FINISH (" + (System.currentTimeMillis() - startTime) + "ms):");

		if (bestResult == null) {
			logger.info("Path not found");
		} else {
			logger.info("Best path: " + bestResult.getTotalCost());
			logger.info(bestResult.getVisited().toString());
		}

		return bestResult;
	}

	private void updatePheromones() {

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {

				final Pair<Double, Double> pheromon = globalPheromones[i][j];

				double pValue = pheromon.first;
				double nValue = pheromon.second;

				pValue *= 1.0 - AntColony.PHEROMONE_PERSISTENCE;

				if (pValue < 1.0) {
					pValue = 1.0;
				}

				nValue *= 1.0 - AntColony.PHEROMONE_PERSISTENCE;

				if (nValue < 1.0) {
					nValue = 1.0;
				}

				globalPheromones[i][j] = new Pair<Double, Double>(pValue, nValue);
			}
		}
	}
}
