package com.prokxi.dimples.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.prokxi.dimples.domain.CourseFixtures.holes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TeeTest {

	private static final TeeId ID = TeeId.generate();

	private static final CourseRating CR_18 = CourseRating.of("71.3");

	private static final CourseRating CR_9 = CourseRating.of("35.4");

	private static final SlopeRating SR = new SlopeRating(128);

	private static final NineHoleRating FRONT = new NineHoleRating(CourseRating.of("35.4"), new SlopeRating(125));

	private static final NineHoleRating BACK = new NineHoleRating(CourseRating.of("36.1"), new SlopeRating(131));

	private static Tee tee18(List<Hole> holes) {
		return new Tee(ID, "Yellow", CR_18, SR, null, null, holes);
	}

	private static Tee tee9(List<Hole> holes) {
		return new Tee(ID, "Yellow", CR_9, SR, null, null, holes);
	}

	@Nested
	class Creation {

		@Test
		void keepsItsValues() {
			Tee tee = new Tee(ID, "Yellow", CR_18, SR, FRONT, BACK, holes(18));

			assertThat(tee.id()).isEqualTo(ID);
			assertThat(tee.name()).isEqualTo("Yellow");
			assertThat(tee.courseRating()).isEqualTo(CR_18);
			assertThat(tee.slopeRating()).isEqualTo(SR);
			assertThat(tee.frontNine()).contains(FRONT);
			assertThat(tee.backNine()).contains(BACK);
			assertThat(tee.holes()).isEqualTo(holes(18));
			assertThat(tee.holeCount()).isEqualTo(HoleCount.EIGHTEEN);
			assertThat(tee.par()).isEqualTo(73);
		}

		@Test
		void nineHoleRatingsAreOptional() {
			Tee tee = tee18(holes(18));

			assertThat(tee.frontNine()).isEmpty();
			assertThat(tee.backNine()).isEmpty();
		}

		@Test
		void ordersHolesByNumber() {
			List<Hole> shuffled = new ArrayList<>(holes(18));
			Collections.reverse(shuffled);

			assertThat(tee18(shuffled).holes()).isEqualTo(holes(18));
		}

		@Test
		void keepsAnUnmodifiableCopyOfTheHoles() {
			List<Hole> holes = new ArrayList<>(holes(9));
			Tee tee = tee9(holes);
			holes.clear();

			assertThat(tee.holes()).hasSize(9).isUnmodifiable();
		}

		@Test
		void derivesTheHoleCountFromTheHoles() {
			assertThat(tee9(holes(9)).holeCount()).isEqualTo(HoleCount.NINE);
			assertThat(tee9(holes(9)).par()).isEqualTo(36);
		}

		@Test
		void stripsTheName() {
			assertThat(new Tee(ID, "  White ", CR_18, SR, null, null, holes(18)).name()).isEqualTo("White");
		}

		@ParameterizedTest
		@ValueSource(strings = { "W", "123456789012345678901234567890" })
		void acceptsNamesWith1To30Characters(String name) {
			assertThat(new Tee(ID, name, CR_18, SR, null, null, holes(18)).name()).isEqualTo(name);
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "   ", "1234567890123456789012345678901" })
		void rejectsNamesWithoutOrWithTooManyCharacters(String name) {
			assertThatIllegalArgumentException().isThrownBy(() -> new Tee(ID, name, CR_18, SR, null, null, holes(18)))
				.withMessage("Tee name must have 1 to 30 characters");
		}

		@Test
		void rejectsControlCharactersInTheName() {
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new Tee(ID, "Yel\tlow", CR_18, SR, null, null, holes(18)))
				.withMessage("Tee name must not contain control characters");
		}

		@Test
		void rejectsMissingValues() {
			List<Hole> holes = holes(18);
			assertThatNullPointerException().isThrownBy(() -> new Tee(null, "Yellow", CR_18, SR, null, null, holes));
			assertThatNullPointerException().isThrownBy(() -> new Tee(ID, null, CR_18, SR, null, null, holes));
			assertThatNullPointerException().isThrownBy(() -> new Tee(ID, "Yellow", null, SR, null, null, holes));
			assertThatNullPointerException().isThrownBy(() -> new Tee(ID, "Yellow", CR_18, null, null, null, holes));
			assertThatNullPointerException().isThrownBy(() -> new Tee(ID, "Yellow", CR_18, SR, null, null, null));
		}

	}

	@Nested
	class Holes {

		@ParameterizedTest
		@ValueSource(ints = { 0, 1, 8, 10, 17 })
		void rejectsOtherNumbersOfHolesThan9Or18(int count) {
			List<Hole> holes = new ArrayList<>();
			for (int number = 1; number <= count; number++) {
				holes.add(new Hole(number, 4, number, null));
			}

			assertThatIllegalArgumentException().isThrownBy(() -> tee18(holes))
				.withMessage("A tee must have 9 or 18 holes");
		}

		@Test
		void rejectsGapsInTheNumbering() {
			List<Hole> holes = new ArrayList<>(holes(9));
			holes.set(4, new Hole(10, 4, 5, null));

			assertThatIllegalArgumentException().isThrownBy(() -> tee9(holes))
				.withMessage("Holes must be numbered 1 to 9 without gaps");
		}

		@Test
		void rejectsDuplicateNumbers() {
			List<Hole> holes = new ArrayList<>(holes(18));
			holes.set(17, new Hole(17, 4, 1, null));

			assertThatIllegalArgumentException().isThrownBy(() -> tee18(holes))
				.withMessage("Holes must be numbered 1 to 18 without gaps");
		}

		@Test
		void rejectsNumberingThatDoesNotStartAt1() {
			List<Hole> holes = new ArrayList<>();
			for (int number = 10; number <= 18; number++) {
				holes.add(new Hole(number, 4, number - 9, null));
			}

			assertThatIllegalArgumentException().isThrownBy(() -> tee9(holes))
				.withMessage("Holes must be numbered 1 to 9 without gaps");
		}

		@Test
		void rejectsDuplicateStrokeIndexes() {
			List<Hole> holes = new ArrayList<>(holes(18));
			holes.set(0, new Hole(1, 4, 17, null));

			assertThatIllegalArgumentException().isThrownBy(() -> tee18(holes))
				.withMessage("Stroke indexes must be 1 to 18, each used once");
		}

		@Test
		void rejectsStrokeIndexesAboveTheNumberOfHoles() {
			List<Hole> holes = new ArrayList<>(holes(9));
			holes.set(0, new Hole(1, 4, 10, null));

			assertThatIllegalArgumentException().isThrownBy(() -> tee9(holes))
				.withMessage("Stroke indexes must be 1 to 9, each used once");
		}

		@Test
		void acceptsTheHighestStrokeIndexOnAnyHole() {
			List<Hole> holes = new ArrayList<>();
			for (int number = 1; number <= 9; number++) {
				holes.add(new Hole(number, 4, number, null));
			}

			assertThat(tee9(holes).holes()).extracting(Hole::strokeIndex).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
		}

	}

	@Nested
	class Ratings {

		@ParameterizedTest
		@ValueSource(strings = { "50.0", "71.3", "90.0" })
		void acceptsCourseRatingsFrom50To90On18Holes(String courseRating) {
			Tee tee = new Tee(ID, "Yellow", CourseRating.of(courseRating), SR, null, null, holes(18));

			assertThat(tee.courseRating()).isEqualTo(CourseRating.of(courseRating));
		}

		@ParameterizedTest
		@ValueSource(strings = { "49.9", "25.0", "45.0" })
		void rejectsCourseRatingsBelow50On18Holes(String courseRating) {
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new Tee(ID, "Yellow", CourseRating.of(courseRating), SR, null, null, holes(18)))
				.withMessage("The Course Rating of an 18-hole tee must be between 50.0 and 90.0");
		}

		@ParameterizedTest
		@ValueSource(strings = { "25.0", "35.4", "45.0" })
		void acceptsCourseRatingsFrom25To45On9Holes(String courseRating) {
			Tee tee = new Tee(ID, "Yellow", CourseRating.of(courseRating), SR, null, null, holes(9));

			assertThat(tee.courseRating()).isEqualTo(CourseRating.of(courseRating));
		}

		@ParameterizedTest
		@ValueSource(strings = { "45.1", "71.3" })
		void rejectsCourseRatingsAbove45On9Holes(String courseRating) {
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new Tee(ID, "Yellow", CourseRating.of(courseRating), SR, null, null, holes(9)))
				.withMessage("The Course Rating of a 9-hole tee must be between 25.0 and 45.0");
		}

		@Test
		void requiresFrontAndBackNineTogether() {
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new Tee(ID, "Yellow", CR_18, SR, FRONT, null, holes(18)))
				.withMessage("Nine-hole ratings need both the front and the back nine");
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new Tee(ID, "Yellow", CR_18, SR, null, BACK, holes(18)))
				.withMessage("Nine-hole ratings need both the front and the back nine");
		}

		@Test
		void rejectsNineHoleRatingsOn9HoleTees() {
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new Tee(ID, "Yellow", CR_9, SR, FRONT, BACK, holes(9)))
				.withMessage("Nine-hole ratings are only allowed on 18-hole tees");
		}

	}

	@Nested
	class RatingFor {

		@Test
		void allHolesOfAn18HoleTeeUseTheTeesRatings() {
			TeeRating rating = tee18(holes(18)).ratingFor(HoleSelection.ALL);

			assertThat(rating.courseRating()).isEqualTo(CourseRating.of("71.3"));
			assertThat(rating.slopeRating()).isEqualTo(new SlopeRating(128));
			assertThat(rating.par()).isEqualTo(73);
			assertThat(rating.holes()).isEqualTo(holes(18));
		}

		@Test
		void allHolesUseTheTeesRatingsEvenWithNineHoleRatings() {
			TeeRating rating = new Tee(ID, "Yellow", CR_18, SR, FRONT, BACK, holes(18)).ratingFor(HoleSelection.ALL);

			assertThat(rating.courseRating()).isEqualTo(CourseRating.of("71.3"));
			assertThat(rating.slopeRating()).isEqualTo(new SlopeRating(128));
			assertThat(rating.par()).isEqualTo(73);
		}

		@Test
		void frontNineWithoutNineHoleRatingsHalvesTheCourseRatingRoundingHalfUp() {
			TeeRating rating = tee18(holes(18)).ratingFor(HoleSelection.FRONT_NINE);

			assertThat(rating.courseRating()).isEqualTo(CourseRating.of("35.7"));
			assertThat(rating.slopeRating()).isEqualTo(new SlopeRating(128));
			assertThat(rating.par()).isEqualTo(36);
			assertThat(rating.holes()).isEqualTo(holes(18).subList(0, 9));
		}

		@Test
		void backNineWithoutNineHoleRatingsHalvesTheCourseRatingRoundingHalfUp() {
			TeeRating rating = tee18(holes(18)).ratingFor(HoleSelection.BACK_NINE);

			assertThat(rating.courseRating()).isEqualTo(CourseRating.of("35.7"));
			assertThat(rating.slopeRating()).isEqualTo(new SlopeRating(128));
			assertThat(rating.par()).isEqualTo(37);
			assertThat(rating.holes()).isEqualTo(holes(18).subList(9, 18));
		}

		@Test
		void frontNineUsesItsOwnRatings() {
			TeeRating rating = new Tee(ID, "Yellow", CR_18, SR, FRONT, BACK, holes(18))
				.ratingFor(HoleSelection.FRONT_NINE);

			assertThat(rating.courseRating()).isEqualTo(CourseRating.of("35.4"));
			assertThat(rating.slopeRating()).isEqualTo(new SlopeRating(125));
			assertThat(rating.par()).isEqualTo(36);
			assertThat(rating.holes()).extracting(Hole::number).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9);
		}

		@Test
		void backNineUsesItsOwnRatings() {
			TeeRating rating = new Tee(ID, "Yellow", CR_18, SR, FRONT, BACK, holes(18))
				.ratingFor(HoleSelection.BACK_NINE);

			assertThat(rating.courseRating()).isEqualTo(CourseRating.of("36.1"));
			assertThat(rating.slopeRating()).isEqualTo(new SlopeRating(131));
			assertThat(rating.par()).isEqualTo(37);
			assertThat(rating.holes()).extracting(Hole::number).containsExactly(10, 11, 12, 13, 14, 15, 16, 17, 18);
		}

		@Test
		void allHolesOfA9HoleTeeUseTheTeesRatings() {
			TeeRating rating = tee9(holes(9)).ratingFor(HoleSelection.ALL);

			assertThat(rating.courseRating()).isEqualTo(CourseRating.of("35.4"));
			assertThat(rating.slopeRating()).isEqualTo(new SlopeRating(128));
			assertThat(rating.par()).isEqualTo(36);
			assertThat(rating.holes()).isEqualTo(holes(9));
		}

		@ParameterizedTest
		@EnumSource(names = { "FRONT_NINE", "BACK_NINE" })
		void rejectsANineOnA9HoleTee(HoleSelection selection) {
			Tee tee = tee9(holes(9));

			assertThatIllegalArgumentException().isThrownBy(() -> tee.ratingFor(selection))
				.withMessage("The front and back nine exist only on 18-hole courses");
		}

		@Test
		void rejectsAMissingSelection() {
			assertThatNullPointerException().isThrownBy(() -> tee18(holes(18)).ratingFor(null));
		}

	}

	@Test
	void teesWithTheSameIdAreEqual() {
		Tee yellow = tee18(holes(18));
		Tee renamed = new Tee(ID, "White", CR_18, SR, null, null, holes(18));
		Tee other = new Tee(TeeId.generate(), "Yellow", CR_18, SR, null, null, holes(18));

		assertThat(yellow).isEqualTo(renamed)
			.hasSameHashCodeAs(renamed)
			.isNotEqualTo(other)
			.doesNotHaveSameHashCodeAs(other)
			.isNotEqualTo("tee");
		assertThat(yellow).isEqualTo(yellow);
	}

	@Test
	void describesItselfByIdAndName() {
		assertThat(tee18(holes(18))).hasToString("Tee[id=" + ID.value() + ", name=Yellow]");
	}

}
