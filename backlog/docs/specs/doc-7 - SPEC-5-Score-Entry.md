---
id: doc-7
title: SPEC-5 Score Entry
type: specification
created_date: '2026-10-11 11:33'
updated_date: '2026-10-11 11:36'
---
# SPEC-5: Score Entry

Status: Accepted · Owner: prokxi · Last updated: 2026-10-11
Covers: PRD FR-40 – FR-42, FR-50 (event part) · Milestone: M1 · Depends on: SPEC-3 (events, flights), SPEC-4 (match evaluation)

## 1. Goal

Players enter their scores hole by hole, live on the course or after the round, usually one phone per flight.
Everyone in the series follows the matches almost live. Organisers complete the event when all matches have a
result, and can reopen it to correct scores.

## 2. User stories

- **US-1** As a player, I enter the gross strokes of everyone in my flight hole by hole on my phone.
- **US-2** As a player, I record that a player picked up or conceded a hole.
- **US-3** As a player with bad reception, I keep entering scores; they are saved as soon as the connection is back.
- **US-4** As a player, I enter or complete the scores after the round.
- **US-5** As a player or organiser, I correct a wrong score and see who changed what.
- **US-6** As a member, I follow all matches of the event (status, results), updated every minute.
- **US-7** As an organiser, I complete the event when all matches have a result, and reopen it if something needs correcting.

## 3. Domain model

### Scorecard (aggregate root, one per match)

| Field | Type | Rules |
|---|---|---|
| matchId | `MatchId` | ID of the match in the event (SPEC-3). Also the scorecard's ID. |
| eventId | `EventId` | |
| entries | list of `HoleEntry` | At most one per player and hole. |
| version | | Optimistic locking (see 4.3). |

**HoleEntry**: `holeNumber`, `player` (golfer ID), and either `grossStrokes` (1–20) or `pickedUp`; plus
`conceded` (boolean).
- Hole numbers must belong to the event's hole selection; players must be the two players of the match.
- At most one player of a match can concede a given hole.
- Removing an entry is allowed (the hole has no result again).

The scorecard is created when the event starts (one per match, empty). The match status and result are calculated
from the entries with SPEC-4 and are not stored.

### ScoreChange (history, append-only)

`id`, `matchId`, `holeNumber`, `player`, `changedBy` (golfer ID), `changedAt`, `before`, `after`
(e.g. "5", "picked up", "conceded", "–").

## 4. Behaviour

### 4.1 Who may enter scores (`CASUAL` mode)

| Who | Read | Enter / correct |
|---|---|---|
| Players of a match's **flight** | yes | yes, for all matches in their flight |
| **Organisers** of the series | yes | yes, for all matches |
| Other members | yes | no → 403 |
| Non-members | no → 404 | no → 404 |

Scores can be entered only while the event is `STARTED` (otherwise 409). There is no time limit, so scores can be
entered during or after the round.

### 4.2 Entering

- The SPA sends **one hole of one flight at a time**: for every player of the flight their entry, and per match which
  player conceded (if any). This is the natural unit when one phone enters for the whole flight.
- The request **sets** the values (idempotent): sending the same hole twice gives the same result. This makes retries safe.
- Every change that actually changes a value is written to the history with the golfer who made it.
- Holes after the match is decided are accepted (SPEC-4: kept for statistics).

### 4.3 Concurrent changes

- **The last entry counts.** If two people change the same hole, the later request wins; the history shows both.
- Different holes or matches never block each other. If saving a scorecard fails because of a concurrent change
  (optimistic locking), the service reloads it and applies the request again (up to 3 attempts) instead of returning 409.

### 4.4 Bad reception (SPA)

- Every hole entry is first stored on the phone (browser storage) and then sent.
- If sending fails, the SPA retries automatically in the background (with increasing intervals) and when the connection
  comes back. Entries are sent in the order they were made.
- The scorecard shows clearly how many holes are not saved yet and warns before the page is left.
- This is not full offline support: the app must have been loaded once with a connection (full offline, PWA: M5).

### 4.5 Live view

- The event page and scorecards reload every **60 seconds** while they are visible, and immediately after the own entries
  are saved. Reloading pauses while the user is typing.
- The scoreboard response has an `ETag`; unchanged data returns `304 Not Modified` (saves mobile data).

### 4.6 Completing and reopening

- An organiser **completes** the event (`STARTED` → `COMPLETED`) when every match has a result (SPEC-4). Missing scores on holes
  after the decision don't block completion. Otherwise → 409 with the matches that have no result yet.
- An organiser **reopens** a completed event (`COMPLETED` → `STARTED`) to correct scores, then completes it again.
- Completion is done by hand, not automatically, because players keep entering the remaining holes after a match is decided.

## 5. REST API

All endpoints are under `/api/v1/series/{seriesId}/events/{eventId}` and need an authenticated golfer.

