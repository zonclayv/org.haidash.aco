package org.haidash.aco.b.cycle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.haidash.aco.model.Agent;
import org.haidash.aco.model.Pair;
import org.haidash.aco.model.SearchResult;

/**
 * @author Haidash Aleh
 */
public class Ant implements Agent {

	private final static Logger LOGGER = Logger.getLogger(Ant.class);

	// global
	private final int targetNode;
	private final int numNodes;
	private final int maxFuel;
	private final int[][] nodesMap;
	private final int[] fuelLevels;
	private final int[] remainingsFuel;
	private final Set<List<Integer>> badPaths;
	private final Map<Integer, Cycle> cycles;

	// local
	private final int[][] visitsCount;
	private final Pair<Double, Double>[][] localPheromones;

	// ant
	private int node;
	private int fuelBalance = 0;
	private int totalCost = 0;

	private boolean outOfFuel = false;

	private final List<Integer> visited;
	private final List<Integer> spentFuelLevel;
	private final int[] tempFuelLevel;

	private final Random random = new Random(System.nanoTime());

	/**
	 * @param startNode
	 *        начальная вершина
	 * @param targetNode
	 *        конечная вершина
	 * @param numNodes
	 *        количество вершин
	 * @param maxFuel
	 *        максимальный объём топлива, переносимый агентом
	 * @param nodesMap
	 *        матрица стоимостей перемещения из вершины i в вершину j
	 * @param fuelLevels
	 *        начальный уровень топлива в вершинах
	 * @param remainingsFuel
	 *        массив минимальных остатков топлива при движении по кратчайшему пути
	 * @param badPaths
	 *        список путей на котором закончилось топливо
	 * @param cycles
	 *        найденные циклы(вершина-вход, цикл)
	 * @param visitsCount
	 *        матрица количества перемещений по рёбрам
	 * @param localPheromones
	 *        матрица локального уровня феромона
	 */
	public Ant(final int startNode,
			final int targetNode,
			final int numNodes,
			final int maxFuel,
			final int[][] nodesMap,
			final int[] fuelLevels,
			final int[] remainingsFuel,
			final Set<List<Integer>> badPaths,
			final Map<Integer, Cycle> cycles,
			final int[][] visitsCount,
			final Pair<Double, Double>[][] localPheromones) {

		// global
		this.node = startNode;
		this.targetNode = targetNode;
		this.numNodes = numNodes;
		this.maxFuel = maxFuel;
		this.nodesMap = nodesMap;
		this.fuelLevels = fuelLevels;

		this.remainingsFuel = remainingsFuel;

		this.badPaths = badPaths;
		this.cycles = cycles;

		// local
		this.visitsCount = visitsCount;
		this.localPheromones = localPheromones;

		// ant
		this.tempFuelLevel = fuelLevels.clone();
		this.spentFuelLevel = new ArrayList<Integer>();
		this.visited = new ArrayList<Integer>();

		visited.add(startNode);
	}

	private void addCycle(final Cycle cycle) {
		final int node = cycle.getStartNode();

		if (!cycles.containsKey(node)) {
			cycles.put(node, cycle);
		} else {
			final Cycle oldCycle = cycles.get(node);
			if (oldCycle.getFuel() < cycle.getFuel()
					|| oldCycle.getFuel() == cycle.getFuel()
					&& oldCycle.getVisited().size() > cycle.getVisited().size()) {
				cycles.remove(oldCycle);
				cycles.put(node, cycle);
			}
		}
	}

	private void addVisit(final int from, final int to) {
		final int count = visitsCount[from][to];
		visitsCount[from][to] = count + 1;

	}

	private boolean applyCycle(final Cycle cycle) {
		final List<Integer> visitedNodes = cycle.getVisited();
		int currentNode = visitedNodes.get(0);

		for (int i = 1; i < visitedNodes.size(); i++) {
			final int nextNode = visitedNodes.get(i);

			final int fuelCost = nodesMap[currentNode][nextNode];

			final int availableFuel = getAvailableFuel(currentNode);

			if (availableFuel < fuelCost) {
				outOfFuel = true;
				cycles.remove(cycle.getStartNode());
				return false;
			}

			final int usedFuel = availableFuel - fuelBalance;

			goToNextNode(currentNode, nextNode, usedFuel);
			currentNode = nextNode;
		}

		return true;
	}

	private boolean applyNextNode(final int currentNode, final int next, int usedFuel, final Cycle cycle) {

		int futureFuelBalance = fuelBalance + usedFuel - nodesMap[currentNode][next];

		if (futureFuelBalance < 0) {
			if (applyCycle(cycle)) {
				usedFuel = getAvailableFuel(currentNode) - nodesMap[currentNode][next];
				futureFuelBalance = fuelBalance - nodesMap[currentNode][next] + usedFuel;

				if (usedFuel < 0) {
					outOfFuel = true;
					return false;
				}
			} else {
				return false;
			}
		}

		// 1
		visited.add(next);
		final int remainingFuelInCurrentNode = tempFuelLevel[currentNode] - usedFuel;

		spentFuelLevel.add(usedFuel);
		tempFuelLevel[currentNode] = remainingFuelInCurrentNode;

		// 2
		fuelBalance = futureFuelBalance;
		totalCost += usedFuel;

		addVisit(currentNode, next);

		return true;
	}

