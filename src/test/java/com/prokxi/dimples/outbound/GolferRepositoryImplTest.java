package com.prokxi.dimples.outbound;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.prokxi.dimples.TestcontainersConfiguration;
import com.prokxi.dimples.domain.DisplayName;
import com.prokxi.dimples.domain.DistanceUnit;
import com.prokxi.dimples.domain.EmailAddress;
import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.GolferId;
import com.prokxi.dimples.domain.GolferRole;
import com.prokxi.dimples.domain.GolferStatus;
import com.prokxi.dimples.domain.HandicapIndex;
import com.prokxi.dimples.domain.HandicapIndexEntry;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJdbcTest
@Import({ TestcontainersConfiguration.class, GolferRepositoryImpl.class })
class GolferRepositoryImplTest {

	private static final Instant NOW = Instant.parse("2026-06-15T12:00:00.123456Z");

	private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

	@Autowired
	private GolferRepositoryImpl repository;

	@Autowired
	private JdbcClient jdbc;

	private static Golfer newGolfer(String email) {
		return Golfer.register(GolferId.generate(), new EmailAddress(email), "{bcrypt}$2a$10$hash",
				new DisplayName("Max"), NOW);
	}

	@Test
	void insertsANewGolferWithItsPresetId() {
		Golfer golfer = newGolfer("max@example.com");

		Golfer saved = this.repository.save(golfer);

		assertThat(saved.id()).isEqualTo(golfer.id());
		assertThat(saved.version()).isPositive();
		assertThat(this.jdbc.sql("select count(*) from golfer").query(Integer.class).single()).isOne();
	}

	@Test
	void roundTripKeepsAllFieldsIncludingTheHistory() {
		Golfer golfer = new Golfer(GolferId.generate(), new EmailAddress("max@example.com"), "{bcrypt}hash",
				new DisplayName("Max Muster"), DistanceUnit.YARDS, List.of(), GolferStatus.ACTIVE, GolferRole.OPERATOR,
				true, NOW, null, 0)
			.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("-2.3"), LocalDate.of(2026, 3, 1), TODAY, NOW)
			.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("54.0"), LocalDate.of(2026, 5, 1), TODAY, NOW);

		Golfer saved = this.repository.save(golfer);
		Golfer loaded = this.repository.findById(golfer.id()).orElseThrow();

		assertThat(loaded.email()).contains(new EmailAddress("max@example.com"));
		assertThat(loaded.passwordHash()).contains("{bcrypt}hash");
		assertThat(loaded.displayName()).contains(new DisplayName("Max Muster"));
		assertThat(loaded.distanceUnit()).isEqualTo(DistanceUnit.YARDS);
		assertThat(loaded.status()).isEqualTo(GolferStatus.ACTIVE);
		assertThat(loaded.role()).isEqualTo(GolferRole.OPERATOR);
		assertThat(loaded.mustChangePassword()).isTrue();
		assertThat(loaded.createdAt()).isEqualTo(NOW);
		assertThat(loaded.deletedAt()).isEmpty();
		assertThat(loaded.version()).isEqualTo(saved.version());
		assertThat(loaded.handicapIndexes()).containsExactlyElementsOf(golfer.handicapIndexes());
	}

	@Test
	void updatesAnExistingGolfer() {
		Golfer saved = this.repository.save(newGolfer("max@example.com"));
		HandicapIndexEntry first = new HandicapIndexEntry(UUID.randomUUID(), HandicapIndex.of("18.0"),
				LocalDate.of(2026, 1, 1), NOW);
		Golfer withEntry = this.repository
			.save(saved.recordHandicapIndex(first.id(), first.value(), first.effectiveOn(), TODAY, NOW));

		Golfer changed = withEntry.updateProfile(new DisplayName("Maxi"), DistanceUnit.YARDS)
			.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("18.5"), first.effectiveOn(), TODAY,
					NOW.plus(1, ChronoUnit.DAYS))
			.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("17.2"), TODAY, TODAY, NOW);
		Golfer updated = this.repository.save(changed);
		Golfer loaded = this.repository.findById(saved.id()).orElseThrow();

		assertThat(updated.version()).isGreaterThan(withEntry.version());
		assertThat(loaded.displayName()).contains(new DisplayName("Maxi"));
		assertThat(loaded.distanceUnit()).isEqualTo(DistanceUnit.YARDS);
		assertThat(loaded.handicapIndexes()).containsExactlyElementsOf(changed.handicapIndexes());
		assertThat(loaded.findHandicapIndexEntry(first.id()).orElseThrow().value()).isEqualTo(HandicapIndex.of("18.5"));
		assertThat(this.jdbc.sql("select count(*) from golfer").query(Integer.class).single()).isOne();
	}

	@Test
	void rejectsAnUpdateBasedOnAnOutdatedVersion() {
		Golfer saved = this.repository.save(newGolfer("max@example.com"));
		this.repository.save(saved.updateProfile(new DisplayName("First"), DistanceUnit.METERS));

		Golfer outdated = saved.updateProfile(new DisplayName("Second"), DistanceUnit.METERS);

		assertThatExceptionOfType(OptimisticLockingFailureException.class)
			.isThrownBy(() -> this.repository.save(outdated));
	}

	@Test
	void findsActiveGolferByEmail() {
		Golfer saved = this.repository.save(newGolfer("max@example.com"));

		assertThat(this.repository.findActiveByEmail(new EmailAddress("MAX@example.com")))
			.hasValueSatisfying((golfer) -> assertThat(golfer.id()).isEqualTo(saved.id()));
		assertThat(this.repository.findActiveByEmail(new EmailAddress("other@example.com"))).isEmpty();
	}

	@Test
	void findByIdReturnsEmptyForUnknownGolfer() {
		assertThat(this.repository.findById(GolferId.generate())).isEmpty();
	}

	@Test
	void storesADeletedGolferWithoutPersonalData() {
		Golfer saved = this.repository.save(newGolfer("max@example.com").recordHandicapIndex(UUID.randomUUID(),
				HandicapIndex.of("12.0"), TODAY, TODAY, NOW));

		this.repository.save(saved.delete(NOW));
		Golfer loaded = this.repository.findById(saved.id()).orElseThrow();

		assertThat(loaded.status()).isEqualTo(GolferStatus.DELETED);
		assertThat(loaded.email()).isEmpty();
		assertThat(loaded.displayName()).isEmpty();
		assertThat(loaded.handicapIndexes()).isEmpty();
		assertThat(this.jdbc.sql("select count(*) from handicap_index_entry").query(Integer.class).single()).isZero();
		assertThat(this.repository.findActiveByEmail(new EmailAddress("max@example.com"))).isEmpty();
	}

	@Test
	void emailIsUniqueAmongActiveGolfers() {
		this.repository.save(newGolfer("max@example.com"));

		assertThatExceptionOfType(DataIntegrityViolationException.class)
			.isThrownBy(() -> this.repository.save(newGolfer("max@example.com")));
	}

	@Test
	void emailOfADeletedGolferCanBeUsedAgain() {
		Golfer first = this.repository.save(newGolfer("max@example.com"));
		this.repository.save(first.delete(NOW));

		Golfer second = this.repository.save(newGolfer("max@example.com"));

		assertThat(this.repository.findActiveByEmail(new EmailAddress("max@example.com")))
			.hasValueSatisfying((golfer) -> assertThat(golfer.id()).isEqualTo(second.id()));
	}

}
