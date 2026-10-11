---
id: TASK-13
title: Course persistence and REST API
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
milestone: m-0
dependencies:
  - TASK-12
  - TASK-4
documentation:
  - backlog/docs/specs/doc-4 - SPEC-2-Courses.md
priority: medium
type: feature
ordinal: 13000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Golfers enter courses once and everyone reuses them (PRD FR-10, SPEC-2 sections 4 to 6).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Migration V3__create_course.sql and the course repository store courses, tees and holes (SPEC-2 AC-12)
- [ ] #2 Create, search (case-insensitive, paginated), read, update and delete endpoints as in SPEC-2 section 5 (AC-7)
- [ ] #3 Only the creator and the operator can change or delete a course; others get 403 (AC-8)
- [ ] #4 Updates keep tee IDs, add new tees and remove missing ones (AC-9); an outdated version returns 409 (AC-10)
- [ ] #5 401 for unauthenticated requests, 404 for missing courses (AC-11); endpoints documented in OpenAPI (AC-13)
<!-- AC:END -->
