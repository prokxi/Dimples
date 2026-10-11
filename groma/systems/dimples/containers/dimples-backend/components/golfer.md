---
type: C4 Component
title: Golfer
status: stable
groma:
  id: golfer
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/Golfer.java
      symbol: Golfer
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/GolferId.java
      symbol: GolferId
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/EmailAddress.java
      symbol: EmailAddress
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/DisplayName.java
      symbol: DisplayName
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/DistanceUnit.java
      symbol: DistanceUnit
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/GolferStatus.java
      symbol: GolferStatus
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/GolferRole.java
      symbol: GolferRole
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/HandicapIndexEntry.java
      symbol: HandicapIndexEntry
  technology: Plain Java records and classes
description: 'Golfer aggregate: account data, settings and Handicap Index history'
---

The first domain aggregate (SPEC-1 section 3). An immutable `Golfer` holds the normalised email address, password hash, display name, distance unit preference, installation role (`GOLFER` or `OPERATOR`), the `mustChangePassword` flag after an operator reset, and the Handicap Index history. It answers which Handicap Index applies on a date (later used to freeze handicaps when an event starts), records new values (future dates rejected, an existing date replaced), and on account deletion removes all personal data while keeping the golfer as `DELETED` so past results stay consistent. IDs are application-generated UUIDs (decision-2); times are passed in by the service from its `Clock`. Invariant violations are signalled with JDK exceptions.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/domain/Golfer.java](../../../../../../src/main/java/com/prokxi/dimples/domain/Golfer.java) | [src/main/java/com/prokxi/dimples/domain/HandicapIndex.java](../../../../../../src/main/java/com/prokxi/dimples/domain/HandicapIndex.java) | Keeps Handicap Index history | Java method call |
