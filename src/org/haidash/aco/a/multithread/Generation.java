package org.haidash.aco.a.multithread;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.haidash.aco.a.multithread.AntColony.Route;
import org.haidash.aco.model.Pair;

public class Generation implements Callable<Route> {

	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	private final AntColony instance;
	private final int start;
	public final int numberGeneration;

	private volatile Pair<Double, Double>[][] pheromones;
	private volatile AtomicInteger[][] nodeVisits;

	public Generation(AntColony instance, int numberGeneration, int start) {
		this.instance = instance;
		this.start = start;
		this.numberGeneration = numberGeneration;

		nodeVisits = initMatrix();
	}

	private AtomicInteger[][] initMatrix() {

		AtomicInteger[][] matrix = new AtomicInteger[AntColony.numberOfNodes][AntColony.numberOfNodes];

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				matrix[i][j] = new AtomicInteger(0);

			}

		}
		return matrix;
	}

	public int getVisitCount(int from, int to) {
		return nodeVisits[from][to].get();
	}

	public void addVisit(int from, int to) {
		nodeVisits[from][to].incrementAndGet();

	}

	@Override
	public Route call() throws Exception {

		logger.info("Generation " + numberGeneration + " -> start");

		pheromones = instance.initializeLocalPheromones();

		Route bestRoute = null;

		for (int antNumber = 0; antNumber < AntColony.NUM_ANTS; antNumber++) {

			Route route = new Ant(instance, this, antNumber, start).call();

			if (route == null) {
				continue;
			}

			if (bestRoute == null || route.totalCost < bestRoute.totalCost) {
				bestRoute = route;
			}
		}

		instance.updateGlobalTrails(pheromones);

		logger.info("Generation " + numberGeneration + " -> finish");

		return bestRoute;
	}

	final double readTau(final int x, final int y) {
		Pair<Double, Double> pair = pheromones[x][y];
		return pair.first / pair.second;
	}

	public void updateLocalTrails(final boolean outOfFuel, final List<Integer> visited, final int totalCost) {

		int first = visited.get(0);
		double deltaTau = 0;

		if (totalCost != 0) {
			deltaTau = AntColony.Q / totalCost;
		}

		for (int i = 1; i < visited.size(); i++) {

			Integer second = visited.get(i);

			Pair<Double, Double> pairPheromones = pheromones[first][second];

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
