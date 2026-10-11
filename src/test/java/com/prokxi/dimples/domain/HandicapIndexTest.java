package com.prokxi.dimples.domain;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class HandicapIndexTest {

	@ParameterizedTest
	@ValueSource(strings = { "-10.0", "-2.3", "0.0", "12.4", "54.0" })
	void acceptsValuesInTheWhsRange(String value) {
		assertThat(HandicapIndex.of(value).value()).isEqualByComparingTo(value);
	}

	@ParameterizedTest
	@ValueSource(strings = { "-10.1", "54.1", "100", "-11" })
	void rejectsValuesOutsideTheWhsRange(String value) {
		assertThatIllegalArgumentException().isThrownBy(() -> HandicapIndex.of(value))
			.withMessageContaining("between +10.0 and 54.0");
	}

	@ParameterizedTest
	@ValueSource(strings = { "12.34", "0.05", "-2.31" })
	void rejectsMoreThanOneDecimalPlace(String value) {
		assertThatIllegalArgumentException().isThrownBy(() -> HandicapIndex.of(value))
			.withMessageContaining("one decimal place");
	}

	@Test
	void normalisesToOneDecimalPlace() {
		assertThat(HandicapIndex.of("12").value()).isEqualTo(new BigDecimal("12.0"));
		assertThat(HandicapIndex.of("12.30").value()).isEqualTo(new BigDecimal("12.3"));
	}

	@Test
	void valuesWithDifferentScalesAreEqual() {
		assertThat(HandicapIndex.of("12")).isEqualTo(HandicapIndex.of("12.0"));
	}

	@Test
	void rejectsNull() {
		assertThatNullPointerException().isThrownBy(() -> new HandicapIndex(null));
	}

}
