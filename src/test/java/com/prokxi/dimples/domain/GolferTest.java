package com.prokxi.dimples.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class GolferTest {

	private static final GolferId ID = GolferId.generate();

	private static final EmailAddress EMAIL = new EmailAddress("max@example.com");

	private static final String HASH = "{bcrypt}$2a$10$hash";

	private static final DisplayName NAME = new DisplayName("Max");

	private static final Instant CREATED = Instant.parse("2026-01-10T08:00:00Z");

	private static final Instant NOW = Instant.parse("2026-06-15T12:00:00Z");

	private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

	private static Golfer registered() {
		return Golfer.register(ID, EMAIL, HASH, NAME, CREATED);
	}

	private static Golfer withHistory() {
		return registered()
			.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("18.2"), LocalDate.of(2026, 3, 1), TODAY, NOW)
			.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("16.5"), LocalDate.of(2026, 5, 1), TODAY, NOW)
			.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("17.0"), LocalDate.of(2026, 4, 1), TODAY, NOW);
	}

	@Nested
	class Registration {

		@Test
		void createsAnActiveGolferWithDefaults() {
			Golfer golfer = registered();

			assertThat(golfer.id()).isEqualTo(ID);
			assertThat(golfer.email()).contains(EMAIL);
			assertThat(golfer.passwordHash()).contains(HASH);
			assertThat(golfer.displayName()).contains(NAME);
			assertThat(golfer.distanceUnit()).isEqualTo(DistanceUnit.METERS);
			assertThat(golfer.handicapIndexes()).isEmpty();
			assertThat(golfer.status()).isEqualTo(GolferStatus.ACTIVE);
			assertThat(golfer.isActive()).isTrue();
			assertThat(golfer.role()).isEqualTo(GolferRole.GOLFER);
			assertThat(golfer.mustChangePassword()).isFalse();
			assertThat(golfer.createdAt()).isEqualTo(CREATED);
			assertThat(golfer.deletedAt()).isEmpty();
			assertThat(golfer.version()).isZero();
		}

		@Test
		void requiresPersonalData() {
			assertThatNullPointerException().isThrownBy(() -> Golfer.register(ID, null, HASH, NAME, CREATED))
				.withMessageContaining("email");
			assertThatNullPointerException().isThrownBy(() -> Golfer.register(ID, EMAIL, HASH, null, CREATED))
				.withMessageContaining("displayName");
			assertThatIllegalArgumentException().isThrownBy(() -> Golfer.register(ID, EMAIL, null, NAME, CREATED));
			assertThatIllegalArgumentException().isThrownBy(() -> Golfer.register(ID, EMAIL, " ", NAME, CREATED));
		}

		@Test
		void requiresIdAndCreationTime() {
			assertThatNullPointerException().isThrownBy(() -> Golfer.register(null, EMAIL, HASH, NAME, CREATED))
				.withMessageContaining("id");
			assertThatNullPointerException().isThrownBy(() -> Golfer.register(ID, EMAIL, HASH, NAME, null))
				.withMessageContaining("createdAt");
		}

	}

	@Nested
	class Restoring {

		@Test
		void restoresAllFields() {
			HandicapIndexEntry entry = new HandicapIndexEntry(UUID.randomUUID(), HandicapIndex.of("-2.3"),
					LocalDate.of(2026, 2, 1), CREATED);

			Golfer golfer = new Golfer(ID, EMAIL, HASH, NAME, DistanceUnit.YARDS, List.of(entry), GolferStatus.ACTIVE,
					GolferRole.OPERATOR, true, CREATED, null, 3);

			assertThat(golfer.distanceUnit()).isEqualTo(DistanceUnit.YARDS);
			assertThat(golfer.handicapIndexes()).containsExactly(entry);
			assertThat(golfer.role()).isEqualTo(GolferRole.OPERATOR);
			assertThat(golfer.mustChangePassword()).isTrue();
			assertThat(golfer.version()).isEqualTo(3);
		}

		@Test
		void changesKeepTheVersion() {
			Golfer golfer = new Golfer(ID, EMAIL, HASH, NAME, DistanceUnit.METERS, List.of(), GolferStatus.ACTIVE,
					GolferRole.GOLFER, false, CREATED, null, 7);

			assertThat(golfer.updateProfile(NAME, DistanceUnit.YARDS).version()).isEqualTo(7);
			assertThat(golfer.changePassword(HASH).version()).isEqualTo(7);
			assertThat(golfer.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("12.0"), TODAY, TODAY, NOW)
				.version()).isEqualTo(7);
			assertThat(golfer.delete(NOW).version()).isEqualTo(7);
		}

		@Test
		void requiresStatusRoleAndUnit() {
			assertThatNullPointerException()
				.isThrownBy(() -> new Golfer(ID, EMAIL, HASH, NAME, null, List.of(), GolferStatus.ACTIVE,
						GolferRole.GOLFER, false, CREATED, null, 0))
				.withMessageContaining("distanceUnit");
			assertThatNullPointerException()
				.isThrownBy(() -> new Golfer(ID, EMAIL, HASH, NAME, DistanceUnit.METERS, List.of(), null,
						GolferRole.GOLFER, false, CREATED, null, 0))
				.withMessageContaining("status");
			assertThatNullPointerException()
				.isThrownBy(() -> new Golfer(ID, EMAIL, HASH, NAME, DistanceUnit.METERS, List.of(), GolferStatus.ACTIVE,
						null, false, CREATED, null, 0))
				.withMessageContaining("role");
		}

		@Test
		void rejectsActiveGolferWithDeletionTime() {
			assertThatIllegalArgumentException().isThrownBy(() -> new Golfer(ID, EMAIL, HASH, NAME, DistanceUnit.METERS,
					List.of(), GolferStatus.ACTIVE, GolferRole.GOLFER, false, CREATED, NOW, 0));
		}

		@Test
		void restoresDeletedGolfer() {
			Golfer golfer = new Golfer(ID, null, null, null, DistanceUnit.METERS, List.of(), GolferStatus.DELETED,
					GolferRole.GOLFER, false, CREATED, NOW, 0);

			assertThat(golfer.isActive()).isFalse();
			assertThat(golfer.deletedAt()).contains(NOW);
		}

		@Test
		void rejectsDeletedGolferWithPersonalData() {
			HandicapIndexEntry entry = new HandicapIndexEntry(UUID.randomUUID(), HandicapIndex.of("12.0"),
					LocalDate.of(2026, 2, 1), CREATED);

			assertThatIllegalArgumentException().isThrownBy(() -> deleted(EMAIL, null, null, List.of()));
			assertThatIllegalArgumentException().isThrownBy(() -> deleted(null, HASH, null, List.of()));
			assertThatIllegalArgumentException().isThrownBy(() -> deleted(null, null, NAME, List.of()));
			assertThatIllegalArgumentException().isThrownBy(() -> deleted(null, null, null, List.of(entry)));
		}

		@Test
		void rejectsDeletedGolferWithoutDeletionTime() {
			assertThatNullPointerException()
				.isThrownBy(() -> new Golfer(ID, null, null, null, DistanceUnit.METERS, List.of(), GolferStatus.DELETED,
						GolferRole.GOLFER, false, CREATED, null, 0))
				.withMessageContaining("deletedAt");
		}

		@Test
		void rejectsDuplicateEffectiveDatesOrIds() {
			LocalDate date = LocalDate.of(2026, 2, 1);
			UUID id = UUID.randomUUID();
			var first = new HandicapIndexEntry(id, HandicapIndex.of("12.0"), date, CREATED);
			var sameDate = new HandicapIndexEntry(UUID.randomUUID(), HandicapIndex.of("13.0"), date, CREATED);
			var sameId = new HandicapIndexEntry(id, HandicapIndex.of("13.0"), date.plusDays(1), CREATED);

			assertThatIllegalArgumentException().isThrownBy(() -> active(List.of(first, sameDate)));
			assertThatIllegalArgumentException().isThrownBy(() -> active(List.of(first, sameId)));
		}

		private Golfer deleted(EmailAddress email, String hash, DisplayName name, List<HandicapIndexEntry> entries) {
			return new Golfer(ID, email, hash, name, DistanceUnit.METERS, entries, GolferStatus.DELETED,
					GolferRole.GOLFER, false, CREATED, NOW, 0);
		}

		private Golfer active(List<HandicapIndexEntry> entries) {
			return new Golfer(ID, EMAIL, HASH, NAME, DistanceUnit.METERS, entries, GolferStatus.ACTIVE,
					GolferRole.GOLFER, false, CREATED, null, 0);
		}

	}

	@Nested
	class HandicapIndexHistory {

		@Test
		void hasNoHandicapIndexWithoutEntries() {
			Golfer golfer = registered();

			assertThat(golfer.currentHandicapIndex(TODAY)).isEmpty();
			assertThat(golfer.handicapIndexOn(TODAY)).isEmpty();
		}

		@Test
		void currentHandicapIndexIsTheLatestEntry() {
			assertThat(withHistory().currentHandicapIndex(TODAY)).contains(HandicapIndex.of("16.5"));
		}

		@Test
		void handicapIndexOnADateIsTheLatestEntryOnOrBeforeIt() {
			Golfer golfer = withHistory();

			assertThat(golfer.handicapIndexOn(LocalDate.of(2026, 4, 30))).contains(HandicapIndex.of("17.0"));
			assertThat(golfer.handicapIndexOn(LocalDate.of(2026, 4, 1))).contains(HandicapIndex.of("17.0"));
			assertThat(golfer.handicapIndexOn(LocalDate.of(2026, 3, 31))).contains(HandicapIndex.of("18.2"));
			assertThat(golfer.handicapIndexOn(LocalDate.of(2026, 5, 1))).contains(HandicapIndex.of("16.5"));
		}

		@Test
		void hasNoHandicapIndexBeforeTheFirstEntry() {
			assertThat(withHistory().handicapIndexOn(LocalDate.of(2026, 2, 28))).isEmpty();
		}

		@Test
		void currentHandicapIndexIgnoresEntriesAfterToday() {
			HandicapIndexEntry past = new HandicapIndexEntry(UUID.randomUUID(), HandicapIndex.of("20.0"),
					TODAY.minusDays(1), NOW);
			HandicapIndexEntry future = new HandicapIndexEntry(UUID.randomUUID(), HandicapIndex.of("10.0"),
					TODAY.plusDays(1), NOW);
			Golfer golfer = new Golfer(ID, EMAIL, HASH, NAME, DistanceUnit.METERS, List.of(future, past),
					GolferStatus.ACTIVE, GolferRole.GOLFER, false, CREATED, null, 0);

			assertThat(golfer.currentHandicapIndex(TODAY)).contains(HandicapIndex.of("20.0"));
		}

		@Test
		void listsHistoryNewestFirst() {
			assertThat(withHistory().handicapIndexes()).extracting(HandicapIndexEntry::effectiveOn)
				.containsExactly(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 3, 1));
		}

		@Test
		void historyIsUnmodifiable() {
			Golfer golfer = withHistory();

			assertThat(golfer.handicapIndexes()).isUnmodifiable();
		}

		@Test
		void recordsANewEntry() {
			UUID entryId = UUID.randomUUID();

			Golfer golfer = registered().recordHandicapIndex(entryId, HandicapIndex.of("-2.3"), TODAY, TODAY, NOW);

			assertThat(golfer.handicapIndexes())
				.containsExactly(new HandicapIndexEntry(entryId, HandicapIndex.of("-2.3"), TODAY, NOW));
		}

		@Test
		void doesNotChangeTheOriginalGolfer() {
			Golfer golfer = registered();

			golfer.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("12.0"), TODAY, TODAY, NOW);

			assertThat(golfer.handicapIndexes()).isEmpty();
		}

		@Test
		void rejectsAFutureEffectiveDate() {
			Golfer golfer = registered();

			assertThatIllegalArgumentException()
				.isThrownBy(() -> golfer.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("12.0"),
						TODAY.plusDays(1), TODAY, NOW))
				.withMessageContaining("future");
		}

		@Test
		void replacesTheValueOfAnExistingEffectiveDate() {
			Golfer golfer = withHistory();
			HandicapIndexEntry original = golfer.handicapIndexes().get(1);
			Instant later = NOW.plusSeconds(60);

			Golfer corrected = golfer.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("17.1"),
					original.effectiveOn(), TODAY, later);

			assertThat(corrected.handicapIndexes()).hasSize(3);
			assertThat(corrected.findHandicapIndexEntry(original.id())).contains(
					new HandicapIndexEntry(original.id(), HandicapIndex.of("17.1"), original.effectiveOn(), later));
		}

		@Test
		void findsEntryById() {
			Golfer golfer = withHistory();
			HandicapIndexEntry entry = golfer.handicapIndexes().get(2);

			assertThat(golfer.findHandicapIndexEntry(entry.id())).contains(entry);
			assertThat(golfer.findHandicapIndexEntry(UUID.randomUUID())).isEmpty();
		}

		@Test
		void removesAnEntry() {
			Golfer golfer = withHistory();
			HandicapIndexEntry newest = golfer.handicapIndexes().get(0);

			Golfer changed = golfer.removeHandicapIndex(newest.id());

			assertThat(changed.handicapIndexes()).hasSize(2).doesNotContain(newest);
			assertThat(changed.currentHandicapIndex(TODAY)).contains(HandicapIndex.of("17.0"));
		}

		@Test
		void rejectsRemovingAnUnknownEntry() {
			Golfer golfer = withHistory();

			assertThatIllegalArgumentException().isThrownBy(() -> golfer.removeHandicapIndex(UUID.randomUUID()));
		}

	}

	@Nested
	class ProfileAndPassword {

		@Test
		void updatesDisplayNameAndDistanceUnit() {
			Golfer golfer = withHistory().updateProfile(new DisplayName("Maxi"), DistanceUnit.YARDS);

			assertThat(golfer.displayName()).contains(new DisplayName("Maxi"));
			assertThat(golfer.distanceUnit()).isEqualTo(DistanceUnit.YARDS);
			assertThat(golfer.handicapIndexes()).hasSize(3);
			assertThat(golfer.email()).contains(EMAIL);
		}

		@Test
		void resetPasswordForcesAChange() {
			Golfer golfer = registered().resetPassword("{bcrypt}temporary");

			assertThat(golfer.passwordHash()).contains("{bcrypt}temporary");
			assertThat(golfer.mustChangePassword()).isTrue();
		}

		@Test
		void changePasswordLiftsTheForcedChange() {
			Golfer golfer = registered().resetPassword("{bcrypt}temporary").changePassword("{bcrypt}new");

			assertThat(golfer.passwordHash()).contains("{bcrypt}new");
			assertThat(golfer.mustChangePassword()).isFalse();
		}

		@Test
		void rejectsBlankPasswordHash() {
			Golfer golfer = registered();

			assertThatIllegalArgumentException().isThrownBy(() -> golfer.changePassword(""));
			assertThatIllegalArgumentException().isThrownBy(() -> golfer.resetPassword(null));
		}

	}

	@Nested
	class Deletion {

		@Test
		void removesAllPersonalData() {
			Golfer golfer = withHistory().updateProfile(NAME, DistanceUnit.YARDS).resetPassword("{bcrypt}temp");

			Golfer deleted = golfer.delete(NOW);

			assertThat(deleted.id()).isEqualTo(ID);
			assertThat(deleted.status()).isEqualTo(GolferStatus.DELETED);
			assertThat(deleted.isActive()).isFalse();
			assertThat(deleted.email()).isEmpty();
			assertThat(deleted.passwordHash()).isEmpty();
			assertThat(deleted.displayName()).isEmpty();
			assertThat(deleted.handicapIndexes()).isEmpty();
			assertThat(deleted.currentHandicapIndex(TODAY)).isEmpty();
			assertThat(deleted.mustChangePassword()).isFalse();
			assertThat(deleted.deletedAt()).contains(NOW);
			assertThat(deleted.createdAt()).isEqualTo(CREATED);
		}

		@Test
		void requiresDeletionTime() {
			Golfer golfer = registered();

			assertThatNullPointerException().isThrownBy(() -> golfer.delete(null));
		}

		@Test
		void rejectsChangesToADeletedGolfer() {
			Golfer deleted = registered().delete(NOW);

			assertThatIllegalStateException().isThrownBy(() -> deleted.delete(NOW));
			assertThatIllegalStateException().isThrownBy(
					() -> deleted.recordHandicapIndex(UUID.randomUUID(), HandicapIndex.of("12.0"), TODAY, TODAY, NOW));
			assertThatIllegalStateException().isThrownBy(() -> deleted.removeHandicapIndex(UUID.randomUUID()));
			assertThatIllegalStateException().isThrownBy(() -> deleted.updateProfile(NAME, DistanceUnit.METERS));
			assertThatIllegalStateException().isThrownBy(() -> deleted.changePassword(HASH));
			assertThatIllegalStateException().isThrownBy(() -> deleted.resetPassword(HASH));
		}

	}

	@Nested
	class Identity {

		@Test
		void golfersWithTheSameIdAreEqual() {
			Golfer golfer = registered();
			Golfer changed = golfer.updateProfile(new DisplayName("Other"), DistanceUnit.YARDS);
			Golfer other = Golfer.register(GolferId.generate(), EMAIL, HASH, NAME, CREATED);

			assertThat(changed).isEqualTo(golfer).hasSameHashCodeAs(golfer);
			assertThat(golfer.hashCode()).isEqualTo(ID.hashCode());
			assertThat(other).isNotEqualTo(golfer);
			assertThat(golfer).isNotEqualTo("golfer");
		}

		@Test
		void toStringContainsNoPersonalData() {
			Golfer golfer = registered();

			assertThat(golfer.toString()).contains(ID.value().toString())
				.doesNotContain("max@example.com")
				.doesNotContain(HASH)
				.doesNotContain("Max");
		}

	}

}
