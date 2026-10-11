package com.prokxi.dimples.domain;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * A plain-text password chosen by a golfer, before it is hashed. Only the length is
 * checked (NIST 800-63B); bcrypt reads at most 72 bytes, so longer input is rejected
 * instead of being cut off silently.
 */
public record Password(String value) {

	public static final int MIN_LENGTH = 10;

	public static final int MAX_LENGTH = 64;

	public static final int MAX_BYTES = 72;

	public Password {
		Objects.requireNonNull(value, "value must not be null");
		long length = value.codePoints().count();
		if (length < MIN_LENGTH || length > MAX_LENGTH) {
			throw new IllegalArgumentException(
					"Password must have " + MIN_LENGTH + " to " + MAX_LENGTH + " characters");
		}
		if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
			throw new IllegalArgumentException("Password must not be longer than " + MAX_BYTES + " bytes");
		}
	}

	/**
	 * Hides the password so that it does not end up in logs by accident.
	 */
	@Override
	public String toString() {
		return "Password[***]";
	}

}
