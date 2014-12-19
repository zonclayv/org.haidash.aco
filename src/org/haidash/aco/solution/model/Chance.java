package org.haidash.aco.solution.model;

public class Chance {

	private final int node;
	private final int fuel;
	private final double value;

	public Chance(final int node, final int fuel, final double value) {
		this.node = node;
		this.fuel = fuel;
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		final Chance other = (Chance) obj;

		if (fuel != other.fuel) {
			return false;
		}

		if (node != other.node) {
			return false;
		}

		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
			return false;
		}

		return true;
	}

	public int getFuel() {
		return fuel;
	}

	public int getNode() {
		return node;
	}

	public double getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime * result + fuel;
		result = prime * result + node;

		long temp;

		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ temp >>> 32);

		return result;
	}

}
