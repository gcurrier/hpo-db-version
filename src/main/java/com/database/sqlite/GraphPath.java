package com.database.sqlite;

public class GraphPath {

	int term1;
	int term2;
	int distance;

	public GraphPath(int termId1, int termId2, int distance) {

		this.term1 = termId1;
		this.term2 = termId2;
		this.distance = distance;
	}

	@Override
	public int hashCode() {
		return term1 * 32 + term2 * 33 + distance;
	}

	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof GraphPath)) {
			return false;
		}

		GraphPath otherPath = (GraphPath) obj;
		if (otherPath.term1 == term1 && otherPath.term2 == term2 && otherPath.distance == distance)
			return true;

		return false;
	}

	@Override
	public String toString() {
		return term1 + " -> " + term2 + " : " + distance;
	}

}
