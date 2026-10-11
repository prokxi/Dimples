package com.prokxi.dimples.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A golf course with its tees (aggregate root). Courses are shared by all golfers.
 * Immutable: every change returns a new instance. Times are passed in by the caller,
 * which takes them from its {@link java.time.Clock}.
 */
public final class Course {

	public static final int MIN_NAME_LENGTH = 2;

	public static final int MAX_NAME_LENGTH = 100;

	public static final int MAX_CITY_LENGTH = 100;

	private static final Set<String> COUNTRY_CODES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);

	private final CourseId id;

	private final String name;

	private final String city;

	private final String country;

	private final HoleCount holeCount;

	private final List<Tee> tees;

	private final GolferId createdBy;

	private final Instant createdAt;

	private final Instant updatedAt;

	private final int version;

	/**
	 * Restores a course, e.g. from persistence. Use {@link #create} for new courses.
	 * @param name leading and trailing whitespace is removed
	 * @param city {@code null} if unknown; a blank city counts as unknown
	 * @param country ISO 3166-1 alpha-2 code in any case, e.g. {@code DE}; {@code null}
	 * if unknown
	 * @param tees at least one; every tee has {@code holeCount} holes
	 * @param createdBy the golfer who created the course
	 * @param version optimistic-locking version; {@code 0} for a course that has not been
	 * stored yet
	 */
	public Course(CourseId id, String name, String city, String country, HoleCount holeCount, List<Tee> tees,
			GolferId createdBy, Instant createdAt, Instant updatedAt, int version) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.name = requireName(name);
		this.city = normalizeCity(city);
		this.country = normalizeCountry(country);
		this.holeCount = Objects.requireNonNull(holeCount, "holeCount must not be null");
		this.tees = List.copyOf(Objects.requireNonNull(tees, "tees must not be null"));
		this.createdBy = Objects.requireNonNull(createdBy, "createdBy must not be null");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
		this.version = version;
		requireTees(this.tees, holeCount);
		if (updatedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("A course cannot be updated before it was created");
		}
	}

	/**
	 * Creates a new course.
	 * @see #Course(CourseId, String, String, String, HoleCount, List, GolferId, Instant,
	 * Instant, int)
	 */
	public static Course create(CourseId id, String name, String city, String country, HoleCount holeCount,
			List<Tee> tees, GolferId createdBy, Instant now) {
		return new Course(id, name, city, country, holeCount, tees, createdBy, now, now, 0);
	}

	/**
	 * Replaces the whole course, tees and holes included. Tees keep their ID when the
	 * given tee has the same {@link TeeId}. The creator, creation time and version stay
	 * the same.
	 */
	public Course update(String name, String city, String country, HoleCount holeCount, List<Tee> tees, Instant now) {
		return new Course(this.id, name, city, country, holeCount, tees, this.createdBy, this.createdAt,
				Objects.requireNonNull(now, "now must not be null"), this.version);
	}

	public Optional<Tee> findTee(TeeId teeId) {
		return this.tees.stream().filter((tee) -> tee.id().equals(teeId)).findFirst();
	}

	public CourseId id() {
		return this.id;
	}

	public String name() {
		return this.name;
	}

	public Optional<String> city() {
		return Optional.ofNullable(this.city);
	}

	/**
	 * The ISO 3166-1 alpha-2 code in upper case, e.g. {@code DE}.
	 */
	public Optional<String> country() {
		return Optional.ofNullable(this.country);
	}

	public HoleCount holeCount() {
		return this.holeCount;
	}

	public List<Tee> tees() {
		return this.tees;
	}

	public GolferId createdBy() {
		return this.createdBy;
	}

	public Instant createdAt() {
		return this.createdAt;
	}

	public Instant updatedAt() {
		return this.updatedAt;
	}

	public int version() {
		return this.version;
	}

	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof Course course && this.id.equals(course.id));
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public String toString() {
		return "Course[id=" + this.id.value() + ", name=" + this.name + "]";
	}

	private static String requireName(String name) {
		Objects.requireNonNull(name, "name must not be null");
		String stripped = name.strip();
		long length = stripped.codePoints().count();
		if (length < MIN_NAME_LENGTH || length > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException(
					"Course name must have " + MIN_NAME_LENGTH + " to " + MAX_NAME_LENGTH + " characters");
		}
		if (stripped.codePoints().anyMatch(Character::isISOControl)) {
			throw new IllegalArgumentException("Course name must not contain control characters");
		}
		return stripped;
	}

	private static String normalizeCity(String city) {
		if (city == null || city.isBlank()) {
			return null;
		}
		String stripped = city.strip();
		if (stripped.codePoints().count() > MAX_CITY_LENGTH) {
			throw new IllegalArgumentException("City must have at most " + MAX_CITY_LENGTH + " characters");
		}
		if (stripped.codePoints().anyMatch(Character::isISOControl)) {
			throw new IllegalArgumentException("City must not contain control characters");
		}
		return stripped;
	}

	private static String normalizeCountry(String country) {
		if (country == null) {
			return null;
		}
		String code = country.toUpperCase(Locale.ROOT);
		if (!COUNTRY_CODES.contains(code)) {
			throw new IllegalArgumentException("Country must be an ISO 3166-1 alpha-2 code, e.g. DE");
		}
		return code;
	}

	private static void requireTees(List<Tee> tees, HoleCount holeCount) {
		if (tees.isEmpty()) {
			throw new IllegalArgumentException("A course needs at least one tee");
		}
		Set<String> names = new HashSet<>();
		Set<TeeId> ids = new HashSet<>();
		for (Tee tee : tees) {
			if (tee.holeCount() != holeCount) {
				throw new IllegalArgumentException("Every tee must have " + holeCount.holes() + " holes");
			}
			if (!names.add(tee.name().toLowerCase(Locale.ROOT))) {
				throw new IllegalArgumentException("Tee names must be unique within a course");
			}
			if (!ids.add(tee.id())) {
				throw new IllegalArgumentException("Tee IDs must be unique within a course");
			}
		}
	}

}
