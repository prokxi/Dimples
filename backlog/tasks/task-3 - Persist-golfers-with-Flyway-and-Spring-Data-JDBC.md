---
id: TASK-3
title: Persist golfers with Flyway and Spring Data JDBC
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
milestone: m-0
dependencies:
  - TASK-2
documentation:
  - backlog/docs/specs/doc-3 - SPEC-1-Accounts-Profile.md
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
- [ ] #1 Migration V1__create_golfer.sql creates golfer and handicap_index_entry as described in SPEC-1 section 6
- [ ] #2 Email is unique among active golfers only (partial unique index)
- [ ] #3 GolferRepository (service) is implemented by GolferRepositoryImpl (outbound); domain objects are mapped from and to row types
- [ ] #4 A new golfer with a pre-set UUID is inserted, an existing one is updated
- [ ] #5 DataJdbcTest with Testcontainers covers the round trip including the history
<!-- AC:END -->