	private int countNumberEqual(final List<Integer> itemList, final int item) {
		int count = 0;
		for (int i = 1; i < itemList.size(); i++) {
			final int it = itemList.get(i);

			if (it == item) {
				count++;
			}
		}

		return count;
	}

	private Cycle createCycle(final int startNode) {

		final Cycle cycle = new Cycle();
		cycle.setStartNode(startNode);

		int fuel = 0;

		final List<Integer> visitedVertices = cycle.getVisited();

		for (int i = visited.indexOf(startNode); i < visited.size(); i++) {
			final int node = visited.get(i);

			if (!visitedVertices.contains(node)) {
				fuel += fuelLevels[node];
			}

			visitedVertices.add(node);

			if (i != visited.size() - 1) {

				final int nextVertex = visited.get(i + 1);
				fuel -= nodesMap[node][nextVertex];
			}
		}

		cycle.setFuel(fuel);

		return cycle;
	}

	private void fillProbabilities(final int currentNode, final Cycle cycle, final Map<Pair<Integer, Double>, Integer> probabilities) {

		double sum = -1.0;

		for (int nextNode = 0; nextNode < numNodes; nextNode++) {

			final int fuelCost = nodesMap[currentNode][nextNode];
			final int visitCount = visitsCount[currentNode][nextNode];

			if (fuelCost <= 0 || countNumberEqual(visited, nextNode) >= 2) {
				continue;
			}

			final int availableFuel = getAvailableFuel(currentNode);

			if (availableFuel < fuelCost) {
				if (cycle == null) {
					continue;
				}

				final int fuelAfterCycle = getFuelAfterCycle(currentNode, availableFuel, cycle.getFuel());

				if (fuelAfterCycle < fuelCost) {
					continue;
				}

				if (isBadPath(visited, cycle.getVisited(), nextNode)) {
					continue;
				}
			} else {
				if (isBadPath(visited, nextNode)) {
					continue;
				}
			}

			final int usedFuel = availableFuel - fuelBalance;

			final double etaVisits = visitCount == 0 || visitCount == 1 ? 1 : 1 - 1 / visitCount;
			final double etaCost = fuelCost == 1 ? 1 : 1 - 1 / (double) fuelCost;

			double etaRemaning = 0;

			final double k = availableFuel - fuelCost + remainingsFuel[nextNode];

			if (k > 0) {
				etaRemaning = k == 1 ? 1.1 : 1 + 1 - 1 / k;
			} else if (k < 0) {
				etaRemaning = k == -1 ? 0.9 : 1 - 1 / Math.abs(k);
			} else {
				etaRemaning = 2;
			}

			final double eta = 0.1 * etaCost + 0.5 * etaRemaning * 0.4 * etaVisits;
			final double tau = getTau(currentNode, nextNode);

			if (sum == -1.0) {
				sum = getSumProbabilities(currentNode, cycle, availableFuel, usedFuel);
			}

			final double probability = 100 * Math.pow(tau, AntColony.ALPHA) * Math.pow(eta, AntColony.BETA) / sum;

			probabilities.put(new Pair<Integer, Double>(nextNode, probability), usedFuel);
		}
	}

	private int getAvailableFuel(final int node) {
		final int fuelInNode = tempFuelLevel[node];

		if (fuelBalance + fuelInNode > maxFuel) {
			return maxFuel;
		} else {
			return fuelBalance + fuelInNode;
		}
	}

	private int getFuelAfterCycle(final int currentNode, final int currentAvailableFuel, final int fuelInCycle) {
		final int fuelInNode = tempFuelLevel[currentNode];
		int tempFuel = 0;
		if (fuelBalance + fuelInNode > maxFuel) {
			tempFuel = maxFuel - currentAvailableFuel;
		} else {
			tempFuel = fuelBalance + fuelInNode - currentAvailableFuel;
		}

		return tempFuel + fuelInCycle;
	}

