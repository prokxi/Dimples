package com.prokxi.dimples.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DisplayNameTest {

	@Test
	void trimsTheValue() {
		assertThat(new DisplayName("  Max  ").value()).isEqualTo("Max");
	}

	@ParameterizedTest
	@ValueSource(strings = { "Mo", "Max Mustermann", "Jürgen 🏌️", "Zoë" })
	void acceptsValidNames(String value) {
		assertThat(new DisplayName(value).value()).isEqualTo(value);
	}

	@Test
	void acceptsMinimumAndMaximumLength() {
		assertThat(new DisplayName("ab").value()).hasSize(2);
		assertThat(new DisplayName("a".repeat(40)).value()).hasSize(40);
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "   ", "M", " M " })
	void rejectsBlankOrTooShortNames(String value) {
		assertThatIllegalArgumentException().isThrownBy(() -> new DisplayName(value)).withMessageContaining("2 to 40");
	}

	@Test
	void rejectsTooLongName() {
		assertThatIllegalArgumentException().isThrownBy(() -> new DisplayName("a".repeat(41)));
	}

	@Test
	void countsCharactersNotUtf16Units() {
		String fortyEmojis = "🏌".repeat(40);

		assertThat(new DisplayName(fortyEmojis).value()).isEqualTo(fortyEmojis);
		assertThatIllegalArgumentException().isThrownBy(() -> new DisplayName("🏌".repeat(41)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "Max\nMuster", "Max\tMuster", "Max\u0000", "Max\u007F", "Max\u0085x" })
	void rejectsControlCharacters(String value) {
		assertThatIllegalArgumentException().isThrownBy(() -> new DisplayName(value))
			.withMessageContaining("control characters");
	}

	@Test
	void rejectsNull() {
		assertThatNullPointerException().isThrownBy(() -> new DisplayName(null));
	}

}
