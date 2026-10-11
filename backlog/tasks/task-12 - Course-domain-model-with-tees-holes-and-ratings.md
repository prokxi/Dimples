---
id: TASK-12
title: 'Course domain model with tees, holes and ratings'
status: Done
assignee:
  - '@claude'
created_date: '2026-10-11 11:39'
updated_date: '2026-10-11 12:28'
labels:
  - backend
  - domain
milestone: m-0
dependencies: []
references:
  - golferid
  - course-model
documentation:
  - backlog/docs/specs/doc-4 - SPEC-2-Courses.md
modified_files:
  - src/main/java/com/prokxi/dimples/domain/.gitkeep
  - src/test/java/com/prokxi/dimples/domain/.gitkeep
  - src/main/java/com/prokxi/dimples/domain/GolferId.java
  - src/main/java/com/prokxi/dimples/domain/CourseId.java
  - src/main/java/com/prokxi/dimples/domain/TeeId.java
  - src/main/java/com/prokxi/dimples/domain/HoleCount.java
  - src/main/java/com/prokxi/dimples/domain/HoleSelection.java
  - src/main/java/com/prokxi/dimples/domain/SlopeRating.java
  - src/main/java/com/prokxi/dimples/domain/CourseRating.java
  - src/main/java/com/prokxi/dimples/domain/NineHoleRating.java
  - src/main/java/com/prokxi/dimples/domain/TeeRating.java
  - src/main/java/com/prokxi/dimples/domain/Hole.java
  - src/main/java/com/prokxi/dimples/domain/Tee.java
  - src/main/java/com/prokxi/dimples/domain/Course.java
  - src/test/java/com/prokxi/dimples/domain/CourseFixtures.java
  - src/test/java/com/prokxi/dimples/domain/CourseRatingTest.java
  - src/test/java/com/prokxi/dimples/domain/RatingValueObjectsTest.java
  - src/test/java/com/prokxi/dimples/domain/HoleTest.java
  - src/test/java/com/prokxi/dimples/domain/TeeTest.java
  - src/test/java/com/prokxi/dimples/domain/CourseTest.java
  - groma/systems/dimples/containers/dimples-backend/components/course.md
  - groma/systems/dimples/containers/dimples-backend/components/courseid.md
  - groma/systems/dimples/containers/dimples-backend/components/courserating.md
  - groma/systems/dimples/containers/dimples-backend/components/golferid.md
  - groma/systems/dimples/containers/dimples-backend/components/hole.md
  - groma/systems/dimples/containers/dimples-backend/components/holecount.md
  - groma/systems/dimples/containers/dimples-backend/components/holeselection.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/nineholerating.md
  - groma/systems/dimples/containers/dimples-backend/components/sloperating.md
  - groma/systems/dimples/containers/dimples-backend/components/tee.md
  - groma/systems/dimples/containers/dimples-backend/components/teeid.md
  - groma/systems/dimples/containers/dimples-backend/components/teerating.md
  - groma/systems/dimples/containers/dimples-backend/components/course-model.md
priority: high
type: feature
ordinal: 12000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Events need course data to calculate handicaps and allocate strokes, for 18 holes and for a nine (SPEC-2, section 3). ratingFor is the interface the match play calculation builds on.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Course, Tee, Hole and the rating value objects enforce the rules of SPEC-2 section 3 (SPEC-2 AC-1 to AC-4)
- [x] #2 ratingFor returns the values of the table in SPEC-2 section 3 for ALL, FRONT_NINE and BACK_NINE, with and without nine-hole ratings, rounding half up (AC-5)
- [x] #3 FRONT_NINE and BACK_NINE on a 9-hole course are rejected (AC-6)
- [x] #4 Mutation testing passes for the new domain code
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Branch feature/course-domain off origin/develop.
2. Value objects in domain: CourseId, TeeId (UUID, generate()), GolferId (byte-identical copy of the one in the open golfer PR so the merge is clean), HoleCount (NINE/EIGHTEEN), HoleSelection (ALL/FRONT_NINE/BACK_NINE), CourseRating (one decimal, 25.0-90.0 overall, half() rounding half up), SlopeRating (55-155), NineHoleRating (CR 25.0-45.0), TeeRating (result of ratingFor: CR, SR, holes, par = sum).
3. Hole (number 1-18, par 3-6, stroke index, optional lengthMeters 1-800).
4. Tee entity: name 1-30, hole count derived from holes (9/18), holes numbered 1..n, stroke indexes exactly 1..n, CR range per hole count, nine-hole ratings only on 18-hole tees and both-or-none; ratingFor(HoleSelection) per SPEC-2 table, FRONT/BACK rejected on 9 holes.
5. Course aggregate: create factory, restoring constructor, update(..., now), findTee; name 2-100 trimmed, city <=100 optional, country ISO 3166-1 alpha-2 optional, >=1 tee, all tees match holeCount, tee names unique case-insensitive, tee IDs unique, version.
6. Unit tests (JUnit 5 + AssertJ) incl. AC-5 table and 71.3 -> 35.7; ArchitectureTest, verify -Pmutation.
7. groma scan + curate, record files/refs; finalize task, commit, push, PR into develop.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implemented in domain: Course (aggregate root), Tee, Hole, CourseId, TeeId, HoleCount, HoleSelection, CourseRating, SlopeRating, NineHoleRating, TeeRating, plus GolferId (createdBy).

