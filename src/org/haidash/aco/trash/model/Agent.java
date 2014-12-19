package org.haidash.aco.trash.model;

public interface Agent {

	public void search();
	public void updatePheromones(Pair<Double, Double>[][] pheromones);
}
