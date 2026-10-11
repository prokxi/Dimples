---
id: TASK-7
title: Delete own account
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
  - security
milestone: m-0
dependencies:
  - TASK-4
documentation:
  - backlog/docs/specs/doc-3 - SPEC-1-Accounts-Profile.md
priority: medium
type: feature
ordinal: 7000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Golfers can delete their account and personal data (PRD FR-5, legal basics). Past results must stay consistent, so the golfer row stays as a tombstone (SPEC-1 section 4.7). Later specs add checks that block deletion.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 DELETE /api/v1/golfers/me requires the password, removes personal data and logs out (SPEC-1 AC-16)
- [ ] #2 The email can be used for a new registration afterwards
- [ ] #3 Login with the deleted account behaves like a wrong password
- [ ] #4 The service has a single place for deletion checks that later specs extend
<!-- AC:END -->
