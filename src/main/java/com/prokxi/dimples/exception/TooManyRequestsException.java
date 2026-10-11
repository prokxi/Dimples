package com.prokxi.dimples.exception;

/**
 * Thrown when a caller has to wait before trying again, e.g. after too many failed
 * logins. Maps to 429.
 * <p>
 * The message is returned to the client as the problem detail, so it must not contain
 * personal data.
 */
public class TooManyRequestsException extends RuntimeException {

	public TooManyRequestsException(String message) {
		super(message);
	}

}
