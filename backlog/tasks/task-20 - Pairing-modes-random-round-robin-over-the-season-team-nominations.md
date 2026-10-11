---
id: TASK-20
title: 'Pairing modes: random, round robin over the season, team nominations'
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
  - domain
milestone: m-0
dependencies:
  - TASK-19
documentation:
  - backlog/docs/specs/doc-5 - SPEC-3-Series-Events-Teams-Pairings.md
priority: high
type: feature
ordinal: 20000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Organisers let Dimples create the matches of an event and adjust them by hand (PRD FR-24, SPEC-3 section 4.4).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 RANDOM pairs all participants; an odd number gives the bye to the participant with the fewest byes (SPEC-3 AC-6)
- [ ] #2 ROUND_ROBIN: with full attendance and 4, 6, 8 and 10 players every pair meets exactly once in n-1 events, then pairs repeat (AC-7)
- [ ] #3 ROUND_ROBIN with changing attendance minimises repeat meetings (highest count first, then sum) (AC-8)
- [ ] #4 TEAM_NOMINATIONS pairs #i against #i, needs 2 teams and equal numbers, non-nominated get a bye (AC-9)
- [ ] #5 Manual pairings are validated (AC-10); flights are assigned in order with at most 2 matches
- [ ] #6 Randomness comes from an injected generator; tests are repeatable
- [ ] #7 Mutation testing passes for the new domain code
<!-- AC:END -->