Decisions and readings of SPEC-2:
- GolferId is not on develop yet (golfer PRs #5/#6 open). Added a byte-identical copy of the golfer branch's GolferId.java so the later merge is a clean identical add. Groma: on develop this gives a separate 'golferid' component (Golfer ID), while the golfer branch puts GolferId into 'golfer'. After both are merged, run 'groma edit golfer --combine golferid' (golferid has only a title/description, no body, so the combine is allowed).
- A tee's hole count is derived from its holes (9 or 18); Course checks it matches its HoleCount. Hole numbers and stroke indexes are bounded 1-18 per hole, the tee checks 1..n without gaps and stroke indexes exactly 1..n.
- CourseRating accepts 25.0-90.0 with at most one decimal (normalised to scale 1); the tee checks 50.0-90.0 (18 holes) / 25.0-45.0 (9 holes), NineHoleRating checks 25.0-45.0. Nine-hole Slope Rating uses the normal 55-155 range (the spec gives no separate range).
- ratingFor returns TeeRating(courseRating, slopeRating, holes) with par() = sum of the holes' pars. Derived nine CR = CR/2 rounded HALF_UP to one decimal.
- Names: course name stripped, 2-100 code points; tee name stripped, 1-30; no control characters. City optional, blank counts as absent, max 100. Country optional, accepted in any case and normalised to upper case, must be in Locale.getISOCountries(PART1_ALPHA2) (conservative: rejects unknown 2-letter codes such as XX).
- Tee names unique case-insensitively (Locale.ROOT lower case); tee IDs also unique within a course. updatedAt may not be before createdAt.
- update(...) replaces the whole course and keeps id, createdBy, createdAt, version. Matching tee IDs from a request (keep/add/remove) is left to the service in TASK-13; findTee(TeeId) is provided.
- Ownership/permission checks (creator or OPERATOR) are service concerns (TASK-13).

Validation: ./mvnw -q test -Dtest='*ArchitectureTest' green; ./mvnw -q verify -Pmutation exit 0: 191 test cases, JaCoCo domain 219/219 lines and 135/135 branches, PIT 144/144 mutants killed (100%). Groma: scan, combined the course value objects into component course-model (title Course), kept golferid separate, added relation Course.java -> GolferId.java; two rescans created nothing new.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Added the SPEC-2 course domain model in com.prokxi.dimples.domain: the immutable Course aggregate (name/city/country rules, HoleCount, >=1 tee with unique names and IDs, create/update/restore with version) with Tee, Hole and the value objects CourseId, TeeId, HoleCount, HoleSelection, CourseRating, SlopeRating, NineHoleRating and TeeRating. Tee.ratingFor(HoleSelection) implements the SPEC-2 table for ALL, FRONT_NINE and BACK_NINE with and without nine-hole ratings (71.3 -> 35.7, half up) and rejects nines on 9-hole tees. GolferId was copied unchanged from the open golfer branch for createdBy. Verified with plain JUnit 5/AssertJ unit tests (CourseTest, TeeTest, HoleTest, CourseRatingTest, RatingValueObjectsTest) and ./mvnw -q verify -Pmutation: 100% line/branch coverage in domain and 144/144 mutants killed; architecture tests green. Groma map updated (course-model component, golferid component, relation). Persistence and REST follow in TASK-13.
<!-- SECTION:FINAL_SUMMARY:END -->
