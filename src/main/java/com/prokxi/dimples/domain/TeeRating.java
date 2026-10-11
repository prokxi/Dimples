package com.prokxi.dimples.domain;

import java.util.List;
import java.util.Objects;

/**
 * The ratings and holes of a tee for one {@link HoleSelection}: what handicap
 * calculations and stroke allocation need.
 *
 * @param holes the holes of the selection, ordered by number
 */
public record TeeRating(CourseRating courseRating, SlopeRating slopeRating, List<Hole> holes) {

	public TeeRating {
		Objects.requireNonNull(courseRating, "courseRating must not be null");
		Objects.requireNonNull(slopeRating, "slopeRating must not be null");
		holes = List.copyOf(holes);
		if (holes.isEmpty()) {
			throw new IllegalArgumentException("A rating needs at least one hole");
		}
	}

	/**
	 * The sum of the pars of the holes.
	 */
	public int par() {
		return this.holes.stream().mapToInt(Hole::par).sum();
	}

}
