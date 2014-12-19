package org.haidash.aco.trash.model;

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

	public SearchResult(SearchResult newResult) {
		this.spentFuelLevel = new ArrayList<Integer>(newResult.getSpentFuelLevel());
		this.visited = new ArrayList<Integer>(newResult.getVisited());
		this.totalCost = newResult.getTotalCost();
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

	public void setSpentFuelLevel(List<Integer> spentFuelLevel) {
		this.spentFuelLevel = spentFuelLevel;
	}

	public void setTotalCost(int totalCost) {
		this.totalCost = totalCost;
	}
	
	public void setVisited(List<Integer> visited) {
		this.visited = visited;
	}
}
