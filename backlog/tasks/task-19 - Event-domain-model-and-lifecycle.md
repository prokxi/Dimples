---
id: TASK-19
title: Event domain model and lifecycle
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
  - domain
milestone: m-0
dependencies:
  - TASK-17
  - TASK-12
  - TASK-15
documentation:
  - backlog/docs/specs/doc-5 - SPEC-3-Series-Events-Teams-Pairings.md
priority: high
type: feature
ordinal: 19000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Events are planned as drafts and frozen at start, so handicaps and course data cannot change during or after play (SPEC-3 sections 3, 4.3 and 4.5).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Event date within the series period; no more events than planned (SPEC-3 AC-5)
- [ ] #2 Changing participants, nominations or the pairing mode of a draft removes its matches (AC-11)
- [ ] #3 Only the transitions of SPEC-3 section 4.5 are allowed; start fails with reasons when a player has no Handicap Index (AC-12)
- [ ] #4 Start takes the snapshot of tee data and handicaps (via the handicap calculation) and of each participant's team (SPEC-6 section 3.4); it does not change afterwards (AC-13)
- [ ] #5 Mutation testing passes for the new domain code
<!-- AC:END -->
