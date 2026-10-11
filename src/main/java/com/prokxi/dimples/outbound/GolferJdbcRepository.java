package com.prokxi.dimples.outbound;

import java.util.Optional;
import java.util.UUID;

import com.prokxi.dimples.domain.GolferStatus;

import org.springframework.data.repository.ListCrudRepository;

interface GolferJdbcRepository extends ListCrudRepository<GolferRow, UUID> {

	Optional<GolferRow> findByEmailAndStatus(String email, GolferStatus status);

}
