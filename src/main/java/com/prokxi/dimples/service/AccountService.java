package com.prokxi.dimples.service;

import java.time.Clock;
import java.time.Instant;

import com.prokxi.dimples.domain.DisplayName;
import com.prokxi.dimples.domain.EmailAddress;
import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.GolferId;
import com.prokxi.dimples.domain.Password;
import com.prokxi.dimples.exception.ConflictException;
import com.prokxi.dimples.exception.NotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use cases around a golfer's own account (SPEC-1).
 */
@Service
@RequiredArgsConstructor
public class AccountService {

	static final String EMAIL_TAKEN = "Email address is already in use.";

	private final GolferRepository golferRepository;

	private final PasswordEncoder passwordEncoder;

	private final Clock clock;

	/**
	 * Registers a new golfer. Only the hash of the password is stored.
	 * @throws ConflictException if an active golfer already uses the email address
	 */
	@Transactional
	public Golfer register(EmailAddress email, Password password, DisplayName displayName) {
		if (this.golferRepository.findActiveByEmail(email).isPresent()) {
			throw new ConflictException(EMAIL_TAKEN);
		}
		Golfer golfer = Golfer.register(GolferId.generate(), email, this.passwordEncoder.encode(password.value()),
				displayName, Instant.now(this.clock));
		try {
			return this.golferRepository.save(golfer);
		}
		catch (DuplicateKeyException ex) {
			// Another registration with the same email won the race.
			throw new ConflictException(EMAIL_TAKEN);
		}
	}

	/**
	 * Returns an active golfer.
	 * @throws NotFoundException if there is no such golfer or the account was deleted
	 */
	@Transactional(readOnly = true)
	public Golfer getGolfer(GolferId id) {
		return this.golferRepository.findById(id)
			.filter(Golfer::isActive)
			.orElseThrow(() -> new NotFoundException("Golfer not found."));
	}

}