| Method & path | Who | Request | Success | Errors |
|---|---|---|---|---|
| `GET /scoreboard` | member | `If-None-Match` | 200 `ScoreboardResponse` + `ETag`, or 304 | 404 |
| `GET /matches/{matchId}/scorecard` | member | – | 200 `ScorecardResponse` | 404 |
| `PUT /flights/{flight}/holes/{holeNumber}` | flight player, organiser | `FlightHoleRequest` | 200 `FlightHoleResponse` | 400, 403, 404, 409 |
| `GET /matches/{matchId}/history?page&size` | member | – | 200 page of `ScoreChangeResponse`, newest first | 404 |
| `POST /complete` | organiser | – | 200 | 403, 404, 409 with open matches |
| `POST /reopen` | organiser | – | 200 | 403, 404, 409 |

- `FlightHoleRequest {entries: [{player, grossStrokes | pickedUp | null (remove)}], concessions: [{matchId, concededBy}]}`
- `FlightHoleResponse`: the updated hole results and match statuses of the flight's matches.
- `ScorecardResponse {match, players: [{golferId, displayName, playingHandicap}], holes: [{number, par, strokeIndex, strokesReceived per player, entries, netScores, holeResult, statusAfterHole}], status, result}`
- `ScoreboardResponse {eventStatus, matches: [{matchId, flight, players, holesPlayed, status, result}], byes}`
- All endpoints are documented with OpenAPI annotations.

## 6. Persistence (Flyway)

`V6__create_scorecard.sql`:
- `scorecard`: `match_id uuid` (PK, FK `event_match`), `event_id` (FK, index), `version`.
- `hole_entry`: `match_id` (FK), `hole_number`, `golfer_id` (FK), `gross_strokes smallint` (nullable), `picked_up boolean`,
  `conceded boolean`; PK `(match_id, hole_number, golfer_id)`; check: either `gross_strokes` or `picked_up`.
- `score_change`: `id uuid`, `match_id` (FK, index), `hole_number`, `golfer_id`, `changed_by` (FK `golfer`), `changed_at timestamptz`,
  `before text`, `after text`.

## 7. Frontend (SPA)

- **Flight scorecard** (mobile-first, used on the course):
  - One hole per screen with par, stroke index and dots for strokes received per player.
  - Large +/− buttons per player, defaulting to par; "picked up" and "concede hole" per match.
  - After every hole: hole winner and match status ("A 2 up", "dormie", "3&2").
  - "Not saved yet" indicator and retry (section 4.4).
- **Event page**: all matches with status/result, updated every 60 seconds; tap a match for its full scorecard and history.
- **Organiser**: "complete event" with the list of open matches; "reopen".

## 8. Out of scope

- Real-time push for live updates (WebSocket/SSE): later version.
- Full offline support / installable PWA: M5 (FR-44).
- More data per hole (putts, fairways, greens in regulation): statistics in v2.
- `STRICT` mode with marker confirmation: M2 (FR-43).
- Scores for other formats: M2.

## 9. Acceptance criteria

Domain
- [ ] AC-1 A hole entry has either gross strokes 1–20 or picked up; only holes of the hole selection and the two match players are accepted.
- [ ] AC-2 At most one player of a match can concede a hole.
- [ ] AC-3 Setting the same values twice changes nothing and creates no history entry; every real change creates exactly one history entry.
- [ ] AC-4 Removing an entry removes the hole result; status and result are recalculated (SPEC-4).

Service & API
- [ ] AC-5 Flight players and organisers can enter scores; other members get 403, non-members 404.
- [ ] AC-6 Entering is only possible in `STARTED` events (409 otherwise).
- [ ] AC-7 Two concurrent requests for different holes of the same match are both saved (retry on optimistic locking); for the same hole the later one wins.
- [ ] AC-8 The scoreboard returns `ETag` and 304 when nothing changed.
- [ ] AC-9 Completing works only when every match has a result; otherwise 409 lists the open matches. Missing holes after the decision don't block it.
- [ ] AC-10 Reopening allows corrections again; completing again works.
- [ ] AC-11 The history shows who changed which value and when.
- [ ] AC-12 Persistence round trip for scorecards, entries and history.
- [ ] AC-13 All endpoints are in the OpenAPI spec.

Frontend
- [ ] AC-14 Entries made without a connection are kept on the phone and saved automatically, in order, when the connection is back.
- [ ] AC-15 The event page refreshes every 60 seconds while visible.

## 10. Decisions

Decided on 2026-10-11:
- In `CASUAL` mode, players of the flight and organisers enter and correct scores.
- The last entry counts; a history shows who changed what.
- Bad reception: entries are kept on the phone and retried (no full offline in M1).
- Live view refreshes every 60 seconds; real-time push in a later version.
- Only gross strokes (plus picked up / conceded) in M1; more statistics data in v2.
- Events are completed by hand by an organiser and can be reopened.
