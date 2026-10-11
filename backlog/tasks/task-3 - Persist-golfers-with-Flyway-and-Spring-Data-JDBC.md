---
id: TASK-3
title: Persist golfers with Flyway and Spring Data JDBC
status: Done
assignee:
  - '@claude'
created_date: '2026-10-11 11:39'
updated_date: '2026-10-11 12:11'
labels:
  - backend
milestone: m-0
dependencies:
  - TASK-2
references:
  - golfer-persistence
  - golfer-repository
  - golfer
documentation:
  - backlog/docs/specs/doc-3 - SPEC-1-Accounts-Profile.md
modified_files:
  - >-
    groma/systems/dimples/containers/dimples-backend/components/golfer-persistence.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/golfer-repository.md
  - src/main/java/com/prokxi/dimples/domain/Golfer.java
  - src/main/java/com/prokxi/dimples/outbound/.gitkeep
  - src/main/java/com/prokxi/dimples/outbound/GolferJdbcRepository.java
  - src/main/java/com/prokxi/dimples/outbound/GolferRepositoryImpl.java
  - src/main/java/com/prokxi/dimples/outbound/GolferRow.java
  - src/main/java/com/prokxi/dimples/outbound/HandicapIndexEntryRow.java
  - src/main/java/com/prokxi/dimples/service/.gitkeep
  - src/main/java/com/prokxi/dimples/service/GolferRepository.java
  - src/main/resources/db/migration/.gitkeep
  - src/main/resources/db/migration/V1__create_golfer.sql
  - src/test/java/com/prokxi/dimples/TestcontainersConfiguration.java
  - src/test/java/com/prokxi/dimples/domain/GolferTest.java
  - src/test/java/com/prokxi/dimples/outbound/.gitkeep
  - src/test/java/com/prokxi/dimples/outbound/GolferRepositoryImplTest.java
priority: high
type: feature
ordinal: 3000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Golfers and their Handicap Index history must be stored in PostgreSQL (SPEC-1, section 6). This task also establishes the persistence conventions later aggregates copy: UUID IDs set by the application, @Version-based new detection, row types mapped in the Impl class.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Migration V1__create_golfer.sql creates golfer and handicap_index_entry as described in SPEC-1 section 6
- [x] #2 Email is unique among active golfers only (partial unique index)
- [x] #3 GolferRepository (service) is implemented by GolferRepositoryImpl (outbound); domain objects are mapped from and to row types
- [x] #4 A new golfer with a pre-set UUID is inserted, an existing one is updated
- [x] #5 DataJdbcTest with Testcontainers covers the round trip including the history
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Add an optimistic-locking version to Golfer (0 = not yet stored), carried through all changes.
2. V1__create_golfer.sql: golfer and handicap_index_entry per SPEC-1 section 6, partial unique index on email for ACTIVE golfers, check constraints for enums, range and personal data per status.
3. service.GolferRepository (save, findById, findActiveByEmail).
4. outbound: GolferRow + HandicapIndexEntryRow (@MappedCollection), GolferJdbcRepository, GolferRepositoryImpl mapping rows <-> domain. Primitive @Version int: 0 means new, so pre-set UUIDs are inserted.
5. @DataJdbcTest + Testcontainers: round trip with history, insert vs update, version conflict, partial unique email index, deleted golfer.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Golfer got an int version (0 = not stored yet) so updates use optimistic locking; save() returns the stored golfer with its new version. Rows are package-private records; the primitive @Version makes a row with a preset UUID and version 0 an insert. Check constraints back up enums, the Handicap Index range and 'personal data only while ACTIVE'. The unique (golfer_id, effective_on) constraint doubles as the FK index. TestcontainersConfiguration is now public so tests in sub-packages can import it. Validation: ./mvnw -q verify -Pmutation green (GolferRepositoryImplTest 9 tests on Postgres 17, PIT 77/77).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Added V1__create_golfer.sql, the service GolferRepository and its outbound implementation (GolferRow/HandicapIndexEntryRow, GolferJdbcRepository, GolferRepositoryImpl), plus an optimistic-locking version on Golfer. Verified with a Testcontainers @DataJdbcTest (insert with preset UUID, round trip with history, update, outdated version, partial unique email index, deleted golfer) and a green ./mvnw -q verify -Pmutation.
<!-- SECTION:FINAL_SUMMARY:END -->
