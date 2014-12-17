package org.haidash.aco;

import java.io.IOException;

import org.haidash.aco.b.cycle.AntColony;

public class Main {

	public static void main(String[] args) {
		try {
			AntColony ac = new AntColony();
			ac.search();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
