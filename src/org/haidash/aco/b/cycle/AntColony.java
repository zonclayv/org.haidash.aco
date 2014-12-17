package org.haidash.aco.b.cycle;

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

	public static final String INPUT = "files/input_26.txt";

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
	private final Pair<Double, Double>[][] globalPheromones;
	private final int[] remainsFuel;
	private final Map<Integer, Cycle> cycles;
	private final Set<List<Integer>> badPaths;

	public AntColony() throws IOException {

		this.cycles = new HashMap<Integer, Cycle>();
		this.badPaths = new HashSet<List<Integer>>();

		readFromFile(INPUT);

		numAnts = numNodes / 2;
		numGeneration = numNodes / 2;

		globalPheromones = initGlobalPheromones();

		this.remainsFuel = FloydWarshall.getRemainsFuel(numNodes, nodesMap, fuelLevels, maxFuel, targetNode);
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

	private void readFromFile(final String filePath) throws IOException {

		final Scanner text = new Scanner(new FileReader(filePath));

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

		text.close();
	}

	@Override
	public SearchResult search() {

		logger.info("PROCESS START '" + startNode + "' -> '" + targetNode + "'...");

		long startTime = System.currentTimeMillis();

		SearchResult bestSearchResult = null;
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
			final SearchResult route = generation.start();

			updatePheromones();

			if (route == null) {
				continue;
			}

			if (bestSearchResult == null || route.getTotalCost() < bestSearchResult.getTotalCost()) {
				bestSearchResult = route;
			}
		}

		logger.info("PROCESS FINISH (" + (System.currentTimeMillis() - startTime) + "ms):");

		if (bestSearchResult == null) {
			logger.info("Path not found");
		} else {
			logger.info("Best path: " + bestSearchResult.getTotalCost());
			logger.info(bestSearchResult.getVisited().toString());
		}

		return bestSearchResult;
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
