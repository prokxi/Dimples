package com.prokxi.dimples.domain;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class CourseRatingTest {

	@ParameterizedTest
	@ValueSource(strings = { "25.0", "35.7", "71.3", "90.0" })
	void acceptsRatingsFrom25To90(String value) {
		assertThat(CourseRating.of(value).value()).isEqualTo(new BigDecimal(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "24.9", "90.1", "0", "-71.3" })
	void rejectsRatingsOutsideTheRange(String value) {
		assertThatIllegalArgumentException().isThrownBy(() -> CourseRating.of(value))
			.withMessage("Course Rating must be between 25.0 and 90.0");
	}

	@Test
	void rejectsMoreThanOneDecimalPlace() {
		assertThatIllegalArgumentException().isThrownBy(() -> CourseRating.of("71.35"))
			.withMessage("Course Rating must have at most one decimal place");
	}

	@ParameterizedTest
	@CsvSource({ "72, 72.0", "71.30, 71.3" })
	void normalizesToOneDecimalPlace(String value, String expected) {
		assertThat(CourseRating.of(value).value()).isEqualTo(new BigDecimal(expected));
		assertThat(CourseRating.of(value)).isEqualTo(CourseRating.of(expected));
	}

	@Test
	void rejectsNull() {
		assertThatNullPointerException().isThrownBy(() -> new CourseRating(null));
	}

	@ParameterizedTest
	@CsvSource({ "71.3, 35.7", "72.0, 36.0", "71.2, 35.6", "70.9, 35.5", "50.0, 25.0", "90.0, 45.0" })
	void halfRoundsHalfUpToOneDecimal(String rating, String half) {
		assertThat(CourseRating.of(rating).half()).isEqualTo(CourseRating.of(half));
		assertThat(CourseRating.of(rating).half().value()).isEqualTo(new BigDecimal(half));
	}

	@ParameterizedTest
	@CsvSource({ "30.0, true", "29.9, false", "40.0, true", "40.1, false", "35.0, true" })
	void isBetweenIncludesBothBounds(String rating, boolean between) {
		assertThat(CourseRating.of(rating).isBetween(new BigDecimal("30.0"), new BigDecimal("40.0")))
			.isEqualTo(between);
	}

}
