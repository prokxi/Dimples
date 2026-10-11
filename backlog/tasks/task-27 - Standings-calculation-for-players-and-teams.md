---
id: TASK-27
title: Standings calculation for players and teams
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
  - domain
milestone: m-0
dependencies:
  - TASK-16
  - TASK-19
documentation:
  - backlog/docs/specs/doc-8 - SPEC-6-Results-Standings.md
priority: high
type: feature
ordinal: 27000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Series standings are calculated automatically from match results with clear tie-breaks (PRD FR-51, SPEC-6 section 3).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Official view counts COMPLETED events; live view adds decided matches of STARTED events; DRAFT and CANCELLED never count (SPEC-6 AC-1)
- [ ] #2 Points from the series MatchPoints; byes 0 points and counted separately (AC-2)
- [ ] #3 Hole difference as in SPEC-6 section 3.2 (AC-3)
- [ ] #4 Ranking by points, wins, head-to-head among tied players, hole difference, shared rank (AC-4)
- [ ] #5 Team values and ranking as in SPEC-6 section 3.4 (AC-5, AC-6)
- [ ] #6 Mutation testing passes for the new domain code
<!-- AC:END -->
