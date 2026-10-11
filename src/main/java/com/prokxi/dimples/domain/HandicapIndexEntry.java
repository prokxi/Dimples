package com.prokxi.dimples.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * One value in a golfer's Handicap Index history, valid from {@code effectiveOn} until
 * the next entry.
 */
public record HandicapIndexEntry(UUID id, HandicapIndex value, LocalDate effectiveOn, Instant recordedAt) {

	public HandicapIndexEntry {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(value, "value must not be null");
		Objects.requireNonNull(effectiveOn, "effectiveOn must not be null");
		Objects.requireNonNull(recordedAt, "recordedAt must not be null");
	}

}
