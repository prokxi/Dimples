package com.prokxi.dimples.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.prokxi.dimples.domain.DisplayName;
import com.prokxi.dimples.domain.DistanceUnit;
import com.prokxi.dimples.domain.EmailAddress;
import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.GolferId;
import com.prokxi.dimples.domain.GolferRole;
import com.prokxi.dimples.domain.GolferStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class GolferUserDetailsServiceTest {

	private static final EmailAddress EMAIL = new EmailAddress("max@example.com");

	@Mock
	private GolferRepository golferRepository;

	private GolferUserDetailsService service() {
		return new GolferUserDetailsService(this.golferRepository);
	}

	@Test
	void loadsAnActiveGolferByNormalisedEmail() {
		Golfer golfer = new Golfer(GolferId.generate(), EMAIL, "{bcrypt}hash", new DisplayName("Max"),
				DistanceUnit.METERS, List.of(), GolferStatus.ACTIVE, GolferRole.OPERATOR, false, Instant.EPOCH, null,
				1);
		given(this.golferRepository.findActiveByEmail(EMAIL)).willReturn(Optional.of(golfer));

		GolferUserDetails user = service().loadUserByUsername(" MAX@example.com ");

		assertThat(user.golferId()).isEqualTo(golfer.id());
		assertThat(user.getUsername()).isEqualTo("max@example.com");
		assertThat(user.getPassword()).isEqualTo("{bcrypt}hash");
		assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_OPERATOR");
		assertThat(user.toString()).doesNotContain("max@example.com").doesNotContain("hash");
	}

	@Test
	void erasesTheCredentials() {
		Golfer golfer = Golfer.register(GolferId.generate(), EMAIL, "{bcrypt}hash", new DisplayName("Max"),
				Instant.EPOCH);
		given(this.golferRepository.findActiveByEmail(EMAIL)).willReturn(Optional.of(golfer));
		GolferUserDetails user = service().loadUserByUsername("max@example.com");

		user.eraseCredentials();

		assertThat(user.getPassword()).isNull();
		assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_GOLFER");
	}

	@Test
	void rejectsAnUnknownEmail() {
		given(this.golferRepository.findActiveByEmail(EMAIL)).willReturn(Optional.empty());

		assertThatExceptionOfType(UsernameNotFoundException.class)
			.isThrownBy(() -> service().loadUserByUsername("max@example.com"));
	}

	@Test
	void rejectsAnInvalidEmailWithoutQueryingTheDatabase() {
		assertThatExceptionOfType(UsernameNotFoundException.class)
			.isThrownBy(() -> service().loadUserByUsername("not an email"));
		then(this.golferRepository).should(never()).findActiveByEmail(any());
	}

}
