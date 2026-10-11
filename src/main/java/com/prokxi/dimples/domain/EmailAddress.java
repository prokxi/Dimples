package com.prokxi.dimples.domain;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An email address, trimmed and lower-cased so that it can be compared and used as the
 * login name.
 */
public record EmailAddress(String value) {

	public static final int MAX_LENGTH = 254;

	/**
	 * Deliberately basic: one {@code @}, no whitespace and a dot in the domain part.
	 */
	private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

	public EmailAddress {
		Objects.requireNonNull(value, "value must not be null");
		value = value.strip().toLowerCase(Locale.ROOT);
		if (value.length() > MAX_LENGTH) {
			throw new IllegalArgumentException("Email address must have at most " + MAX_LENGTH + " characters");
		}
		if (!FORMAT.matcher(value).matches()) {
			throw new IllegalArgumentException("Email address is not valid");
		}
	}

	/**
	 * Hides the address so that it does not end up in logs by accident.
	 */
	@Override
	public String toString() {
		return "EmailAddress[***]";
	}

}
