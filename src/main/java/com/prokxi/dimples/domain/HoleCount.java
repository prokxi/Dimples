package com.prokxi.dimples.domain;

/**
 * The number of holes of a course. Every tee of the course has exactly this many holes.
 */
public enum HoleCount {

	NINE(9), EIGHTEEN(18);

	private final int holes;

	HoleCount(int holes) {
		this.holes = holes;
	}

	public int holes() {
		return this.holes;
	}

	/**
	 * The hole count for a number of holes.
	 * @throws IllegalArgumentException unless {@code holes} is 9 or 18
	 */
	public static HoleCount of(int holes) {
		for (HoleCount count : values()) {
			if (count.holes == holes) {
				return count;
			}
		}
		throw new IllegalArgumentException("A tee must have 9 or 18 holes");
	}

}
