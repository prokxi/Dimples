---
id: TASK-26
title: Flight scorecard and live event screens
status: To Do
assignee: []
created_date: '2026-10-11 11:40'
labels:
  - frontend
milestone: m-0
dependencies:
  - TASK-25
  - TASK-23
documentation:
  - backlog/docs/specs/doc-7 - SPEC-5-Score-Entry.md
priority: high
type: feature
ordinal: 26000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Scores are entered on a phone on the course, often with bad reception (SPEC-5 sections 4.4, 4.5 and 7).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 One hole per screen with par, stroke index and strokes received; +/- buttons starting at par; picked up and concede hole
- [ ] #2 Hole winner and match status after every hole
- [ ] #3 Entries are stored on the phone first and retried in order when the connection is back; unsaved holes are shown and leaving warns (SPEC-5 AC-14)
- [ ] #4 Event page refreshes every 60 seconds while visible and pauses while typing (AC-15)
- [ ] #5 Organisers complete and reopen the event; the history of a match is visible
<!-- AC:END -->
