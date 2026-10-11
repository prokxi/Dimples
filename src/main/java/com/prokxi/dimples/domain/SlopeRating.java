package com.prokxi.dimples.domain;

/**
 * A WHS Slope Rating: a whole number from 55 to 155.
 */
public record SlopeRating(int value) {

	public static final int MIN = 55;

	public static final int MAX = 155;

	public SlopeRating {
		if (value < MIN || value > MAX) {
			throw new IllegalArgumentException("Slope Rating must be between " + MIN + " and " + MAX);
		}
	}

}
