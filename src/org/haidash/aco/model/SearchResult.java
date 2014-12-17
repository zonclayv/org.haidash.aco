package org.haidash.aco.model;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {

	private List<Integer> spentFuelLevel;
	private List<Integer> visited;
	private int totalCost;

	public List<Integer> getSpentFuelLevel() {
		return spentFuelLevel;
	}

	public void setSpentFuelLevel(List<Integer> spentFuelLevel) {
		this.spentFuelLevel = spentFuelLevel;
	}

	public List<Integer> getVisited() {
		return visited;
	}

	public void setVisited(List<Integer> visited) {
		this.visited = visited;
	}

	public int getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(int totalCost) {
		this.totalCost = totalCost;
	}

	public SearchResult(final List<Integer> spentFuelLevel, final List<Integer> visited, final int totalCost) {
		this.spentFuelLevel = new ArrayList<Integer>(spentFuelLevel);
		this.visited = new ArrayList<Integer>(visited);
		this.totalCost = totalCost;
	}
}
