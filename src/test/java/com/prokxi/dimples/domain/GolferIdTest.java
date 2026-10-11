package com.prokxi.dimples.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class GolferIdTest {

	@Test
	void generatesRandomIds() {
		assertThat(GolferId.generate()).isNotEqualTo(GolferId.generate());
		assertThat(GolferId.generate().value()).isNotNull();
	}

	@Test
	void rejectsNull() {
		assertThatNullPointerException().isThrownBy(() -> new GolferId(null));
	}

}
