---
id: TASK-12
title: 'Course domain model with tees, holes and ratings'
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
  - domain
milestone: m-0
dependencies: []
documentation:
  - backlog/docs/specs/doc-4 - SPEC-2-Courses.md
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
- [ ] #1 Course, Tee, Hole and the rating value objects enforce the rules of SPEC-2 section 3 (SPEC-2 AC-1 to AC-4)
- [ ] #2 ratingFor returns the values of the table in SPEC-2 section 3 for ALL, FRONT_NINE and BACK_NINE, with and without nine-hole ratings, rounding half up (AC-5)
- [ ] #3 FRONT_NINE and BACK_NINE on a 9-hole course are rejected (AC-6)
- [ ] #4 Mutation testing passes for the new domain code
<!-- AC:END -->
