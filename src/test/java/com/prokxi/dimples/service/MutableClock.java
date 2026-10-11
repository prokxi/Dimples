package com.prokxi.dimples.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A clock tests can move forward.
 */
final class MutableClock extends Clock {

	private Instant instant;

	MutableClock(Instant instant) {
		this.instant = instant;
	}

	void advance(Duration duration) {
		this.instant = this.instant.plus(duration);
	}

	@Override
	public ZoneId getZone() {
		return ZoneOffset.UTC;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Instant instant() {
		return this.instant;
	}

}
