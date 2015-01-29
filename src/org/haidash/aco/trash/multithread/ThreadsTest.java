package org.haidash.aco.trash.multithread;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ThreadsTest extends AntColonyBaseTestUtil {

	@Test
	public void testingWithSingleThread() throws IOException {
		assertTrue(Runtime.getRuntime().availableProcessors() >= 1);

		initializeInputFile();
		AntColony antColonyOptimization = new AntColony((short) 1);
		startProcess(antColonyOptimization);

	}

	@Test
	public void testingWithTwoThreads() throws IOException {
		assertTrue(Runtime.getRuntime().availableProcessors() >= 2);

		initializeInputFile();
		AntColony antColonyOptimization = new AntColony((short) 2);
		startProcess(antColonyOptimization);
	}
}
