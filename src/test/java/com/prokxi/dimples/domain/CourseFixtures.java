package com.prokxi.dimples.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds valid tees and holes for course tests.
 */
final class CourseFixtures {

	/**
	 * Pars of an 18-hole tee: the front nine adds up to 36, the back nine to 37.
	 */
	static final int[] PARS = { 4, 4, 3, 5, 4, 4, 3, 5, 4, 4, 3, 5, 4, 4, 4, 3, 5, 5 };

	private CourseFixtures() {
	}

	/**
	 * Holes 1…n with the pars of {@link #PARS} and stroke index {@code n + 1 - number}.
	 */
	static List<Hole> holes(int count) {
		List<Hole> holes = new ArrayList<>();
		for (int number = 1; number <= count; number++) {
			holes.add(new Hole(number, PARS[number - 1], count + 1 - number, 300 + number));
		}
		return holes;
	}

	static Tee tee18(String name) {
		return new Tee(TeeId.generate(), name, CourseRating.of("71.3"), new SlopeRating(128), null, null, holes(18));
	}

	static Tee tee9(String name) {
		return new Tee(TeeId.generate(), name, CourseRating.of("35.4"), new SlopeRating(125), null, null, holes(9));
	}

}
