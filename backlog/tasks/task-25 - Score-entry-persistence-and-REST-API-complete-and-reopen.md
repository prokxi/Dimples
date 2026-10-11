---
id: TASK-25
title: 'Score entry persistence and REST API, complete and reopen'
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
milestone: m-0
dependencies:
  - TASK-24
  - TASK-21
documentation:
  - backlog/docs/specs/doc-7 - SPEC-5-Score-Entry.md
priority: high
type: feature
ordinal: 25000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Players enter scores per flight and hole, live or after the round; organisers complete events (SPEC-5 sections 4 to 6).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Migration V6__create_scorecard.sql; scorecards are created empty when an event starts (SPEC-5 AC-12)
- [ ] #2 PUT per flight and hole sets the values idempotently; flight players and organisers may write, other members 403, non-members 404 (AC-5)
- [ ] #3 Entering only in STARTED events (AC-6)
- [ ] #4 Concurrent requests for different holes are both saved; for the same hole the later wins (AC-7)
- [ ] #5 Scoreboard with ETag and 304 (AC-8); scorecard and history endpoints (AC-11)
- [ ] #6 Complete only when every match has a result, else 409 listing open matches; reopen works (AC-9, AC-10)
- [ ] #7 Endpoints documented in OpenAPI (AC-13)
<!-- AC:END -->
