package org.haidash.aco.solution.multithread;

import java.util.concurrent.ExecutionException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class AntColonyBaseTestUtil {

	protected Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	protected AntColonyBaseTestUtil() {
		BasicConfigurator.configure();
		logger.setLevel(Level.INFO);
	}

	protected void initializeInputFile(String... tspFileName) {
		if (tspFileName.length != 0 && tspFileName[0].length() != 0) {
			AntColony.INPUT = "files/" + tspFileName[0];
			logger.info("Using input file" + tspFileName[0]);
		}
	}

	protected void startProcess(AntColony antColonyOptimization) {
		long start = System.currentTimeMillis();
		int result;
		try {
			result = antColonyOptimization.start();
			logger.info("Took: " + (System.currentTimeMillis() - start) + " ms!");
			if (result > 0) {
				logger.info("Result was: " + result);
			}
		} catch (InterruptedException ie) {
			logger.warn("Interrupted exception, error message: " + ie.getMessage());
		} catch (ExecutionException ee) {
			logger.warn("ExecutionException, error message: " + ee.getMessage());
		}
	}
}
