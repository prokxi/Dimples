package com.prokxi.dimples.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HoleTest {

	@Test
	void keepsItsValues() {
		Hole hole = new Hole(7, 5, 2, 480);

		assertThat(hole.number()).isEqualTo(7);
		assertThat(hole.par()).isEqualTo(5);
		assertThat(hole.strokeIndex()).isEqualTo(2);
		assertThat(hole.lengthMeters()).hasValue(480);
	}

	@Test
	void lengthIsOptional() {
		assertThat(new Hole(1, 4, 1, null).lengthMeters()).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 18 })
	void acceptsNumbersFrom1To18(int number) {
		assertThat(new Hole(number, 4, 1, null).number()).isEqualTo(number);
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 19 })
	void rejectsOtherNumbers(int number) {
		assertThatIllegalArgumentException().isThrownBy(() -> new Hole(number, 4, 1, null))
			.withMessage("Hole number must be between 1 and 18");
	}

	@ParameterizedTest
	@ValueSource(ints = { 3, 6 })
	void acceptsParFrom3To6(int par) {
		assertThat(new Hole(1, par, 1, null).par()).isEqualTo(par);
	}

	@ParameterizedTest
	@ValueSource(ints = { 2, 7 })
	void rejectsOtherPars(int par) {
		assertThatIllegalArgumentException().isThrownBy(() -> new Hole(1, par, 1, null))
			.withMessage("Par must be between 3 and 6");
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 18 })
	void acceptsStrokeIndexesFrom1To18(int strokeIndex) {
		assertThat(new Hole(1, 4, strokeIndex, null).strokeIndex()).isEqualTo(strokeIndex);
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 19 })
	void rejectsOtherStrokeIndexes(int strokeIndex) {
		assertThatIllegalArgumentException().isThrownBy(() -> new Hole(1, 4, strokeIndex, null))
			.withMessage("Stroke index must be between 1 and 18");
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 800 })
	void acceptsLengthsFrom1To800Meters(int lengthMeters) {
		assertThat(new Hole(1, 4, 1, lengthMeters).lengthMeters()).hasValue(lengthMeters);
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 801 })
	void rejectsOtherLengths(int lengthMeters) {
		assertThatIllegalArgumentException().isThrownBy(() -> new Hole(1, 4, 1, lengthMeters))
			.withMessage("Hole length must be between 1 and 800 meters");
	}

	@Test
	void holesWithTheSameValuesAreEqual() {
		assertThat(new Hole(3, 4, 5, 350)).isEqualTo(new Hole(3, 4, 5, 350))
			.hasSameHashCodeAs(new Hole(3, 4, 5, 350))
			.doesNotHaveSameHashCodeAs(new Hole(4, 4, 5, 350))
			.isNotEqualTo(new Hole(4, 4, 5, 350))
			.isNotEqualTo(new Hole(3, 5, 5, 350))
			.isNotEqualTo(new Hole(3, 4, 6, 350))
			.isNotEqualTo(new Hole(3, 4, 5, 351))
			.isNotEqualTo(new Hole(3, 4, 5, null))
			.isNotEqualTo("hole");
		assertThat(new Hole(3, 4, 5, null)).isEqualTo(new Hole(3, 4, 5, null));
		Hole hole = new Hole(3, 4, 5, 350);
		assertThat(hole).isEqualTo(hole);
	}

	@Test
	void describesItself() {
		assertThat(new Hole(3, 4, 5, 350)).hasToString("Hole[number=3, par=4, strokeIndex=5, lengthMeters=350]");
	}

}
