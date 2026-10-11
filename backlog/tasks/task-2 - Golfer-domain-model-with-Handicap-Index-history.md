---
id: TASK-2
title: Golfer domain model with Handicap Index history
status: Done
assignee:
  - '@claude'
created_date: '2026-10-11 11:39'
updated_date: '2026-10-11 12:08'
labels:
  - backend
  - domain
milestone: m-0
dependencies: []
references:
  - golfer
  - handicapindex
documentation:
  - backlog/docs/specs/doc-3 - SPEC-1-Accounts-Profile.md
modified_files:
  - src/main/java/com/prokxi/dimples/domain/.gitkeep
  - src/test/java/com/prokxi/dimples/domain/.gitkeep
  - src/main/java/com/prokxi/dimples/domain/DisplayName.java
  - src/main/java/com/prokxi/dimples/domain/DistanceUnit.java
  - src/main/java/com/prokxi/dimples/domain/EmailAddress.java
  - src/main/java/com/prokxi/dimples/domain/Golfer.java
  - src/main/java/com/prokxi/dimples/domain/GolferId.java
  - src/main/java/com/prokxi/dimples/domain/GolferRole.java
  - src/main/java/com/prokxi/dimples/domain/GolferStatus.java
  - src/main/java/com/prokxi/dimples/domain/HandicapIndex.java
  - src/main/java/com/prokxi/dimples/domain/HandicapIndexEntry.java
  - src/test/java/com/prokxi/dimples/domain/DisplayNameTest.java
  - src/test/java/com/prokxi/dimples/domain/EmailAddressTest.java
  - src/test/java/com/prokxi/dimples/domain/GolferIdTest.java
  - src/test/java/com/prokxi/dimples/domain/GolferTest.java
  - src/test/java/com/prokxi/dimples/domain/HandicapIndexEntryTest.java
  - src/test/java/com/prokxi/dimples/domain/HandicapIndexTest.java
  - groma/systems/dimples/containers/dimples-backend/components/golfer.md
  - groma/systems/dimples/containers/dimples-backend/components/handicapindex.md
priority: high
type: feature
ordinal: 2000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
The golfer is the first aggregate and the base for everything else (SPEC-1, section 3). Later specs read the Handicap Index on an event date to freeze handicaps at event start, so the history rules must be exact.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Golfer, EmailAddress, DisplayName, HandicapIndex and HandicapIndexEntry exist in domain with the rules of SPEC-1 section 3 (SPEC-1 AC-1 to AC-3)
- [x] #2 Current Handicap Index and Handicap Index on a date follow SPEC-1 section 3, including no entry yet (AC-4)
- [x] #3 Future effective dates are rejected; an existing effective date replaces the value (AC-5)
- [x] #4 Deleting an account clears all personal data and sets status DELETED (AC-6)
- [x] #5 IDs are application-generated UUIDs (decision-2)
- [x] #6 Mutation testing passes for the new domain code
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Value objects as records with validation in compact constructors: GolferId (UUID, generate()), EmailAddress (trim, lower-case, max 254, basic format), DisplayName (trim, 2-40 code points, no control characters), HandicapIndex (BigDecimal -10.0..54.0, one decimal, normalised to scale 1).
2. Enums DistanceUnit, GolferStatus, GolferRole; record HandicapIndexEntry (UUID id, value, effectiveOn, recordedAt).
3. Immutable Golfer aggregate: register(), full constructor for reconstitution with status invariants, handicapIndexOn(date)/currentHandicapIndex(today), recordHandicapIndex (rejects future dates, replaces an existing effectiveOn), removeHandicapIndex, updateProfile, changePassword/resetPassword (mustChangePassword), delete(now) clearing personal data. Time comes in as parameters (no clock access in domain).
4. Plain JUnit/AssertJ unit tests per type; run architecture tests, verify and -Pmutation.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Golfer is an immutable class (Optional accessors for personal data that a deleted golfer no longer has); value objects are records. The domain never reads the clock: today/now are parameters, because the ArchUnit rule forbids every Instant.now/LocalDate.now overload. Replacing an existing effectiveOn keeps the entry ID and updates recordedAt. No @Version in the domain yet; TASK-3 decides how optimistic locking is mapped. Validation: ./mvnw -q verify -Pmutation green, PIT 76/76 mutants killed, 100% line coverage of domain.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Added the Golfer aggregate with GolferId, EmailAddress, DisplayName, HandicapIndex, HandicapIndexEntry, DistanceUnit, GolferStatus and GolferRole following SPEC-1 section 3: normalisation and validation of values, Handicap Index on a date / current, future dates rejected, same date replaces, deletion clears personal data. Verified with plain JUnit/AssertJ tests and ./mvnw -q verify -Pmutation (76/76 mutants killed).
<!-- SECTION:FINAL_SUMMARY:END -->
