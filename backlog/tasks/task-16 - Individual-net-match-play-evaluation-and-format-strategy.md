---
id: TASK-16
title: Individual net match play evaluation and format strategy
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
  - domain
milestone: m-0
dependencies:
  - TASK-15
documentation:
  - backlog/docs/specs/doc-6 - SPEC-4-Handicaps-Individual-Net-Match-Play.md
priority: high
type: feature
ordinal: 16000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Matches are evaluated hole by hole and as a whole in the usual match play notation (SPEC-4 sections 4 and 5). It is the first format implementation, so later formats can be added without changing it.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Hole results follow SPEC-4 section 4.2, including concessions, picked up and missing scores (AC-10)
- [ ] #2 Status shows lead, all square, dormie, holes played and remaining (AC-11)
- [ ] #3 Results 3&2, 1 up, 2 up and halved, for 18 holes and both nines (AC-12)
- [ ] #4 Scores after the decision are accepted and do not change the result (AC-13)
- [ ] #5 A format interface exists in domain; INDIVIDUAL_MATCH_PLAY_NET is selected from the event format (AC-14)
- [ ] #6 Mutation testing passes for the new domain code
<!-- AC:END -->
