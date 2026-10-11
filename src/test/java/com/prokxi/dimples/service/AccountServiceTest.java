package com.prokxi.dimples.service;

import java.time.Instant;
import java.util.Optional;

import com.prokxi.dimples.domain.DisplayName;
import com.prokxi.dimples.domain.EmailAddress;
import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.GolferId;
import com.prokxi.dimples.domain.GolferStatus;
import com.prokxi.dimples.domain.Password;
import com.prokxi.dimples.exception.ConflictException;
import com.prokxi.dimples.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	private static final Instant NOW = Instant.parse("2026-06-15T12:00:00Z");

	private static final EmailAddress EMAIL = new EmailAddress("max@example.com");

	private static final Password PASSWORD = new Password("correct horse");

	private static final DisplayName NAME = new DisplayName("Max");

	@Mock
	private GolferRepository golferRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private AccountService service() {
		return new AccountService(this.golferRepository, this.passwordEncoder, new MutableClock(NOW));
	}

	private static Golfer golfer() {
		return Golfer.register(GolferId.generate(), EMAIL, "{bcrypt}hash", NAME, NOW);
	}

	@Test
	void registerStoresTheHashedPassword() {
		given(this.golferRepository.findActiveByEmail(EMAIL)).willReturn(Optional.empty());
		given(this.passwordEncoder.encode("correct horse")).willReturn("{bcrypt}hash");
		given(this.golferRepository.save(any())).willAnswer((invocation) -> invocation.getArgument(0));

		Golfer golfer = service().register(EMAIL, PASSWORD, NAME);

		ArgumentCaptor<Golfer> saved = ArgumentCaptor.forClass(Golfer.class);
		then(this.golferRepository).should().save(saved.capture());
		assertThat(saved.getValue().passwordHash()).contains("{bcrypt}hash");
		assertThat(saved.getValue().email()).contains(EMAIL);
		assertThat(saved.getValue().displayName()).contains(NAME);
		assertThat(saved.getValue().createdAt()).isEqualTo(NOW);
		assertThat(saved.getValue().isActive()).isTrue();
		assertThat(golfer).isSameAs(saved.getValue());
	}

	@Test
	void registerReturnsTheStoredGolfer() {
		Golfer stored = golfer();
		given(this.golferRepository.findActiveByEmail(EMAIL)).willReturn(Optional.empty());
		given(this.passwordEncoder.encode(any())).willReturn("{bcrypt}hash");
		given(this.golferRepository.save(any())).willReturn(stored);

		assertThat(service().register(EMAIL, PASSWORD, NAME)).isSameAs(stored);
	}

	@Test
	void registerRejectsAnEmailOfAnActiveGolfer() {
		given(this.golferRepository.findActiveByEmail(EMAIL)).willReturn(Optional.of(golfer()));

		assertThatExceptionOfType(ConflictException.class).isThrownBy(() -> service().register(EMAIL, PASSWORD, NAME))
			.withMessage("Email address is already in use.");
		then(this.golferRepository).should(never()).save(any());
	}

	@Test
	void registerTurnsALostRaceIntoAConflict() {
		given(this.golferRepository.findActiveByEmail(EMAIL)).willReturn(Optional.empty());
		given(this.passwordEncoder.encode(any())).willReturn("{bcrypt}hash");
		given(this.golferRepository.save(any())).willThrow(new DuplicateKeyException("duplicate"));

		assertThatExceptionOfType(ConflictException.class).isThrownBy(() -> service().register(EMAIL, PASSWORD, NAME));
	}

	@Test
	void getGolferReturnsAnActiveGolfer() {
		Golfer golfer = golfer();
		given(this.golferRepository.findById(golfer.id())).willReturn(Optional.of(golfer));

		assertThat(service().getGolfer(golfer.id())).isSameAs(golfer);
	}

	@Test
	void getGolferRejectsAnUnknownGolfer() {
		GolferId id = GolferId.generate();
		given(this.golferRepository.findById(id)).willReturn(Optional.empty());

		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service().getGolfer(id));
	}

	@Test
	void getGolferRejectsADeletedGolfer() {
		Golfer deleted = golfer().delete(NOW);
		given(this.golferRepository.findById(deleted.id())).willReturn(Optional.of(deleted));

		assertThat(deleted.status()).isEqualTo(GolferStatus.DELETED);
		assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> service().getGolfer(deleted.id()));
	}

}
