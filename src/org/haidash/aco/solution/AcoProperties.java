package org.haidash.aco.solution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import org.haidash.aco.solution.model.AcoRuntimeException;

public class AcoProperties {

	private static AcoProperties instance;

	public static AcoProperties getInstance() {

		if (instance == null) {
			instance = new AcoProperties();
		}

		return instance;
	}


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
	private int[] remainsFuel;

	private AcoProperties() {
	}

	public int[] getFuelLevels() {
		return fuelLevels;
	}

	public int getMaxFuel() {
		return maxFuel;
	}

	public int[][] getNodesMap() {
		return nodesMap;
	}

	public int getNumAnts() {
		return numAnts;
	}

	public int getNumGeneration() {
		return numGeneration;
	}

	public int getNumNodes() {
		return numNodes;
	}

	public int[] getRemainsFuel() {
		return remainsFuel;
	}

	public int getStartNode() {
		return startNode;
	}

	public int getTargetNode() {
		return targetNode;
	}

	public void initializeValue(final File file) throws AcoRuntimeException {

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

		} catch (final FileNotFoundException e) {
			throw new AcoRuntimeException("Can't find input file", e);
		}

		numAnts = numNodes / 2;
		numGeneration = numNodes / 2;

		remainsFuel = FloydWarshall.getRemainsFuel(numNodes, nodesMap, fuelLevels, maxFuel, targetNode);
	}

}
