package com.prokxi.dimples.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class HandicapIndexEntryTest {

	private static final UUID ID = UUID.randomUUID();

	private static final HandicapIndex VALUE = HandicapIndex.of("12.4");

	private static final LocalDate DATE = LocalDate.of(2026, 5, 1);

	private static final Instant NOW = Instant.parse("2026-05-01T10:00:00Z");

	@Test
	void requiresAllFields() {
		assertThatNullPointerException().isThrownBy(() -> new HandicapIndexEntry(null, VALUE, DATE, NOW))
			.withMessageContaining("id");
		assertThatNullPointerException().isThrownBy(() -> new HandicapIndexEntry(ID, null, DATE, NOW))
			.withMessageContaining("value");
		assertThatNullPointerException().isThrownBy(() -> new HandicapIndexEntry(ID, VALUE, null, NOW))
			.withMessageContaining("effectiveOn");
		assertThatNullPointerException().isThrownBy(() -> new HandicapIndexEntry(ID, VALUE, DATE, null))
			.withMessageContaining("recordedAt");
	}

}
