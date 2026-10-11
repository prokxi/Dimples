package com.prokxi.dimples.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A WHS Course Rating in strokes, with one decimal place. The range accepted here covers
 * nine and 18 holes; {@link Tee} and {@link NineHoleRating} check the range for their
 * number of holes.
 */
public record CourseRating(BigDecimal value) {

	public static final BigDecimal MIN = new BigDecimal("25.0");

	public static final BigDecimal MAX = new BigDecimal("90.0");

	private static final BigDecimal TWO = BigDecimal.valueOf(2);

	public CourseRating {
		Objects.requireNonNull(value, "value must not be null");
		if (value.compareTo(MIN) < 0 || value.compareTo(MAX) > 0) {
			throw new IllegalArgumentException("Course Rating must be between " + MIN + " and " + MAX);
		}
		try {
			value = value.setScale(1, RoundingMode.UNNECESSARY);
		}
		catch (ArithmeticException ex) {
			throw new IllegalArgumentException("Course Rating must have at most one decimal place", ex);
		}
	}

	public static CourseRating of(String value) {
		return new CourseRating(new BigDecimal(value));
	}

	/**
	 * Half of this rating, rounded half up to one decimal (71.3 → 35.7): the Course
	 * Rating of a nine that has no rating of its own.
	 */
	public CourseRating half() {
		return new CourseRating(this.value.divide(TWO).setScale(1, RoundingMode.HALF_UP));
	}

	/**
	 * Whether this rating lies between {@code min} and {@code max}, both inclusive.
	 */
	boolean isBetween(BigDecimal min, BigDecimal max) {
		return this.value.compareTo(min) >= 0 && this.value.compareTo(max) <= 0;
	}

}
