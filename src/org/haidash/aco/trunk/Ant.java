package org.haidash.aco.trunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.haidash.aco.model.Pair;
import org.haidash.aco.model.SearchResult;

public class Ant {

	private final Logger logger = Logger.getLogger(AntColony.class);

	private Generation generation;

	private int totalCost = 0;
	private int fuelBalance = 0;

	private boolean outOfFuel = false;
	private int start;
	private List<Integer> spentFuelLevel;
	private List<Integer> visited;
	private int[] tempFuelLevel;

	private final Random random = new Random(System.nanoTime());

	public Ant(Generation generation, int start) {
		this.generation = generation;
		this.start = start;

		this.tempFuelLevel = generation.getFuelLevels();

		this.spentFuelLevel = new ArrayList<Integer>();
		this.visited = new ArrayList<Integer>();
		visited.add(start);
	}

	public final SearchResult search() {

		int next = start;

		while (next != AntColony.targetNode && !outOfFuel && next != -1) {
			next = selectNextNode(next);
		}

		generation.updatePheromones(outOfFuel, visited, totalCost);

		SearchResult searchResult = null;

		if (!outOfFuel) {
			searchResult = new SearchResult(spentFuelLevel, visited, totalCost);

			logger.info("Ant find path " + searchResult.getTotalCost());
		}

		return searchResult;
	}

	private final int selectNextNode(int currentNode) {

		double sum = -1.0;

		List<Pair<Integer, Double>> probabilities = new ArrayList<Pair<Integer, Double>>();
		List<Integer> usedFuels = new ArrayList<Integer>();

		for (int nextNode = 0; nextNode < AntColony.numNodes; nextNode++) {

			int fuelCost = generation.getFuelCost(currentNode, nextNode);

			if (fuelCost <= 0) {
				continue;
			}

			int availableFuel = 0;
			int fuelInNode = tempFuelLevel[currentNode];

			if ((fuelBalance + fuelInNode) > AntColony.maxFuel) {
				availableFuel = AntColony.maxFuel;
			} else {
				availableFuel = fuelBalance + fuelInNode;
			}

			if (availableFuel < fuelCost) {
				continue;
			}

			int availableFuelInCurrentNode = availableFuel - fuelBalance;
			int usedFuel = getFuel(availableFuelInCurrentNode, fuelCost);

			int visitCount = generation.getVisitCount(currentNode, nextNode);
			double etaVisits = visitCount == 0 || visitCount == 1 ? 1 : 1 - 1 / (visitCount);

			if (visitCount == 0) {

				int nextInt = random.nextInt(1);

				if (nextInt == 1) {
					goToNextNode(currentNode, usedFuel, nextNode);

					return nextNode;
				}
			}

			double etaCost = fuelCost == 1 ? 1 : 1 - 1 / (double) (fuelCost);
			double etaRemaning = 0;
			double k = availableFuel - fuelCost + generation.getRemainsFuel(nextNode);

			if (k > 0) {
				etaRemaning = k == 1 ? 1.1 : 1 + 1 - 1 / (double) (k);
			} else if (k < 0) {
				etaRemaning = k == -1 ? 0.9 : 1 - 1 / (double) (Math.abs(k));
			} else {
				etaRemaning = 2;
			}

			double eta = 0.1 * etaCost + 0.5 * etaRemaning * 0.4 * etaVisits;
			double tau = generation.readTau(currentNode, nextNode);

			if (sum == -1.0) {
				sum = calculateProbability(currentNode, availableFuel, usedFuel);
			}

			double probability = 100 * Math.pow(tau, AntColony.ALPHA) * (Math.pow(eta, AntColony.BETA)) / sum;

			probabilities.add(new Pair<Integer, Double>(nextNode, probability));
			usedFuels.add(usedFuel);
		}

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

					int nextNode = pair.first;

					goToNextNode(currentNode, usedFuels.get(i), nextNode);

					return nextNode;
				}
			}
		}

		probabilities.clear();
		usedFuels.clear();

		return -1;

	}

	private void goToNextNode(int currentNode, int usedFuel, int next) {

		// 1
		visited.add(next);
		int remainingFuelInCurrentNode = tempFuelLevel[currentNode] - usedFuel;

		spentFuelLevel.add(usedFuel);
		tempFuelLevel[currentNode] = remainingFuelInCurrentNode;

		// 2
		fuelBalance += (usedFuel - generation.getFuelCost(currentNode, next));
		totalCost += usedFuel;

		generation.addVisit(currentNode, next);
	}

	private int getFuel(int availableFuel, int costToMove) {
		return availableFuel;
	}

	private double calculateProbability(int currentNode, int availableFuel, int usedFuel) {
		double sum = 0.0;

		for (int nextNode = 0; nextNode < AntColony.numNodes; nextNode++) {

			double fuelCost = generation.getFuelCost(currentNode, nextNode);

			if (fuelCost <= 0) {
				continue;
			}

			if (availableFuel >= fuelCost) {
				int visitCount = generation.getVisitCount(currentNode, nextNode);
				double etaVisits = visitCount == 0 || visitCount == 1 ? 1 : 1 - 1 / (visitCount);

				double etaCost = fuelCost == 1 ? 1 : 1 - 1 / (double) (fuelCost);
				double etaRemaning = 0;
				double k = availableFuel - fuelCost + generation.getRemainsFuel(nextNode);

				if (k > 0) {
					etaRemaning = k == 1 ? 1.1 : 1 + 1 - 1 / (double) (k);
				} else if (k < 0) {
					etaRemaning = k == -1 ? 0.9 : 1 - 1 / (double) (Math.abs(k));
				} else {
					etaRemaning = 2;
				}

				double eta = 0.1 * etaCost + 0.5 * etaRemaning * 0.4 * etaVisits;
				double tau = generation.readTau(currentNode, nextNode);

				sum += Math.pow(tau, AntColony.ALPHA) * (Math.pow(eta, AntColony.BETA));
			}

		}
		return sum;
	}
}
