package com.prokxi.dimples.outbound;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("handicap_index_entry")
record HandicapIndexEntryRow(@Id UUID id, BigDecimal value, LocalDate effectiveOn, Instant recordedAt) {

}
