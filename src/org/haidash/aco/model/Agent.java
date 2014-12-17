package org.haidash.aco.model;

public interface Agent {

	public SearchResult start();

	public void updatePheromones(Pair<Double, Double>[][] pheromones);
}
