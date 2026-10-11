---
id: TASK-24
title: Scorecard domain model with change history
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - backend
  - domain
milestone: m-0
dependencies:
  - TASK-16
documentation:
  - backlog/docs/specs/doc-7 - SPEC-5-Score-Entry.md
priority: high
type: feature
ordinal: 24000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Scores are a separate aggregate per match so that several phones can enter scores at the same time (SPEC-5 section 3).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Hole entries accept gross strokes 1 to 20 or picked up, only for holes of the selection and the two match players (SPEC-5 AC-1)
- [ ] #2 At most one player of a match concedes a hole (AC-2)
- [ ] #3 Setting the same values twice changes nothing; every real change creates exactly one history entry (AC-3)
- [ ] #4 Removing an entry removes the hole result; status and result are recalculated with the match play evaluation (AC-4)
- [ ] #5 Mutation testing passes for the new domain code
<!-- AC:END -->
