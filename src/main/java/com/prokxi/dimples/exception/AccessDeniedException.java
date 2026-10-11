package com.prokxi.dimples.exception;

/**
 * Thrown when the caller may see a resource but is not allowed to perform the action on
 * it. Maps to 403.
 * <p>
 * The message is returned to the client as the problem detail, so it must not contain
 * personal data.
 */
public class AccessDeniedException extends RuntimeException {

	public AccessDeniedException(String message) {
		super(message);
	}

}
