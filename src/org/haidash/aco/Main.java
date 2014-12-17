package org.haidash.aco;

import java.io.File;

import org.haidash.aco.model.AcoRuntimeException;
import org.haidash.aco.solution.cycle.AntColony;

public class Main {

	public static final String INPUT = "files/input8.txt";

	public static void main(String[] args) {
		try {
			AntColony ac = new AntColony();
			ac.initializeValue(new File(INPUT));
			ac.search();
		} catch (AcoRuntimeException e) {
			e.printStackTrace();
			return;
		}

	}
}
