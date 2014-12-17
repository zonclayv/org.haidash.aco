package org.haidash.aco.a.multithread;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.haidash.aco.model.Pair;

public class AntColony {

	static class Route {

		List<Integer> spentFuelLevel;
		List<Integer> visited;
		int totalCost;

		public Route(final List<Integer> spentFuelLevel, final List<Integer> visited, final int totalCost) {
			this.spentFuelLevel = new LinkedList<Integer>(spentFuelLevel);
			this.visited = new LinkedList<Integer>(visited);
			this.totalCost = totalCost;
		}
	}

	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	public static String INPUT = "files/input.txt";

	public static double ALPHA = 0.1;
	public static double BETA = 0.1;
	public static double Q = 0.0001d;
	public static double PHEROMONE_PERSISTENCE = 0.3d;
	public static double INITIAL_PHEROMONES = 0.8d;
	private static int NUM_GENERATIONS = 20;
	public static int NUM_ANTS = 1000;

	private static int POOL_SIZE = Runtime.getRuntime().availableProcessors();

	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(POOL_SIZE);

	private final ExecutorCompletionService<Route> antCompletionService = new ExecutorCompletionService<Route>(THREAD_POOL);

	public int[][] matrix;
	public int[] fuelLevel;

	public static int numberOfNodes;
	public static int maxFuel;
	public static int startNode;
	public static int targetNode;

	public volatile AtomicReference<Pair<Double, Double>>[][] globalPheromones;

	private List<Node> nodes;

	private final int[] remainsFuel;

	@SuppressWarnings("unchecked")
	public AntColony() throws IOException {

		readMatrixFromFile();
		initializeGlobalPheromones();

		final FloydWarshall floydWarshall = new FloydWarshall(numberOfNodes, matrix, fuelLevel, maxFuel, targetNode);

		int finish = targetNode;
		int from = 0;
		do {
			int to = floydWarshall.findPrevVertex(from, finish);

			globalPheromones[to][finish] = new AtomicReference<Pair<Double, Double>>(new Pair<Double, Double>(15.0, 1.0));

			finish = to;
		} while (finish != from);

		this.remainsFuel = floydWarshall.getRemainsFuel();
	}

	public AntColony(double alpha, double beta, double q, double pheromonePersistence, double initialPheromones) throws IOException {

		this();

		AntColony.ALPHA = alpha;
		AntColony.BETA = beta;
		AntColony.Q = q;
		AntColony.PHEROMONE_PERSISTENCE = pheromonePersistence;
		AntColony.INITIAL_PHEROMONES = initialPheromones;
	}

	/**
	 * @param poolSize
	 * @param numAgents
	 * @throws IOException
	 */
	public AntColony(final short poolSize, final int... numAgents) throws IOException {

		this();

		if (numAgents.length != 0 && numAgents[0] > 0) {
			AntColony.NUM_ANTS = numAgents[0];
		}

		AntColony.POOL_SIZE = poolSize;
	}

	public int getRemainsFuel(final int index) {
		return remainsFuel[index];
	}

	public void addParams(int number, Pair<Integer, Integer> pair) {
		Node node = nodes.get(number);
		node.setItem(pair);
	}

	@SuppressWarnings("unchecked")
	public void initializeGlobalPheromones() {

		globalPheromones = new AtomicReference[numberOfNodes][numberOfNodes];

		for (int i = 0; i < numberOfNodes; i++) {
			for (int j = 0; j < numberOfNodes; j++) {
				globalPheromones[i][j] = new AtomicReference<Pair<Double, Double>>(new Pair<Double, Double>(1.0, 1.0));
			}
		}
	}

	private void readMatrixFromFile() throws IOException {

		final Scanner text = new Scanner(new FileReader(INPUT));

		numberOfNodes = text.nextInt();

		nodes = new ArrayList<Node>();

		fuelLevel = new int[numberOfNodes];

		for (int i = 0; i < numberOfNodes; i++) {
			fuelLevel[i] = text.nextInt();
			nodes.add(new Node(i));
		}

		int numberOfEdges = text.nextInt();

		matrix = new int[numberOfNodes][numberOfNodes];

		for (int i = 0; i < numberOfNodes; i++) {
			for (int j = 0; j < numberOfNodes; j++) {
				matrix[i][j] = -1;
			}
		}

		for (int i = 0; i < numberOfEdges; i++) {
			int start = text.nextInt() - 1;
			int finish = text.nextInt() - 1;

			matrix[start][finish] = text.nextInt();
		}

		maxFuel = text.nextInt();

		startNode = text.nextInt();
		targetNode = text.nextInt();

		text.close();
	}

	final int start() throws InterruptedException, ExecutionException {

		logger.info("=========================================================================");
		logger.info("PROCESS START '" + startNode + "' -> '" + targetNode + "'...");
		logger.info("=========================================================================");

		Route bestRoute = null;
		List<Future<Route>> futures = new ArrayList<Future<Route>>();

		for (int generation = 0; generation < NUM_GENERATIONS; generation++) {

			Future<Route> newFuture = antCompletionService.submit(new Generation(this, generation, startNode));

			futures.add(newFuture);
		}

		for (Iterator<Future<Route>> iterator = futures.iterator(); iterator.hasNext();) {

			Future<Route> future = (Future<Route>) iterator.next();

			final Route route = future.get();

			if (route == null) {
				continue;
			}

			if (bestRoute == null || route.totalCost < bestRoute.totalCost) {
				bestRoute = route;
				iterator.remove();
			}
		}

		THREAD_POOL.shutdownNow();

		logger.info("=========================================================================");
		logger.info("PROCESS FINISH:");

		int totalCost = -1;

		if (bestRoute == null) {
			logger.info("Path not found");
		} else {
			logger.info("Best path: " + bestRoute.totalCost);
			logger.info(bestRoute.visited.toString());

			totalCost = bestRoute.totalCost;
		}

		logger.info("=========================================================================");

		for (Node node : nodes) {
			System.out.println(node);
		}

		return totalCost;

	}

	@SuppressWarnings("unchecked")
	public Pair<Double, Double>[][] initializeLocalPheromones() {

		int numberOfNodes = AntColony.numberOfNodes;

		Pair<Double, Double>[][] pheromones = new Pair[numberOfNodes][numberOfNodes];

		for (int i = 0; i < numberOfNodes; i++) {
			for (int j = 0; j < numberOfNodes; j++) {
				pheromones[i][j] = new Pair<Double, Double>(globalPheromones[i][j].get());
			}
		}

		return pheromones;
	}

	public void updateGlobalTrails(Pair<Double, Double>[][] localPheromones) {

		for (int i = 0; i < numberOfNodes; i++) {
			for (int j = 0; j < numberOfNodes; j++) {

				Pair<Double, Double> pheromon = globalPheromones[i][j].get();
				Pair<Double, Double> localPheromon = localPheromones[i][j];

				double pValue = pheromon.first + localPheromon.first;
				double nValue = pheromon.second + localPheromon.second;

				globalPheromones[i][j].compareAndSet(pheromon, new Pair<Double, Double>(pValue, nValue));

			}
		}

		for (int i = 0; i < numberOfNodes; i++) {
			for (int j = 0; j < numberOfNodes; j++) {

				Pair<Double, Double> pheromon = globalPheromones[i][j].get();

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

				globalPheromones[i][j].compareAndSet(pheromon, new Pair<Double, Double>(pValue, nValue));

			}
		}
	}
}
