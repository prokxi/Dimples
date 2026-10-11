package com.prokxi.dimples.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A registered user of Dimples (aggregate root). Immutable: every change returns a new
 * instance.
 * <p>
 * Personal data (email, password hash, display name, Handicap Index history) exists only
 * while the golfer is {@link GolferStatus#ACTIVE}; deleting the account removes it. Times
 * are passed in by the caller, which takes them from its {@link java.time.Clock}.
 */
public final class Golfer {

	private static final Comparator<HandicapIndexEntry> NEWEST_FIRST = Comparator
		.comparing(HandicapIndexEntry::effectiveOn)
		.reversed();

	private final GolferId id;

	private final EmailAddress email;

	private final String passwordHash;

	private final DisplayName displayName;

	private final DistanceUnit distanceUnit;

	private final List<HandicapIndexEntry> handicapIndexes;

	private final GolferStatus status;

	private final GolferRole role;

	private final boolean mustChangePassword;

	private final Instant createdAt;

	private final Instant deletedAt;

	private final int version;

	/**
	 * Restores a golfer, e.g. from persistence. Use {@link #register} for new golfers.
	 * @param email {@code null} for a deleted golfer
	 * @param passwordHash {@code null} for a deleted golfer
	 * @param displayName {@code null} for a deleted golfer
	 * @param deletedAt {@code null} for an active golfer
	 * @param version optimistic-locking version; {@code 0} for a golfer that has not been
	 * stored yet
	 */
	public Golfer(GolferId id, EmailAddress email, String passwordHash, DisplayName displayName,
			DistanceUnit distanceUnit, List<HandicapIndexEntry> handicapIndexes, GolferStatus status, GolferRole role,
			boolean mustChangePassword, Instant createdAt, Instant deletedAt, int version) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.distanceUnit = Objects.requireNonNull(distanceUnit, "distanceUnit must not be null");
		this.status = Objects.requireNonNull(status, "status must not be null");
		this.role = Objects.requireNonNull(role, "role must not be null");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
		this.handicapIndexes = handicapIndexes.stream().sorted(NEWEST_FIRST).toList();
		this.email = email;
		this.passwordHash = passwordHash;
		this.displayName = displayName;
		this.mustChangePassword = mustChangePassword;
		this.deletedAt = deletedAt;
		this.version = version;
		if (status == GolferStatus.ACTIVE) {
			Objects.requireNonNull(email, "email must not be null");
			requirePasswordHash(passwordHash);
			Objects.requireNonNull(displayName, "displayName must not be null");
			if (deletedAt != null) {
				throw new IllegalArgumentException("An active golfer has no deletion time");
			}
		}
		else {
			if (email != null || passwordHash != null || displayName != null || !this.handicapIndexes.isEmpty()) {
				throw new IllegalArgumentException("A deleted golfer has no personal data");
			}
			Objects.requireNonNull(deletedAt, "deletedAt must not be null");
		}
		requireUniqueEntries(this.handicapIndexes);
	}

	/**
	 * Creates a new active golfer with default settings.
	 */
	public static Golfer register(GolferId id, EmailAddress email, String passwordHash, DisplayName displayName,
			Instant now) {
		return new Golfer(id, email, passwordHash, displayName, DistanceUnit.METERS, List.of(), GolferStatus.ACTIVE,
				GolferRole.GOLFER, false, now, null, 0);
	}

	/**
	 * The Handicap Index that applies on {@code date}: the entry with the latest
	 * {@code effectiveOn} on or before it. Empty if there is none.
	 */
	public Optional<HandicapIndex> handicapIndexOn(LocalDate date) {
		Objects.requireNonNull(date, "date must not be null");
		return this.handicapIndexes.stream()
			.filter((entry) -> !entry.effectiveOn().isAfter(date))
			.findFirst()
			.map(HandicapIndexEntry::value);
	}

	/**
	 * The Handicap Index that applies today; entries are never in the future, so this is
	 * normally the newest one.
	 */
	public Optional<HandicapIndex> currentHandicapIndex(LocalDate today) {
		return handicapIndexOn(today);
	}

	public Optional<HandicapIndexEntry> findHandicapIndexEntry(UUID entryId) {
		return this.handicapIndexes.stream().filter((entry) -> entry.id().equals(entryId)).findFirst();
	}

	/**
	 * Adds a Handicap Index to the history. If an entry with the same {@code effectiveOn}
	 * exists, its value is replaced instead (typo correction) and it keeps its ID.
	 * @param entryId the ID of the new entry, if one is added
	 * @param today the current date, to reject future dates
	 * @param now the time the value is recorded
	 */
	public Golfer recordHandicapIndex(UUID entryId, HandicapIndex value, LocalDate effectiveOn, LocalDate today,
			Instant now) {
		requireActive();
		if (effectiveOn.isAfter(today)) {
			throw new IllegalArgumentException("A Handicap Index cannot take effect in the future");
		}
		HandicapIndexEntry existing = this.handicapIndexes.stream()
			.filter((entry) -> entry.effectiveOn().equals(effectiveOn))
			.findFirst()
			.orElse(null);
		UUID id = (existing != null) ? existing.id() : entryId;
		HandicapIndexEntry recorded = new HandicapIndexEntry(id, value, effectiveOn, now);
		List<HandicapIndexEntry> entries = new ArrayList<>(this.handicapIndexes);
		entries.remove(existing);
		entries.add(recorded);
		return withHandicapIndexes(entries);
	}

	/**
	 * Removes an entry from the history. Snapshots taken from it earlier are not
	 * affected.
	 * @throws IllegalArgumentException if the golfer has no entry with this ID
	 */
	public Golfer removeHandicapIndex(UUID entryId) {
		requireActive();
		HandicapIndexEntry entry = findHandicapIndexEntry(entryId)
			.orElseThrow(() -> new IllegalArgumentException("No Handicap Index entry with this ID"));
		return withHandicapIndexes(
				this.handicapIndexes.stream().filter((other) -> !other.id().equals(entry.id())).toList());
	}

	public Golfer updateProfile(DisplayName displayName, DistanceUnit distanceUnit) {
		requireActive();
		return new Golfer(this.id, this.email, this.passwordHash, Objects.requireNonNull(displayName),
				Objects.requireNonNull(distanceUnit), this.handicapIndexes, this.status, this.role,
				this.mustChangePassword, this.createdAt, this.deletedAt, this.version);
	}

	/**
	 * Sets the password chosen by the golfer and lifts a pending forced change.
	 */
	public Golfer changePassword(String newPasswordHash) {
		return withPassword(newPasswordHash, false);
	}

	/**
	 * Sets a temporary password from the operator, which the golfer has to change.
	 */
	public Golfer resetPassword(String temporaryPasswordHash) {
		return withPassword(temporaryPasswordHash, true);
	}

	/**
	 * Deletes the account: removes all personal data and keeps the golfer as
	 * {@link GolferStatus#DELETED} so past results stay consistent.
	 */
	public Golfer delete(Instant now) {
		requireActive();
		return new Golfer(this.id, null, null, null, this.distanceUnit, List.of(), GolferStatus.DELETED, this.role,
				false, this.createdAt, Objects.requireNonNull(now, "now must not be null"), this.version);
	}

	public boolean isActive() {
		return this.status == GolferStatus.ACTIVE;
	}

	public GolferId id() {
		return this.id;
	}

	public Optional<EmailAddress> email() {
		return Optional.ofNullable(this.email);
	}

	public Optional<String> passwordHash() {
		return Optional.ofNullable(this.passwordHash);
	}

	public Optional<DisplayName> displayName() {
		return Optional.ofNullable(this.displayName);
	}

	public DistanceUnit distanceUnit() {
		return this.distanceUnit;
	}

	/**
	 * The Handicap Index history, newest {@code effectiveOn} first.
	 */
	public List<HandicapIndexEntry> handicapIndexes() {
		return this.handicapIndexes;
	}

	public GolferStatus status() {
		return this.status;
	}

	public GolferRole role() {
		return this.role;
	}

	public boolean mustChangePassword() {
		return this.mustChangePassword;
	}

	public Instant createdAt() {
		return this.createdAt;
	}

	public Optional<Instant> deletedAt() {
		return Optional.ofNullable(this.deletedAt);
	}

	public int version() {
		return this.version;
	}

	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof Golfer golfer && this.id.equals(golfer.id));
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public String toString() {
		return "Golfer[id=" + this.id.value() + ", status=" + this.status + "]";
	}

	private Golfer withHandicapIndexes(List<HandicapIndexEntry> entries) {
		return new Golfer(this.id, this.email, this.passwordHash, this.displayName, this.distanceUnit, entries,
				this.status, this.role, this.mustChangePassword, this.createdAt, this.deletedAt, this.version);
	}

	private Golfer withPassword(String passwordHash, boolean mustChangePassword) {
		requireActive();
		return new Golfer(this.id, this.email, passwordHash, this.displayName, this.distanceUnit, this.handicapIndexes,
				this.status, this.role, mustChangePassword, this.createdAt, this.deletedAt, this.version);
	}

	private void requireActive() {
		if (!isActive()) {
			throw new IllegalStateException("The golfer's account has been deleted");
		}
	}

	private static void requirePasswordHash(String passwordHash) {
		if (passwordHash == null || passwordHash.isBlank()) {
			throw new IllegalArgumentException("passwordHash must not be blank");
		}
	}

	private static void requireUniqueEntries(List<HandicapIndexEntry> entries) {
		Set<LocalDate> dates = new HashSet<>();
		Set<UUID> ids = new HashSet<>();
		for (HandicapIndexEntry entry : entries) {
			if (!dates.add(entry.effectiveOn()) || !ids.add(entry.id())) {
				throw new IllegalArgumentException("Handicap Index entries must have unique IDs and dates");
			}
		}
	}

}
