---
id: TASK-21
title: 'Event persistence and REST API: setup, pairings, start, cancel'
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
milestone: m-0
dependencies:
  - TASK-19
  - TASK-20
  - TASK-18
  - TASK-13
documentation:
  - backlog/docs/specs/doc-5 - SPEC-3-Series-Events-Teams-Pairings.md
priority: high
type: feature
ordinal: 21000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Organisers plan events, generate pairings and start events through the API (SPEC-3 section 5).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Migration V5__create_event.sql stores events with participants, nominations, matches, byes and snapshot, including each participant's team (SPEC-3 AC-18, SPEC-6 AC-11)
- [ ] #2 Event endpoints as in SPEC-3 section 5, including generate, manual pairings, start and cancel
- [ ] #3 Start returns 409 with the list of reasons when it is not possible
- [ ] #4 EventResponse includes Course Handicap, Playing Handicap and strokes received per match player after start (SPEC-4 section 6)
- [ ] #5 A course or tee used by an event cannot be deleted (SPEC-3 AC-17)
- [ ] #6 Non-members 404, non-organisers 403 for changes; endpoints documented in OpenAPI
<!-- AC:END -->
