package com.prokxi.dimples.service;

import java.util.Optional;

import com.prokxi.dimples.domain.EmailAddress;
import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.GolferId;

/**
 * Stores golfers, including their Handicap Index history.
 */
public interface GolferRepository {

	/**
	 * Inserts a new golfer (version {@code 0}) or updates an existing one.
	 * @return the stored golfer with its new version
	 * @throws org.springframework.dao.OptimisticLockingFailureException if the golfer was
	 * changed in the meantime
	 */
	Golfer save(Golfer golfer);

	/**
	 * Finds a golfer, whether active or deleted.
	 */
	Optional<Golfer> findById(GolferId id);

	Optional<Golfer> findActiveByEmail(EmailAddress email);

}
