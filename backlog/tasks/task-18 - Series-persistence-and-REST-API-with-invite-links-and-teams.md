---
id: TASK-18
title: Series persistence and REST API with invite links and teams
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
milestone: m-0
dependencies:
  - TASK-17
  - TASK-4
  - TASK-7
documentation:
  - backlog/docs/specs/doc-5 - SPEC-3-Series-Events-Teams-Pairings.md
priority: high
type: feature
ordinal: 18000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Organisers create series, share invite links and manage members and teams (SPEC-3 sections 4.1, 4.2 and 5).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Migration V4__create_series.sql and the series repository (part of SPEC-3 AC-18)
- [ ] #2 Series, member, invite and team endpoints as in SPEC-3 section 5
- [ ] #3 Non-members get 404; members who are not organisers get 403 for changes (AC-14)
- [ ] #4 Joining via a valid link adds the golfer directly; renewed tokens make old links 404; finished or full series return 409 (AC-15)
- [ ] #5 Account deletion of the last organiser of a running series returns 409 (AC-16)
- [ ] #6 Endpoints documented in OpenAPI (AC-19); tests include the other-golfer case
<!-- AC:END -->
