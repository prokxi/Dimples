package com.prokxi.dimples.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EmailAddressTest {

	@Test
	void normalisesByTrimmingAndLowerCasing() {
		assertThat(new EmailAddress("  Max.Muster@Example.COM \t").value()).isEqualTo("max.muster@example.com");
	}

	@Test
	void equalAddressesDifferingOnlyInCaseAreEqual() {
		assertThat(new EmailAddress("A@B.de")).isEqualTo(new EmailAddress("a@b.de"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "   ", "plain", "@example.com", "max@", "max@example", "max@@example.com",
			"max muster@example.com", "max@exa mple.com", "max@example." })
	void rejectsInvalidFormat(String value) {
		assertThatIllegalArgumentException().isThrownBy(() -> new EmailAddress(value));
	}

	@Test
	void acceptsMaximumLength() {
		String value = "a".repeat(EmailAddress.MAX_LENGTH - "@example.com".length()) + "@example.com";

		assertThat(new EmailAddress(value).value()).hasSize(254);
	}

	@Test
	void rejectsTooLongAddress() {
		String value = "a".repeat(EmailAddress.MAX_LENGTH + 1 - "@example.com".length()) + "@example.com";

		assertThatIllegalArgumentException().isThrownBy(() -> new EmailAddress(value))
			.withMessageContaining("at most 254");
	}

	@Test
	void lengthIsCheckedAfterTrimming() {
		String value = " " + "a".repeat(EmailAddress.MAX_LENGTH - "@example.com".length()) + "@example.com ";

		assertThat(new EmailAddress(value).value()).hasSize(254);
	}

	@Test
	void rejectsNull() {
		assertThatNullPointerException().isThrownBy(() -> new EmailAddress(null));
	}

	@Test
	void toStringDoesNotRevealTheAddress() {
		assertThat(new EmailAddress("max@example.com")).hasToString("EmailAddress[***]");
	}

}
