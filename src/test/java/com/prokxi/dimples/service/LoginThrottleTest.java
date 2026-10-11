package com.prokxi.dimples.service;

import java.time.Duration;
import java.time.Instant;

import com.prokxi.dimples.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class LoginThrottleTest {

	private static final String EMAIL = "max@example.com";

	private final MutableClock clock = new MutableClock(Instant.parse("2026-06-15T12:00:00Z"));

	private final LoginThrottle throttle = new LoginThrottle(this.clock);

	private void fail(int times) {
		for (int i = 0; i < times; i++) {
			this.throttle.checkAllowed(EMAIL);
			this.throttle.recordFailure(EMAIL);
		}
	}

	@Test
	void allowsAnUnknownEmail() {
		assertThatNoException().isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
	}

	@Test
	void allowsFourFailures() {
		fail(4);

		assertThatNoException().isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
	}

	@Test
	void blocksTheSixthAttemptAfterFiveFailures() {
		fail(5);

		assertThatExceptionOfType(TooManyRequestsException.class).isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
	}

	@Test
	void blockAppliesToTheNormalisedEmail() {
		fail(5);

		assertThatExceptionOfType(TooManyRequestsException.class)
			.isThrownBy(() -> this.throttle.checkAllowed("  MAX@Example.com "));
	}

	@Test
	void blockDoesNotAffectOtherEmails() {
		fail(5);

		assertThatNoException().isThrownBy(() -> this.throttle.checkAllowed("other@example.com"));
	}

	@Test
	void blockLastsFifteenMinutes() {
		fail(5);

		this.clock.advance(Duration.ofMinutes(15).minusSeconds(1));
		assertThatExceptionOfType(TooManyRequestsException.class).isThrownBy(() -> this.throttle.checkAllowed(EMAIL));

		this.clock.advance(Duration.ofSeconds(1));
		assertThatNoException().isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
	}

	@Test
	void afterTheBlockTheCountStartsAgain() {
		fail(5);
		this.clock.advance(Duration.ofMinutes(15));

		fail(4);

		assertThatNoException().isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
	}

	@Test
	void failuresOlderThanTheWindowDoNotCount() {
		fail(4);
		this.clock.advance(Duration.ofMinutes(15));

		fail(1);

		assertThatNoException().isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
	}

	@Test
	void failuresJustInsideTheWindowCount() {
		fail(4);
		this.clock.advance(Duration.ofMinutes(15).minusSeconds(1));

		fail(1);

		assertThatExceptionOfType(TooManyRequestsException.class).isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
	}

	@Test
	void successResetsTheFailures() {
		fail(4);

		this.throttle.recordSuccess(" Max@example.com");
		fail(4);

		assertThatNoException().isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
		assertThat(this.throttle.trackedEmails()).isOne();
	}

	@Test
	void dropsStaleEntriesWhenManyEmailsAreTracked() {
		for (int i = 0; i < LoginThrottle.CLEANUP_THRESHOLD; i++) {
			this.throttle.recordFailure("user" + i + "@example.com");
		}
		fail(5);
		assertThat(this.throttle.trackedEmails()).isEqualTo(LoginThrottle.CLEANUP_THRESHOLD + 1);

		this.clock.advance(Duration.ofMinutes(15).plusSeconds(1));
		this.throttle.recordFailure("new@example.com");

		assertThat(this.throttle.trackedEmails()).isOne();
	}

	@Test
	void keepsBlockedAndRecentEntriesDuringCleanup() {
		fail(5);
		this.clock.advance(Duration.ofMinutes(10));
		this.throttle.recordFailure("recent@example.com");
		for (int i = 0; i < LoginThrottle.CLEANUP_THRESHOLD; i++) {
			this.throttle.recordFailure("user" + i + "@example.com");
		}

		this.throttle.recordFailure("new@example.com");

		assertThat(this.throttle.trackedEmails()).isEqualTo(LoginThrottle.CLEANUP_THRESHOLD + 3);
		assertThatExceptionOfType(TooManyRequestsException.class).isThrownBy(() -> this.throttle.checkAllowed(EMAIL));
	}

}
