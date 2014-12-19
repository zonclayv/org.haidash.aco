package org.haidash.aco.solution.model;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

	private List<Integer> spentFuelLevel;
	private List<Integer> visited;
	private int totalCost;

	public SearchResult(final List<Integer> spentFuelLevel, final List<Integer> visited, final int totalCost) {
		this.spentFuelLevel = new ArrayList<Integer>(spentFuelLevel);
		this.visited = new ArrayList<Integer>(visited);
		this.totalCost = totalCost;
	}

	public List<Integer> getSpentFuelLevel() {
		return spentFuelLevel;
	}

	public int getTotalCost() {
		return totalCost;
	}

	public List<Integer> getVisited() {
		return visited;
	}

	public void setSpentFuelLevel(final List<Integer> spentFuelLevel) {
		this.spentFuelLevel = spentFuelLevel;
	}

	public void setTotalCost(final int totalCost) {
		this.totalCost = totalCost;
	}

	public void setVisited(final List<Integer> visited) {
		this.visited = visited;
	}
}
