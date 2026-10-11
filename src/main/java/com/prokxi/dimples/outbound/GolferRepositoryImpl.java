package com.prokxi.dimples.outbound;

import java.util.Optional;
import java.util.stream.Collectors;

import com.prokxi.dimples.domain.DisplayName;
import com.prokxi.dimples.domain.EmailAddress;
import com.prokxi.dimples.domain.Golfer;
import com.prokxi.dimples.domain.GolferId;
import com.prokxi.dimples.domain.GolferStatus;
import com.prokxi.dimples.domain.HandicapIndex;
import com.prokxi.dimples.domain.HandicapIndexEntry;
import com.prokxi.dimples.service.GolferRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class GolferRepositoryImpl implements GolferRepository {

	private final GolferJdbcRepository jdbcRepository;

	@Override
	public Golfer save(Golfer golfer) {
		return toDomain(this.jdbcRepository.save(toRow(golfer)));
	}

	@Override
	public Optional<Golfer> findById(GolferId id) {
		return this.jdbcRepository.findById(id.value()).map(GolferRepositoryImpl::toDomain);
	}

	@Override
	public Optional<Golfer> findActiveByEmail(EmailAddress email) {
		return this.jdbcRepository.findByEmailAndStatus(email.value(), GolferStatus.ACTIVE)
			.map(GolferRepositoryImpl::toDomain);
	}

	private static GolferRow toRow(Golfer golfer) {
		return new GolferRow(golfer.id().value(), golfer.email().map(EmailAddress::value).orElse(null),
				golfer.passwordHash().orElse(null), golfer.displayName().map(DisplayName::value).orElse(null),
				golfer.distanceUnit(), golfer.status(), golfer.role(), golfer.mustChangePassword(), golfer.createdAt(),
				golfer.deletedAt().orElse(null), golfer.version(),
				golfer.handicapIndexes()
					.stream()
					.map((entry) -> new HandicapIndexEntryRow(entry.id(), entry.value().value(), entry.effectiveOn(),
							entry.recordedAt()))
					.collect(Collectors.toSet()));
	}

	private static Golfer toDomain(GolferRow row) {
		return new Golfer(new GolferId(row.id()), (row.email() != null) ? new EmailAddress(row.email()) : null,
				row.passwordHash(), (row.displayName() != null) ? new DisplayName(row.displayName()) : null,
				row.distanceUnit(),
				row.handicapIndexes()
					.stream()
					.map((entry) -> new HandicapIndexEntry(entry.id(), new HandicapIndex(entry.value()),
							entry.effectiveOn(), entry.recordedAt()))
					.toList(),
				row.status(), row.role(), row.mustChangePassword(), row.createdAt(), row.deletedAt(), row.version());
	}

}
