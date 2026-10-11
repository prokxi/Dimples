package com.prokxi.dimples.domain;

import java.util.Objects;

/**
 * The name other participants see, e.g. in pairings and standings.
 */
public record DisplayName(String value) {

	public static final int MIN_LENGTH = 2;

	public static final int MAX_LENGTH = 40;

	public DisplayName {
		Objects.requireNonNull(value, "value must not be null");
		value = value.strip();
		long length = value.codePoints().count();
		if (length < MIN_LENGTH || length > MAX_LENGTH) {
			throw new IllegalArgumentException(
					"Display name must have " + MIN_LENGTH + " to " + MAX_LENGTH + " characters");
		}
		if (value.codePoints().anyMatch(Character::isISOControl)) {
			throw new IllegalArgumentException("Display name must not contain control characters");
		}
	}

}
