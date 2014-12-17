package org.haidash.aco.a.multithread;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class HeuristicParametersTest extends AntColonyBaseTestUtil {

	/*
	 * Next 3 tests test how greedy the algorithm can be.
	 */

	@Test
	public void testLitleMoreGreedyAlgorithm() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.5d, 9.6d, 0.0001d, 0.3d, 0.8d);
		startProcess(ACO);
	}

	@Test
	public void testMoreGreedyAlgorithm() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.75d, 9.6d, 0.0001d, 0.3d, 0.8d);
		startProcess(ACO);
	}

	@Test
	public void testMuchMoreGreedyAlgorithm() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-1.0d, 9.6d, 0.0001d, 0.3d, 0.8d);
		startProcess(ACO);
	}

	/*
	 * Next 3 tests are using different rapid selection
	 */

	@Test
	public void testSlowRapidSelectionAlgorithm() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 9.6d, 0.0001d, 0.3d, 0.8d);
		startProcess(ACO);
	}

	@Test
	public void testNormalRapidSelectionAlgorithm() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 9.6d, 0.0001d, 0.3d, 0.8d);
		startProcess(ACO);
	}

	@Test
	public void testFastRapidSelectionAlgorithm() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 9.6d, 0.0001d, 0.3d, 0.8d);
		startProcess(ACO);
	}

	/*
	 * Next 3 methods are testing the pheromone persistence
	 */

	@Test
	public void testWithSmallInitialPheromones() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 9.6d, 0.0001d, 0.1d, 0.8d);
		startProcess(ACO);
	}

	@Test
	public void testWithLitleMoreInitialPheromones() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 9.6d, 0.0001d, 0.5d, 0.8d);
		startProcess(ACO);
	}

	@Test
	public void testWithLargeInitialPheromones() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 9.6d, 0.0001d, 1.0d, 0.8d);
		startProcess(ACO);
	}

	/*
	 * Next 3 methods are testing the initial pheromones
	 */

	@Test
	public void testWithSmallPheromonePersistence() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 5.0d, 0.0001d, 0.3d, 0.5d);
		startProcess(ACO);
	}

	@Test
	public void testWithLitleMorePheromonePersistence() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 10.0d, 0.0001d, 0.3d, 1.0d);
		startProcess(ACO);
	}

	@Test
	public void testWithLargePheromonePersistence() throws IOException {
		assertEquals(true, (Runtime.getRuntime().availableProcessors() >= 1));
		initializeInputFile();
		AntColony ACO = new AntColony(-0.2d, 15.0d, 0.0001d, 0.3d, 1.5d);
		startProcess(ACO);
	}
}
