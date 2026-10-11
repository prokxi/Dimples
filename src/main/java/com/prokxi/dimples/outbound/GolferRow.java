package com.prokxi.dimples.outbound;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.prokxi.dimples.domain.DistanceUnit;
import com.prokxi.dimples.domain.GolferRole;
import com.prokxi.dimples.domain.GolferStatus;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Persistence shape of the golfer aggregate. The primitive {@code version} makes Spring
 * Data JDBC insert a row with {@code version == 0} although its ID is already set.
 */
@Table("golfer")
record GolferRow(@Id UUID id, String email, String passwordHash, String displayName, DistanceUnit distanceUnit,
		GolferStatus status, GolferRole role, boolean mustChangePassword, Instant createdAt, Instant deletedAt,
		@Version int version, @MappedCollection(idColumn = "golfer_id") Set<HandicapIndexEntryRow> handicapIndexes) {

}
