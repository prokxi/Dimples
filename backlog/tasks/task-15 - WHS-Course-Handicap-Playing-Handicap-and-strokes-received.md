---
id: TASK-15
title: 'WHS Course Handicap, Playing Handicap and strokes received'
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
  - domain
milestone: m-0
dependencies:
  - TASK-12
  - TASK-2
documentation:
  - backlog/docs/specs/doc-6 - SPEC-4-Handicaps-Individual-Net-Match-Play.md
priority: high
type: feature
ordinal: 15000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Match play needs handicaps calculated exactly by the World Handicap System (PRD FR-30, SPEC-4 section 3). The rounding of .5, especially for plus handicaps, must be verified against the official WHS rules and worked examples.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Course Handicap for 18 and 9 holes with the examples of SPEC-4 AC-1 and AC-2
- [ ] #2 Rounding matches the official WHS examples, including .5 and plus handicaps (AC-3, AC-5)
- [ ] #3 Playing Handicap applies the allowance (AC-4)
- [ ] #4 Strokes received: lower Playing Handicap plays off 0, difference allocated by stroke index, more strokes than holes, nines of 18-hole tees, plus handicaps (AC-6 to AC-9)
- [ ] #5 Mutation testing passes for the new domain code
<!-- AC:END -->
