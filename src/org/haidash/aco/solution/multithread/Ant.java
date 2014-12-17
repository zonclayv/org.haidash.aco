package org.haidash.aco.solution.multithread;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.haidash.aco.model.Pair;
import org.haidash.aco.solution.multithread.AntColony.Route;

public class Ant {

	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	private final AntColony instance;

	private int totalCost = 0;
	private int fuel = 0;
	private boolean outOfFuel = false;
	private Generation generation;

	private final int start;
	private final List<Integer> spentFuelLevel;
	private final List<Integer> visited;
	private final int[] tempFuelLevel;

	private final Random random = new Random(System.nanoTime());

	private int numberAnt;

	public Ant(AntColony instance, Generation generation, int numberAnt, int start) {
		this.instance = instance;
		this.spentFuelLevel = new LinkedList<>();
		this.tempFuelLevel = new int[instance.matrix.length];

		for (int i = 0; i < instance.fuelLevel.length; i++) {
			tempFuelLevel[i] = instance.fuelLevel[i];
		}

		this.generation = generation;
		this.start = start;
		this.numberAnt = numberAnt;
		this.visited = new LinkedList<>();
		visited.add(start);
	}

	public final Route call() {

		int lastNode = start;
		int next = start;

		while (lastNode != AntColony.targetNode && lastNode != -1) {
			next = selectNextNode(lastNode);
			lastNode = next;
		}

		generation.updateLocalTrails(outOfFuel, visited, totalCost);

		Route route = null;

		if (!outOfFuel) {
			route = new Route(spentFuelLevel, visited, totalCost);

			logger.info("Ant " + generation.numberGeneration + "_" + numberAnt + " -> find path " + route.totalCost);
		}

		return route;
	}

	private final int selectNextNode(int currentNode) {

		double sum = -1.0;

		List<Pair<Integer, Double>> probabilities = new ArrayList<Pair<Integer, Double>>();
		List<Integer> usedFuels = new ArrayList<Integer>();

		for (int nextNode = 0; nextNode < AntColony.numberOfNodes; nextNode++) {

			int fuelCost = instance.matrix[currentNode][nextNode];

			if (fuelCost <= 0) {
				continue;
			}

			int availableFuel = 0;
			int fuelInNode = tempFuelLevel[currentNode];

			if ((fuel + fuelInNode) > AntColony.maxFuel) {
				availableFuel = AntColony.maxFuel;
			} else {
				availableFuel = fuel + fuelInNode;
			}

			if (availableFuel < fuelCost) {
				continue;
			}

			int availableFuelInCurrentNode = availableFuel - fuel;
			int usedFuel = getFuel(availableFuelInCurrentNode, fuelCost);

			double etaCost = 1 / (double) (fuelCost);
			double etaRemaning = 0;
			double k = availableFuel - fuelCost + instance.getRemainsFuel(nextNode);

			if (k == 0) {
				etaRemaning = 0.1;
			} else {
				etaRemaning = 0.1 / k;
			}

			int visitCount = generation.getVisitCount(currentNode, nextNode);

			double eta = visitCount == 0 ? 1 : 0.1 / (visitCount);
			double tau = generation.readTau(currentNode, nextNode);

			if (sum == -1.0) {
				sum = calculateProbability(currentNode, availableFuel, usedFuel);
			}

			double probability = 100 * Math.pow(tau, AntColony.ALPHA) * (Math.pow(eta, AntColony.BETA)) / sum;

			probabilities.add(new Pair<Integer, Double>(nextNode, Math.abs(probability)));
			usedFuels.add(usedFuel);
		}

		int nextNode = -1;

		if (probabilities.size() == 0) {
			outOfFuel = true;
		} else {
			Double accumulatedProbabilities = 0.0;

			for (Pair<Integer, Double> pair : probabilities) {
				accumulatedProbabilities += pair.second;
			}

			final int r = random.nextInt(accumulatedProbabilities.intValue());

			accumulatedProbabilities = 0.0;

			for (int i = 0; i < probabilities.size(); i++) {
				Pair<Integer, Double> pair = probabilities.get(i);

				accumulatedProbabilities += pair.second;

				if (accumulatedProbabilities >= r) {

					int next = pair.first;
					int usedFuel = usedFuels.get(i);

					// 1
					visited.add(next);
					int remainingFuelInCurrentNode = tempFuelLevel[currentNode] - usedFuel;

					spentFuelLevel.add(usedFuel);
					tempFuelLevel[currentNode] = remainingFuelInCurrentNode;

					// 2
					fuel += (usedFuel - instance.matrix[currentNode][next]);
					totalCost += usedFuel;

					generation.addVisit(currentNode, next);

					// // 3
					// logger.info("Ant " + Thread.currentThread().getId()
					// + "-> select next node " + next);

					instance.addParams(next, new Pair<Integer, Integer>(fuel, totalCost));

					nextNode = next;
					break;
				}
			}

		}

		probabilities.clear();
		usedFuels.clear();

		return nextNode;

	}

	private int getFuel(int availableFuel, int costToMove) {

		return availableFuel;
	}

	private double calculateProbability(int currentNode, int availableFuel, int usedFuel) {
		double sum = 0.0;

		for (int nextNode = 0; nextNode < AntColony.numberOfNodes; nextNode++) {

			double fuelCost = instance.matrix[currentNode][nextNode];

			if (fuelCost <= 0) {
				continue;
			}

			if (availableFuel >= fuelCost) {
				double etaCost = 1 / (double) (fuelCost);
				double etaRemaning = 0;
				double k = availableFuel - fuelCost + instance.getRemainsFuel(nextNode);

				if (k == 0) {
					etaRemaning = 0.1;
				} else {
					etaRemaning = 0.1 / k;
				}

				int visitCount = generation.getVisitCount(currentNode, nextNode);

				double eta = visitCount == 0 ? 1 : 0.1 / visitCount;

				double tau = generation.readTau(currentNode, nextNode);

				sum += Math.pow(tau, AntColony.ALPHA) * (Math.pow(eta, AntColony.BETA));
			}

		}
		return sum;
	}
}
