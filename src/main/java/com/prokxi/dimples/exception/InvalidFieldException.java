package com.prokxi.dimples.exception;

/**
 * Thrown when a request field passes Bean Validation but breaks a domain rule, e.g. a
 * display name that is too short after trimming. Maps to 400 with the field listed like a
 * Bean Validation error.
 * <p>
 * The message is returned to the client, so it must not contain the rejected value or
 * other personal data.
 */
public class InvalidFieldException extends RuntimeException {

	private final String field;

	public InvalidFieldException(String field, String message) {
		super(message);
		this.field = field;
	}

	public String getField() {
		return this.field;
	}

}
