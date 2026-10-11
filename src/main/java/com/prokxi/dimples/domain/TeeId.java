package com.prokxi.dimples.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies a tee of a course. Stays the same when the course is edited, so events can
 * refer to it.
 */
public record TeeId(UUID value) {

	public TeeId {
		Objects.requireNonNull(value, "value must not be null");
	}

	public static TeeId generate() {
		return new TeeId(UUID.randomUUID());
	}

}
