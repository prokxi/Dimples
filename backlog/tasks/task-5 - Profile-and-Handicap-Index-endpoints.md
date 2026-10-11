---
id: TASK-5
title: Profile and Handicap Index endpoints
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
milestone: m-0
dependencies:
  - TASK-4
documentation:
  - backlog/docs/specs/doc-3 - SPEC-1-Accounts-Profile.md
priority: medium
type: feature
ordinal: 5000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Golfers maintain their display name, distance unit and official Handicap Index themselves (PRD FR-2); match play depends on the Handicap Index being entered (SPEC-1 sections 4.4 and 5).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 GET and PUT /api/v1/golfers/me read and change display name and distance unit
- [ ] #2 Handicap Index history can be listed (paginated, newest first), added and deleted under /api/v1/golfers/me/handicap-indexes
- [ ] #3 Deleting another golfer's entry returns 404 (SPEC-1 AC-13)
- [ ] #4 WebMvcTest covers validation, 401 and the other-golfer case; endpoints are documented in OpenAPI
<!-- AC:END -->
