package org.haidash.aco.trash.trunk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.haidash.aco.solution.model.AcoRuntimeException;
import org.haidash.aco.trash.model.Pair;
import org.haidash.aco.trash.model.SearchAlgorithm;
import org.haidash.aco.trash.model.SearchResult;

public class AntColony implements SearchAlgorithm {

	private final Logger logger = Logger.getLogger(AntColony.class);

	public static final double ALPHA = 0.1;
	public static final double BETA = 0.1;
	public static final double Q = 0.0001d;
	public static final double PHEROMONE_PERSISTENCE = 0.3d;
	public static final double INITIAL_PHEROMONES = 0.8d;

	public int[][] matrix;
	public int[] fuelLevel;

	public static int numNodes;
	public static int maxFuel;

	public static int numGeneration;
	public static int numAnts;

	public static int startNode;
	public static int targetNode;

	public volatile Pair<Double, Double>[][] globalPheromones;

	private int[] remainsFuel;

	public AntColony() {

	}

	public int getRemainsFuel(final int index) {
		return remainsFuel[index];
	}

	@SuppressWarnings("unchecked")
	private Pair<Double, Double>[][] initializeGlobalPheromones() {

		Pair<Double, Double>[][] result = new Pair[numNodes][numNodes];

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				result[i][j] = new Pair<Double, Double>(new Pair<Double, Double>(1.0, 1.0));
			}
		}

		return result;
	}

	@Override
	public void initializeValue(File file) throws AcoRuntimeException {

		try (Scanner text = new Scanner(new FileReader(file))) {

			numNodes = text.nextInt();

			fuelLevel = new int[numNodes];

			for (int i = 0; i < numNodes; i++) {
				fuelLevel[i] = text.nextInt();
			}

			final int numEdges = text.nextInt();

			matrix = new int[numNodes][numNodes];

			for (int i = 0; i < numNodes; i++) {
				for (int j = 0; j < numNodes; j++) {
					matrix[i][j] = -1;
				}
			}

			for (int i = 0; i < numEdges; i++) {
				final int start = text.nextInt();
				final int finish = text.nextInt();

				matrix[start][finish] = text.nextInt();
			}

			maxFuel = text.nextInt();

			startNode = text.nextInt();
			targetNode = text.nextInt();

		} catch (FileNotFoundException e) {
			throw new AcoRuntimeException("Can't find input file", e);
		}

		numAnts = numNodes;
		numGeneration = numNodes;

		globalPheromones = initializeGlobalPheromones();

		final FloydWarshall floydWarshall = new FloydWarshall(numNodes, matrix, fuelLevel, maxFuel, targetNode);
		this.remainsFuel = floydWarshall.getRemainsFuel();
	}

	@Override
	public SearchResult search() {

		logger.info("=========================================================================");
		logger.info("PROCESS START '" + startNode + "' -> '" + targetNode + "'...");
		logger.info("=========================================================================");

		SearchResult bestSearchResult = null;

		for (int i = 0; i < numGeneration; i++) {

			final Generation generation = new Generation(this, startNode);

			final SearchResult route = generation.start();

			if (route == null) {
				continue;
			}

			if (bestSearchResult == null || route.getTotalCost() < bestSearchResult.getTotalCost()) {
				bestSearchResult = route;
			}
		}

		logger.info("=========================================================================");
		logger.info("PROCESS FINISH:");

		if (bestSearchResult == null) {
			logger.info("Path not found");
		} else {
			logger.info("Best path: " + bestSearchResult.getTotalCost());
			logger.info(bestSearchResult.getVisited().toString());
		}

		logger.info("=========================================================================");

		return bestSearchResult;
	}

	public void updatePheromones(final Pair<Double, Double>[][] localPheromones) {

		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {

				final Pair<Double, Double> pheromon = globalPheromones[i][j];
				final Pair<Double, Double> localPheromon = localPheromones[i][j];

				final double pValue = pheromon.first + localPheromon.first;
				final double nValue = pheromon.second + localPheromon.second;

				globalPheromones[i][j] = new Pair<Double, Double>(pValue, nValue);

			}
		}

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
