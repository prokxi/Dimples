package com.prokxi.dimples.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.prokxi.dimples.exception.TooManyRequestsException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Slows down password guessing (SPEC-1, section 4.2): after {@value #MAX_FAILURES} failed
 * logins for an email within {@link #WINDOW}, further attempts for it are rejected for
 * {@link #WINDOW}. Kept in memory, which is enough for a single instance.
 */
@Service
@RequiredArgsConstructor
public class LoginThrottle {

	static final int MAX_FAILURES = 5;

	static final Duration WINDOW = Duration.ofMinutes(15);

	/**
	 * Above this number of tracked emails, stale entries are dropped so that guessing
	 * many different emails cannot grow the map without limit.
	 */
	static final int CLEANUP_THRESHOLD = 1000;

	private final Clock clock;

	private final Map<String, Attempts> attemptsByEmail = new HashMap<>();

	/**
	 * @throws TooManyRequestsException if the email is currently blocked
	 */
	public synchronized void checkAllowed(String email) {
		Attempts attempts = this.attemptsByEmail.get(key(email));
		if (attempts != null && attempts.isBlocked(Instant.now(this.clock))) {
			throw new TooManyRequestsException("Too many failed logins. Try again later.");
		}
	}

	public synchronized void recordFailure(String email) {
		Instant now = Instant.now(this.clock);
		if (this.attemptsByEmail.size() >= CLEANUP_THRESHOLD) {
			this.attemptsByEmail.values().removeIf((attempts) -> attempts.isStale(now));
		}
		this.attemptsByEmail.computeIfAbsent(key(email), (ignored) -> new Attempts()).recordFailure(now);
	}

	public synchronized void recordSuccess(String email) {
		this.attemptsByEmail.remove(key(email));
	}

	synchronized int trackedEmails() {
		return this.attemptsByEmail.size();
	}

	private static String key(String email) {
		return email.strip().toLowerCase(Locale.ROOT);
	}

	private static final class Attempts {

		private final List<Instant> failures = new ArrayList<>();

		private Instant blockedUntil;

		void recordFailure(Instant now) {
			this.failures.removeIf((failure) -> !failure.isAfter(now.minus(WINDOW)));
			this.failures.add(now);
			if (this.failures.size() >= MAX_FAILURES) {
				this.blockedUntil = now.plus(WINDOW);
				this.failures.clear();
			}
		}

		boolean isBlocked(Instant now) {
			return this.blockedUntil != null && now.isBefore(this.blockedUntil);
		}

		boolean isStale(Instant now) {
			return !isBlocked(now) && this.failures.stream().noneMatch((failure) -> failure.isAfter(now.minus(WINDOW)));
		}

	}

}
