package org.haidash.aco;

import java.io.File;

import org.haidash.aco.model.AcoRuntimeException;
import org.haidash.aco.solution.AcoProperties;
import org.haidash.aco.solution.AntColony;

public class Main {

	public static final String INPUT = "files/input_26.txt";

	public static void main(String[] args) {

		final AcoProperties properties = AcoProperties.getInstance();

		try {
			properties.initializeValue(new File(INPUT));
		} catch (AcoRuntimeException e) {
			e.printStackTrace();
			return;
		}

		AntColony ac = new AntColony();
		ac.start();

	}
}
