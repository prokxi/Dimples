package com.prokxi.dimples.exception;

/**
 * Thrown when a resource does not exist or the caller may not see it. Services use it for
 * both cases so that the existence of other golfers' data is not revealed. Maps to 404.
 * <p>
 * The message is returned to the client as the problem detail, so it must not contain
 * personal data.
 */
public class NotFoundException extends RuntimeException {

	public NotFoundException(String message) {
		super(message);
	}

}
