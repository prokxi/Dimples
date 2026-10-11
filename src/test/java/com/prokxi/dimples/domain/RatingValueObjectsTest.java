package com.prokxi.dimples.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class RatingValueObjectsTest {

	@Nested
	class SlopeRatings {

		@ParameterizedTest
		@ValueSource(ints = { 55, 113, 155 })
		void acceptsTheWhsRange(int value) {
			assertThat(new SlopeRating(value).value()).isEqualTo(value);
		}

		@ParameterizedTest
		@ValueSource(ints = { 54, 156, 0 })
		void rejectsValuesOutsideTheWhsRange(int value) {
			assertThatIllegalArgumentException().isThrownBy(() -> new SlopeRating(value))
				.withMessage("Slope Rating must be between 55 and 155");
		}

	}

	@Nested
	class NineHoleRatings {

		@ParameterizedTest
		@ValueSource(strings = { "25.0", "35.7", "45.0" })
		void acceptsCourseRatingsFrom25To45(String courseRating) {
			NineHoleRating rating = new NineHoleRating(CourseRating.of(courseRating), new SlopeRating(120));

			assertThat(rating.courseRating()).isEqualTo(CourseRating.of(courseRating));
			assertThat(rating.slopeRating()).isEqualTo(new SlopeRating(120));
		}

		@Test
		void rejectsCourseRatingsAbove45() {
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new NineHoleRating(CourseRating.of("45.1"), new SlopeRating(120)))
				.withMessage("The Course Rating of a nine must be between 25.0 and 45.0");
		}

		@Test
		void rejectsMissingRatings() {
			assertThatNullPointerException().isThrownBy(() -> new NineHoleRating(null, new SlopeRating(120)));
			assertThatNullPointerException().isThrownBy(() -> new NineHoleRating(CourseRating.of("35.0"), null));
		}

	}

	@Nested
	class TeeRatings {

		@Test
		void parIsTheSumOfTheHolesPars() {
			TeeRating rating = new TeeRating(CourseRating.of("35.7"), new SlopeRating(128), CourseFixtures.holes(9));

			assertThat(rating.par()).isEqualTo(36);
		}

		@Test
		void keepsAnUnmodifiableCopyOfTheHoles() {
			List<Hole> holes = new ArrayList<>(CourseFixtures.holes(9));
			TeeRating rating = new TeeRating(CourseRating.of("35.7"), new SlopeRating(128), holes);
			holes.clear();

			assertThat(rating.holes()).hasSize(9);
			assertThat(rating.holes()).isUnmodifiable();
		}

		@Test
		void rejectsAnEmptySelection() {
			assertThatIllegalArgumentException()
				.isThrownBy(() -> new TeeRating(CourseRating.of("35.7"), new SlopeRating(128), List.of()))
				.withMessage("A rating needs at least one hole");
		}

		@Test
		void rejectsMissingValues() {
			List<Hole> holes = CourseFixtures.holes(9);
			assertThatNullPointerException().isThrownBy(() -> new TeeRating(null, new SlopeRating(128), holes));
			assertThatNullPointerException().isThrownBy(() -> new TeeRating(CourseRating.of("35.7"), null, holes));
			assertThatNullPointerException()
				.isThrownBy(() -> new TeeRating(CourseRating.of("35.7"), new SlopeRating(128), null));
		}

	}

	@Nested
	class HoleCounts {

		@Test
		void mapsNumbersOfHoles() {
			assertThat(HoleCount.of(9)).isEqualTo(HoleCount.NINE);
			assertThat(HoleCount.of(18)).isEqualTo(HoleCount.EIGHTEEN);
			assertThat(HoleCount.NINE.holes()).isEqualTo(9);
			assertThat(HoleCount.EIGHTEEN.holes()).isEqualTo(18);
		}

		@ParameterizedTest
		@ValueSource(ints = { 0, 8, 10, 17, 19, 27 })
		void rejectsOtherNumbersOfHoles(int holes) {
			assertThatIllegalArgumentException().isThrownBy(() -> HoleCount.of(holes))
				.withMessage("A tee must have 9 or 18 holes");
		}

	}

	@Nested
	class Ids {

		@Test
		void generatesDistinctIds() {
			assertThat(CourseId.generate()).isNotEqualTo(CourseId.generate());
			assertThat(TeeId.generate()).isNotEqualTo(TeeId.generate());
			assertThat(GolferId.generate()).isNotEqualTo(GolferId.generate());
		}

		@Test
		void wrapsTheUuid() {
			UUID uuid = UUID.randomUUID();

			assertThat(new CourseId(uuid).value()).isEqualTo(uuid);
			assertThat(new TeeId(uuid).value()).isEqualTo(uuid);
			assertThat(new GolferId(uuid).value()).isEqualTo(uuid);
		}

		@Test
		void rejectsNull() {
			assertThatNullPointerException().isThrownBy(() -> new CourseId(null));
			assertThatNullPointerException().isThrownBy(() -> new TeeId(null));
			assertThatNullPointerException().isThrownBy(() -> new GolferId(null));
		}

	}

}
