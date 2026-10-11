package com.prokxi.dimples.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class PasswordTest {

	@Test
	void acceptsMinimumAndMaximumLength() {
		assertThat(new Password("a".repeat(10)).value()).hasSize(10);
		assertThat(new Password("a".repeat(64)).value()).hasSize(64);
	}

	@Test
	void rejectsTooShortPassword() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Password("a".repeat(9)))
			.withMessageContaining("10 to 64");
	}

	@Test
	void rejectsTooLongPassword() {
		assertThatIllegalArgumentException().isThrownBy(() -> new Password("a".repeat(65)))
			.withMessageContaining("10 to 64");
	}

	@Test
	void countsCharactersNotUtf16Units() {
		// 5 emojis are 10 UTF-16 units but only 5 characters.
		assertThatIllegalArgumentException().isThrownBy(() -> new Password("🏌".repeat(5)));
	}

	@Test
	void acceptsExactly72Bytes() {
		// "ä" is 2 bytes in UTF-8: 36 characters, 72 bytes.
		assertThat(new Password("ä".repeat(36)).value()).hasSize(36);
	}

	@Test
	void rejectsMoreThan72Bytes() {
		// 37 characters, 74 bytes: within the character limit but over the bcrypt limit.
		assertThatIllegalArgumentException().isThrownBy(() -> new Password("ä".repeat(37)))
			.withMessageContaining("72 bytes");
	}

	@Test
	void doesNotTrim() {
		assertThat(new Password(" secret pw ").value()).isEqualTo(" secret pw ");
	}

	@Test
	void rejectsNull() {
		assertThatNullPointerException().isThrownBy(() -> new Password(null));
	}

	@Test
	void toStringHidesThePassword() {
		assertThat(new Password("correct horse battery")).hasToString("Password[***]");
	}

}
