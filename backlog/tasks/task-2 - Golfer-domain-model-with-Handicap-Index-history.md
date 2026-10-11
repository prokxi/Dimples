---
id: TASK-2
title: Golfer domain model with Handicap Index history
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
  - domain
milestone: m-0
dependencies: []
documentation:
  - backlog/docs/specs/doc-3 - SPEC-1-Accounts-Profile.md
priority: high
type: feature
ordinal: 2000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
The golfer is the first aggregate and the base for everything else (SPEC-1, section 3). Later specs read the Handicap Index on an event date to freeze handicaps at event start, so the history rules must be exact.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Golfer, EmailAddress, DisplayName, HandicapIndex and HandicapIndexEntry exist in domain with the rules of SPEC-1 section 3 (SPEC-1 AC-1 to AC-3)
- [ ] #2 Current Handicap Index and Handicap Index on a date follow SPEC-1 section 3, including no entry yet (AC-4)
- [ ] #3 Future effective dates are rejected; an existing effective date replaces the value (AC-5)
- [ ] #4 Deleting an account clears all personal data and sets status DELETED (AC-6)
- [ ] #5 IDs are application-generated UUIDs (decision-2)
- [ ] #6 Mutation testing passes for the new domain code
<!-- AC:END -->
