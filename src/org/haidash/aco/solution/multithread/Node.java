package org.haidash.aco.solution.multithread;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.haidash.aco.model.Pair;

public class Node {

	private final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (number != other.number)
			return false;
		return true;
	}

	private Set<Pair<Integer, Integer>> list;

	public Node(int number) {
		this.number = number;
		list = new HashSet<Pair<Integer, Integer>>();
	}

	private int number;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setItem(Pair<Integer, Integer> pair) {
		// synchronized (list) {

		// for (Iterator<Pair<Integer, Integer>> iterator = list.iterator(); iterator.hasNext();) {
		//
		// Pair<Integer, Integer> item = (Pair<Integer, Integer>) iterator.next();
		//
		// if (item.first > pair.first && item.second > pair.second) {
		// iterator.remove();
		// }
		// }

		list.add(pair);
		// }
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(number);
		builder.append(number).append(":");

		for (Pair<Integer, Integer> pair : list) {
			builder.append("{");
			builder.append(pair.first);
			builder.append(", ");
			builder.append(pair.second);
			builder.append("} ");
		}

		return builder.toString();
	}
}
