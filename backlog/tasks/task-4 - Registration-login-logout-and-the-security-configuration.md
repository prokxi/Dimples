---
id: TASK-4
title: 'Registration, login, logout and the security configuration'
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
  - security
milestone: m-0
dependencies:
  - TASK-1
  - TASK-3
documentation:
  - backlog/docs/specs/doc-3 - SPEC-1-Accounts-Profile.md
priority: high
type: feature
ordinal: 4000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Golfers need accounts before they can do anything in Dimples (PRD FR-1). This task defines the first SecurityFilterChain; the owner approved the configuration in SPEC-1 sections 4.2, 4.3 and 5. Remember-me keeps golfers logged in on their phone during a round and across deploys, because sessions are in memory.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 POST /api/v1/auth/register creates a golfer with a bcrypt hash, logs them in and returns 201 with Location (SPEC-1 AC-7)
- [ ] #2 Duplicate active email returns 409; passwords outside 10 to 64 characters or over 72 bytes return 400 (AC-8)
- [ ] #3 Wrong email and wrong password return the same 401; the 6th failed attempt within 15 minutes returns 429 (AC-9)
- [ ] #4 Remember-me with persistent tokens (migration V2) keeps a golfer logged in after session expiry and restart (AC-10)
- [ ] #5 CSRF uses a cookie readable by the SPA; GET /api/v1/auth/csrf issues it; changing requests without a token get 403 (AC-11)
- [ ] #6 Unauthenticated API requests return 401, not a redirect (AC-12)
- [ ] #7 Logout ends the session and the remember-me token
- [ ] #8 No passwords, hashes or emails are logged (AC-17)
- [ ] #9 Endpoints are documented in OpenAPI
<!-- AC:END -->
