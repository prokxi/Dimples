package com.prokxi.dimples.exception;

/**
 * Thrown when a request conflicts with the current state, e.g. a duplicate email or an
 * outdated version. Maps to 409.
 * <p>
 * The message is returned to the client as the problem detail, so it must not contain
 * personal data.
 */
public class ConflictException extends RuntimeException {

	public ConflictException(String message) {
		super(message);
	}

}
