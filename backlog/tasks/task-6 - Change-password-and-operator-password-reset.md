---
id: TASK-6
title: Change password and operator password reset
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
ordinal: 6000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
There is no email in M1, so a forgotten password is reset by the operator (PRD FR-4, SPEC-1 sections 4.5 and 4.6). The temporary password must be changed at the next login.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 PUT /api/v1/golfers/me/password requires the current password, applies the password rules and deletes all remember-me tokens (SPEC-1 AC-14)
- [ ] #2 POST /api/v1/operator/password-resets returns a 16-character temporary password once, only for the OPERATOR role; everyone else gets 404 (AC-17a)
- [ ] #3 While mustChangePassword is set, only reading the own profile, changing the password and logout are allowed; other calls return 403 with type password-change-required (AC-15)
- [ ] #4 The temporary password is never stored in plain text or logged
<!-- AC:END -->
