package com.prokxi.dimples.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The ratings of the front or back nine of an 18-hole tee, if the course has them.
 */
public record NineHoleRating(CourseRating courseRating, SlopeRating slopeRating) {

	public static final BigDecimal MIN_COURSE_RATING = new BigDecimal("25.0");

	public static final BigDecimal MAX_COURSE_RATING = new BigDecimal("45.0");

	public NineHoleRating {
		Objects.requireNonNull(courseRating, "courseRating must not be null");
		Objects.requireNonNull(slopeRating, "slopeRating must not be null");
		if (!courseRating.isBetween(MIN_COURSE_RATING, MAX_COURSE_RATING)) {
			throw new IllegalArgumentException(
					"The Course Rating of a nine must be between " + MIN_COURSE_RATING + " and " + MAX_COURSE_RATING);
		}
	}

}
