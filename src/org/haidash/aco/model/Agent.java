package org.haidash.aco.model;

public interface Agent {

	public SearchResult search();

	public void updatePheromones(Pair<Double, Double>[][] pheromones);
}
