package com.prokxi.dimples.inbound;

import java.util.function.Supplier;

import com.prokxi.dimples.exception.InvalidFieldException;

/**
 * Turns request values into domain values. A broken domain rule becomes a 400 error for
 * that field instead of a 500.
 */
final class RequestFields {

	private RequestFields() {
	}

	static <T> T parse(String field, Supplier<T> factory) {
		try {
			return factory.get();
		}
		catch (IllegalArgumentException ex) {
			throw new InvalidFieldException(field, ex.getMessage());
		}
	}

}
