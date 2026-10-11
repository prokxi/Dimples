---
type: C4 Component
title: Golfer persistence
status: stable
groma:
  id: golfer-persistence
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/outbound/GolferRepositoryImpl.java
      symbol: GolferRepositoryImpl
    - scanner: java
      file: src/main/java/com/prokxi/dimples/outbound/GolferJdbcRepository.java
      symbol: GolferJdbcRepository
    - scanner: java
      file: src/main/java/com/prokxi/dimples/outbound/GolferRow.java
      symbol: GolferRow
    - scanner: java
      file: src/main/java/com/prokxi/dimples/outbound/HandicapIndexEntryRow.java
      symbol: HandicapIndexEntryRow
  technology: Spring Data JDBC, PostgreSQL
description: Stores golfers and their Handicap Index history in PostgreSQL
---

Implements the service interface `GolferRepository`. `GolferRepositoryImpl` maps the domain `Golfer` to the `GolferRow` / `HandicapIndexEntryRow` persistence records and back, and delegates to the internal Spring Data JDBC `GolferJdbcRepository`. The history is a mapped collection of the golfer row, so it is replaced as a whole on every save. IDs are set by the application; the primitive `@Version` makes rows with version 0 inserts and gives optimistic locking on updates. The schema (tables `golfer` and `handicap_index_entry`, partial unique index on the email of active golfers) comes from the Flyway migration `V1__create_golfer.sql`. This is the pattern later aggregates copy.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/outbound/GolferRepositoryImpl.java](../../../../../../src/main/java/com/prokxi/dimples/outbound/GolferRepositoryImpl.java) | [src/main/java/com/prokxi/dimples/service/GolferRepository.java](../../../../../../src/main/java/com/prokxi/dimples/service/GolferRepository.java) | Implements golfer storage | Java interface |
| [src/main/java/com/prokxi/dimples/outbound/GolferRepositoryImpl.java](../../../../../../src/main/java/com/prokxi/dimples/outbound/GolferRepositoryImpl.java) | [src/main/java/com/prokxi/dimples/domain/Golfer.java](../../../../../../src/main/java/com/prokxi/dimples/domain/Golfer.java) | Maps golfers to rows | Java method call |
