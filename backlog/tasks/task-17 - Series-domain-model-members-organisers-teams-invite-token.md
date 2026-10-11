---
id: TASK-17
title: 'Series domain model: members, organisers, teams, invite token'
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
  - domain
milestone: m-0
dependencies:
  - TASK-2
documentation:
  - backlog/docs/specs/doc-5 - SPEC-3-Series-Events-Teams-Pairings.md
priority: high
type: feature
ordinal: 17000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
A series is how friends organise their competition (PRD FR-20 to FR-22, FR-25, FR-26; SPEC-3 sections 3 and 4.1).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Series validates name, period, planned event count, points (win >= halved >= loss) and at most 16 active members (SPEC-3 AC-1)
- [ ] #2 At least one organiser at all times (AC-2)
- [ ] #3 Leaving only before the first event started; removal keeps past results (AC-3)
- [ ] #4 A member is in at most one team; teams are fixed after the first event started; late joiners can be added (AC-4)
- [ ] #5 Invite tokens have 128 random bits and can be renewed
- [ ] #6 Mutation testing passes for the new domain code
<!-- AC:END -->
