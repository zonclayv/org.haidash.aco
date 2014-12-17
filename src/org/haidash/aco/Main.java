package org.haidash.aco;

import java.io.File;
import java.io.IOException;

import org.haidash.aco.b.cycle.AntColony;

public class Main {

	public static final String INPUT = "files/input_26.txt";

	public static void main(String[] args) {
		try {
			AntColony ac = new AntColony();
			ac.initializeValue(new File(INPUT));
			ac.search();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
