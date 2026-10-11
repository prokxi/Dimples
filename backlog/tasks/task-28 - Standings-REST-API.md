---
id: TASK-28
title: Standings REST API
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
milestone: m-0
dependencies:
  - TASK-27
  - TASK-25
documentation:
  - backlog/docs/specs/doc-8 - SPEC-6-Results-Standings.md
priority: medium
type: feature
ordinal: 28000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Members read the standings and a player's matches; the data comes from events and scorecards (SPEC-6 section 4).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 GET standings with view official or live, ETag and 304 (SPEC-6 AC-9)
- [ ] #2 GET a player's matches in the series, byes included with 0 points
- [ ] #3 Non-members get 404 (AC-8); removed and deleted golfers stay in the standings (AC-10)
- [ ] #4 Changed points or corrected scores show on the next request (AC-7)
- [ ] #5 Endpoints documented in OpenAPI (AC-12)
<!-- AC:END -->