	private double getSumProbabilities(final int currentNode, final Cycle cycle, final int availableFuel, final int usedFuel) {
		double sum = 0.0;

		for (int nextNode = 0; nextNode < numNodes; nextNode++) {

			final double fuelCost = nodesMap[currentNode][nextNode];
			final int visitCount = visitsCount[currentNode][nextNode];

			if (fuelCost <= 0 || countNumberEqual(visited, nextNode) >= 2) {
				continue;
			}

			if (availableFuel < fuelCost) {
				if (cycle == null) {
					continue;
				}

				final int fuelAfterCycle = getFuelAfterCycle(currentNode, availableFuel, cycle.getFuel());

				if (fuelAfterCycle < fuelCost) {
					continue;
				}

				if (isBadPath(visited, cycle.getVisited(), nextNode)) {
					continue;
				}
			} else {
				if (isBadPath(visited, nextNode)) {
					continue;
				}
			}

			final double etaVisits = visitCount == 0 || visitCount == 1 ? 1 : 1 - 1 / visitCount;
			final double etaCost = fuelCost == 1 ? 1 : 1 - 1 / fuelCost;

			double etaRemaning = 0;

			final double k = availableFuel - fuelCost + remainingsFuel[nextNode];

			if (k > 0) {
				etaRemaning = k == 1 ? 1.1 : 1 + 1 - 1 / k;
			} else if (k < 0) {
				etaRemaning = k == -1 ? 0.9 : 1 - 1 / Math.abs(k);
			} else {
				etaRemaning = 2;
			}

			final double eta = 0.1 * etaCost + 0.5 * etaRemaning * 0.4 * etaVisits;
			final double tau = getTau(currentNode, nextNode);

			sum += Math.pow(tau, AntColony.ALPHA) * Math.pow(eta, AntColony.BETA);

		}
		return sum;
	}

	private final double getTau(final int x, final int y) {
		final Pair<Double, Double> pair = localPheromones[x][y];
		return pair.first / pair.second;
	}

	private void goToNextNode(final int currentNode, final int next, final int usedFuel) {

		// 1
		visited.add(next);
		final int remainingFuelInCurrentNode = tempFuelLevel[currentNode] - usedFuel;

		spentFuelLevel.add(usedFuel);
		tempFuelLevel[currentNode] = remainingFuelInCurrentNode;

		// 2
		fuelBalance += usedFuel - nodesMap[currentNode][next];
		totalCost += usedFuel;

		addVisit(currentNode, next);
	}

	private boolean isBadPath(final List<Integer> visits, final int nextNode) {
		final List<Integer> nodes = new ArrayList<Integer>(visits);
		nodes.add(nextNode);

		if (badPaths.contains(nodes)) {
			return true;
		}

		return false;
	}

	private boolean isBadPath(final List<Integer> visits, final List<Integer> cycleNodes, final int nextNode) {
		final List<Integer> nodes = new ArrayList<Integer>(visits);
		nodes.remove(nodes.size() - 1);
		nodes.addAll(cycleNodes);
		nodes.add(nextNode);

		if (badPaths.contains(nodes)) {
			return true;
		}

		return false;
	}

	private final int selectNextNode(final int currentNode) {

		final Map<Pair<Integer, Double>, Integer> probabilities = new HashMap<Pair<Integer, Double>, Integer>();
		final Cycle cycle = cycles.get(currentNode);

		fillProbabilities(currentNode, cycle, probabilities);

		if (probabilities.size() == 0 || outOfFuel) {
			outOfFuel = true;

			return -1;
		}

		Double rouletteProbabilities = 0.0;

		for (final Pair<Integer, Double> pair : probabilities.keySet()) {
			rouletteProbabilities += pair.second;
		}

		final int r = random.nextInt(rouletteProbabilities.intValue());

		rouletteProbabilities = 0.0;

		for (final Entry<Pair<Integer, Double>, Integer> entry : probabilities.entrySet()) {

			final Pair<Integer, Double> pair = entry.getKey();
			final int usedFuels = entry.getValue();

			rouletteProbabilities += pair.second;

			if (rouletteProbabilities < r) {
				continue;
			}

			final int nextNode = pair.first;

			if (!applyNextNode(currentNode, nextNode, usedFuels, cycle)) {
				return -1;
			}

			final int indexOf = visited.indexOf(nextNode);
			final int lastIndexOf = visited.lastIndexOf(nextNode);

			if (indexOf != lastIndexOf && indexOf != -1) {
				final Cycle newCycle = createCycle(nextNode);
				addCycle(newCycle);
			}

			return nextNode;
		}

		return -1;
	}

	@Override
	public final SearchResult start() {

		while (node != targetNode && !outOfFuel && node != -1) {
			node = selectNextNode(node);
		}

		SearchResult searchResult = null;

		if (!outOfFuel) {
			searchResult = new SearchResult(spentFuelLevel, visited, totalCost);
			LOGGER.info("New path " + searchResult.getTotalCost() + " " + visited.toString());
		} else {
			badPaths.add(visited);
			LOGGER.info("Out fuel " + visited.toString());
		}

		return searchResult;
	}

	@Override
	public void updatePheromones(Pair<Double, Double>[][] globalPheromones) {

		int first = visited.get(0);
		double deltaTau = 0;

		if (totalCost != 0) {
			deltaTau = AntColony.Q / totalCost;
		}

		for (int i = 1; i < visited.size(); i++) {

			final Integer second = visited.get(i);

			final Pair<Double, Double> pairPheromones = globalPheromones[first][second];

			double pValue = pairPheromones.first;
			double nValue = pairPheromones.second;

			if (!outOfFuel) {
				pValue += deltaTau;
			} else {
				nValue += deltaTau;
			}

			globalPheromones[first][second] = new Pair<Double, Double>(pValue, nValue);

			first = second;
		}
	}
}
