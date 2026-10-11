package com.prokxi.dimples.domain;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A tee set of a course with its ratings and holes. Part of the {@link Course} aggregate.
 * The number of holes (9 or 18) follows from the holes; the course checks that it matches
 * its {@link HoleCount}.
 */
public final class Tee {

	public static final int MAX_NAME_LENGTH = 30;

	private static final BigDecimal MIN_18_HOLE_COURSE_RATING = new BigDecimal("50.0");

	private static final BigDecimal MAX_18_HOLE_COURSE_RATING = new BigDecimal("90.0");

	private static final int HOLES_PER_NINE = 9;

	private final TeeId id;

	private final String name;

	private final CourseRating courseRating;

	private final SlopeRating slopeRating;

	private final NineHoleRating frontNine;

	private final NineHoleRating backNine;

	private final List<Hole> holes;

	private final HoleCount holeCount;

	/**
	 * @param name the tee's name, e.g. "Yellow"; leading and trailing whitespace is
	 * removed
	 * @param courseRating the Course Rating for all holes of the tee
	 * @param slopeRating the Slope Rating for all holes of the tee
	 * @param frontNine the ratings of holes 1–9, or {@code null}; only on 18-hole tees
	 * and only together with {@code backNine}
	 * @param backNine the ratings of holes 10–18, or {@code null}
	 * @param holes 9 or 18 holes, numbered 1…n, in any order
	 */
	public Tee(TeeId id, String name, CourseRating courseRating, SlopeRating slopeRating, NineHoleRating frontNine,
			NineHoleRating backNine, List<Hole> holes) {
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.name = requireName(name);
		this.courseRating = Objects.requireNonNull(courseRating, "courseRating must not be null");
		this.slopeRating = Objects.requireNonNull(slopeRating, "slopeRating must not be null");
		this.holes = Objects.requireNonNull(holes, "holes must not be null")
			.stream()
			.sorted(Comparator.comparingInt(Hole::number))
			.toList();
		this.holeCount = HoleCount.of(this.holes.size());
		requireNumberedWithoutGaps(this.holes);
		requireStrokeIndexesOnePerHole(this.holes);
		requireCourseRatingForHoleCount(courseRating, this.holeCount);
		if ((frontNine == null) != (backNine == null)) {
			throw new IllegalArgumentException("Nine-hole ratings need both the front and the back nine");
		}
		if (frontNine != null && this.holeCount != HoleCount.EIGHTEEN) {
			throw new IllegalArgumentException("Nine-hole ratings are only allowed on 18-hole tees");
		}
		this.frontNine = frontNine;
		this.backNine = backNine;
	}

	/**
	 * The ratings, par and holes for a hole selection (SPEC-2, section 3). A nine without
	 * ratings of its own gets half the tee's Course Rating, rounded half up to one
	 * decimal, and the tee's Slope Rating.
	 * @throws IllegalArgumentException for a front or back nine on a 9-hole tee
	 */
	public TeeRating ratingFor(HoleSelection selection) {
		Objects.requireNonNull(selection, "selection must not be null");
		return switch (selection) {
			case ALL -> new TeeRating(this.courseRating, this.slopeRating, this.holes);
			case FRONT_NINE -> nineRating(this.frontNine, this.holes.subList(0, HOLES_PER_NINE));
			case BACK_NINE -> nineRating(this.backNine, this.holes.subList(HOLES_PER_NINE, this.holes.size()));
		};
	}

	public TeeId id() {
		return this.id;
	}

	public String name() {
		return this.name;
	}

	public CourseRating courseRating() {
		return this.courseRating;
	}

	public SlopeRating slopeRating() {
		return this.slopeRating;
	}

	public Optional<NineHoleRating> frontNine() {
		return Optional.ofNullable(this.frontNine);
	}

	public Optional<NineHoleRating> backNine() {
		return Optional.ofNullable(this.backNine);
	}

	/**
	 * The holes, ordered by number.
	 */
	public List<Hole> holes() {
		return this.holes;
	}

	public HoleCount holeCount() {
		return this.holeCount;
	}

	/**
	 * The sum of the pars of all holes.
	 */
	public int par() {
		return this.holes.stream().mapToInt(Hole::par).sum();
	}

	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof Tee tee && this.id.equals(tee.id));
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public String toString() {
		return "Tee[id=" + this.id.value() + ", name=" + this.name + "]";
	}

	private TeeRating nineRating(NineHoleRating nine, List<Hole> nineHoles) {
		if (this.holeCount != HoleCount.EIGHTEEN) {
			throw new IllegalArgumentException("The front and back nine exist only on 18-hole courses");
		}
		if (nine != null) {
			return new TeeRating(nine.courseRating(), nine.slopeRating(), nineHoles);
		}
		return new TeeRating(this.courseRating.half(), this.slopeRating, nineHoles);
	}

	private static String requireName(String name) {
		Objects.requireNonNull(name, "name must not be null");
		String stripped = name.strip();
		long length = stripped.codePoints().count();
		if (length < 1 || length > MAX_NAME_LENGTH) {
			throw new IllegalArgumentException("Tee name must have 1 to " + MAX_NAME_LENGTH + " characters");
		}
		if (stripped.codePoints().anyMatch(Character::isISOControl)) {
			throw new IllegalArgumentException("Tee name must not contain control characters");
		}
		return stripped;
	}

	private static void requireNumberedWithoutGaps(List<Hole> sortedHoles) {
		for (int i = 0; i < sortedHoles.size(); i++) {
			if (sortedHoles.get(i).number() != i + 1) {
				throw new IllegalArgumentException(
						"Holes must be numbered 1 to " + sortedHoles.size() + " without gaps");
			}
		}
	}

	private static void requireStrokeIndexesOnePerHole(List<Hole> holes) {
		Set<Integer> strokeIndexes = new HashSet<>();
		for (Hole hole : holes) {
			if (hole.strokeIndex() > holes.size() || !strokeIndexes.add(hole.strokeIndex())) {
				throw new IllegalArgumentException("Stroke indexes must be 1 to " + holes.size() + ", each used once");
			}
		}
	}

	private static void requireCourseRatingForHoleCount(CourseRating courseRating, HoleCount holeCount) {
		if (holeCount == HoleCount.EIGHTEEN
				&& !courseRating.isBetween(MIN_18_HOLE_COURSE_RATING, MAX_18_HOLE_COURSE_RATING)) {
			throw new IllegalArgumentException("The Course Rating of an 18-hole tee must be between "
					+ MIN_18_HOLE_COURSE_RATING + " and " + MAX_18_HOLE_COURSE_RATING);
		}
		if (holeCount == HoleCount.NINE
				&& !courseRating.isBetween(NineHoleRating.MIN_COURSE_RATING, NineHoleRating.MAX_COURSE_RATING)) {
			throw new IllegalArgumentException("The Course Rating of a 9-hole tee must be between "
					+ NineHoleRating.MIN_COURSE_RATING + " and " + NineHoleRating.MAX_COURSE_RATING);
		}
	}

}
