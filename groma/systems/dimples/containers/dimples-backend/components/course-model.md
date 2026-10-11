---
type: C4 Component
title: Course
status: stable
groma:
  id: course-model
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/Course.java
      symbol: Course
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/Tee.java
      symbol: Tee
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/Hole.java
      symbol: Hole
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/CourseId.java
      symbol: CourseId
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/TeeId.java
      symbol: TeeId
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/HoleCount.java
      symbol: HoleCount
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/HoleSelection.java
      symbol: HoleSelection
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/CourseRating.java
      symbol: CourseRating
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/SlopeRating.java
      symbol: SlopeRating
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/NineHoleRating.java
      symbol: NineHoleRating
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/TeeRating.java
      symbol: TeeRating
  technology: Plain Java records and classes, BigDecimal
description: 'Course aggregate: tees with WHS ratings, holes, and ratings for a hole selection'
---

The course domain model of SPEC-2 section 3. Courses are shared by all golfers, so they hold no personal data apart from the creator's `GolferId`. An immutable `Course` (aggregate root, optimistic-locking `version`) owns its tees; it checks the name (2–100 characters), optional city and ISO 3166-1 alpha-2 country, that every tee has the course's `HoleCount` (9 or 18), and that tee names (case-insensitive) and tee IDs are unique. An update replaces the whole course and keeps ID, creator and creation time; times come from the caller's `Clock`.

Each `Tee` (stable `TeeId`, so events can refer to it) carries a `CourseRating` (one decimal; 50.0–90.0 on 18 holes, 25.0–45.0 on 9), a `SlopeRating` (55–155), optional front and back `NineHoleRating`s (18-hole tees only, both or none) and its holes. `Hole`s are numbered 1…n without gaps, have par 3–6, stroke indexes exactly 1…n and an optional length in meters.

`Tee.ratingFor(HoleSelection)` is the interface handicap calculation and stroke allocation build on: for `ALL`, `FRONT_NINE` or `BACK_NINE` it returns a `TeeRating` with Course Rating, Slope Rating, par and holes. A nine without its own rating gets half the 18-hole Course Rating, rounded half up (71.3 → 35.7), and the tee's Slope Rating; nines on 9-hole tees are rejected. Broken invariants raise JDK exceptions with messages free of personal data.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/domain/Course.java](../../../../../../src/main/java/com/prokxi/dimples/domain/Course.java) | [src/main/java/com/prokxi/dimples/domain/GolferId.java](../../../../../../src/main/java/com/prokxi/dimples/domain/GolferId.java) | Records the creator | Java field reference |
