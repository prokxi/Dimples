package com.prokxi.dimples.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * A WHS Handicap Index with one decimal place. Plus handicaps are negative: "+2.3" is
 * stored as {@code -2.3}.
 */
public record HandicapIndex(BigDecimal value) {

	public static final BigDecimal MIN = new BigDecimal("-10.0");

	public static final BigDecimal MAX = new BigDecimal("54.0");

	public HandicapIndex {
		Objects.requireNonNull(value, "value must not be null");
		if (value.compareTo(MIN) < 0 || value.compareTo(MAX) > 0) {
			throw new IllegalArgumentException("Handicap Index must be between +10.0 and 54.0");
		}
		try {
			value = value.setScale(1, RoundingMode.UNNECESSARY);
		}
		catch (ArithmeticException ex) {
			throw new IllegalArgumentException("Handicap Index must have at most one decimal place", ex);
		}
	}

	public static HandicapIndex of(String value) {
		return new HandicapIndex(new BigDecimal(value));
	}

}
