---
id: doc-8
title: SPEC-6 Results & Standings
type: specification
created_date: '2026-10-11 11:36'
updated_date: '2026-10-11 11:40'
---
# SPEC-6: Results & Standings

Status: Accepted · Owner: prokxi · Last updated: 2026-10-11
Covers: PRD FR-25, FR-26 (team points), FR-50, FR-51 · Milestone: M1 · Depends on: SPEC-3, SPEC-4, SPEC-5

## 1. Goal

Dimples calculates the series standings for players and teams automatically from the match results, with clear
tie-breaks. Members see the official standings (completed events) and a live view that also includes matches of
running events.

## 2. User stories

- **US-1** As a member, I see the player standings of the series: rank, points, matches, wins/halved/losses, byes, hole difference.
- **US-2** As a member, I see the team standings.
- **US-3** As a member, I switch to the live view during an event and see how the standings would look now.
- **US-4** As a member, I see a player's matches in the series with their results and points.
- **US-5** As an organiser, I change the points per result, and the standings are recalculated.

## 3. Calculation

Standings are **calculated on request** from the events and scorecards. They are not stored. With at most 16 members
and 50 events this is cheap. The calculation is pure domain logic in `domain`.

### 3.1 Counted matches

| View | Counts |
|---|---|
| **Official** (default) | All matches of `COMPLETED` events. |
| **Live** | Official, plus the matches of `STARTED` events that already have a result (SPEC-4). Running matches without a result don't count yet. Marked as provisional. |

`DRAFT` and `CANCELLED` events never count.

### 3.2 Player values

Per player (every golfer who ever played a counted match or had a bye, including removed and deleted golfers):

| Value | Meaning |
|---|---|
| Points | Sum of match points with the series' current `MatchPoints` (default win 3, halved 1, loss 0). |
| Matches | Number of counted matches. |
| W / H / L | Wins, halved, losses. |
| Byes | Number of byes in counted events. A bye gives **0 points** and is not a match. |
| Hole difference | Per match: the winner gets **+lead**, the loser **−lead** ("3&2" → +3 / −3, "1 up" → +1 / −1), halved 0. Summed up. |

Players who miss an event get nothing for it. The standings use **total points** (no average).

### 3.3 Ranking and tie-breaks

Players are ordered by:
1. Points (more is better)
2. Wins
3. **Head-to-head** among the tied players only: the points they earned in counted matches *against each other*.
   With more than two tied players, this is a mini table of their mutual matches.
4. Hole difference
5. Otherwise they **share the rank** (e.g. 1, 2, 2, 4), ordered by display name.

### 3.4 Team standings

- A team's values are the sums of its players' values (points, wins, halved, losses, hole difference) from counted matches.
- Only matches count in which the player **belonged to the team when the event started**. For this, the event snapshot
  (SPEC-3, 4.5) also stores each participant's team at the start. A late joiner's points from before joining the team do not count for the team.
- If teammates play each other (e.g. round robin), both results count for the team.
- Ranking: points, wins, head-to-head (points of the tied teams' players in matches against players of the other tied teams),
  hole difference, otherwise shared rank.
- Without teams in the series, there are no team standings.

### 3.5 Changes

Changing the points per result (SPEC-3) or reopening an event and correcting scores (SPEC-5) changes the standings
immediately, because they are always calculated from the current data.

## 4. REST API

All endpoints are under `/api/v1/series/{seriesId}`, need an authenticated golfer, and are visible to **members only**
(404 for everyone else).

| Method & path | Request | Success |
|---|---|---|
| `GET /standings?view=official\|live` | `If-None-Match` | 200 `StandingsResponse` + `ETag`, or 304 |
| `GET /standings/players/{golferId}/matches` | – | 200 list of `PlayerMatchResponse` (404 if the golfer has no counted match or bye in the series) |

- `StandingsResponse {view, provisional, countedEvents, players: [{rank, golferId, displayName, points, matches, wins, halved, losses, byes, holeDifference}], teams: [{rank, teamId, name, points, wins, halved, losses, holeDifference}]}`
- `PlayerMatchResponse {eventId, eventName, date, opponent, result, notation, points, provisional}`; byes are listed with 0 points.
- Deleted golfers appear as "Deleted golfer" without an ID.
- All endpoints are documented with OpenAPI annotations.

## 5. Persistence

No new tables. `event_participant` (SPEC-3, `V5__create_event.sql`) also stores the participant's team at the start
(`team_id`, nullable, FK, index). Both specs are implemented before V5 is committed, so no separate migration is needed.

## 6. Frontend (SPA)

- **Standings page** of a series: player table and (if there are teams) team table, with the toggle "Official / Live".
  The live view is clearly marked as provisional and refreshes every 60 seconds while it is visible.
- Tap a player → their matches in the series.
- Mobile-first: on narrow screens only rank, name, points and matches; the other columns on wider screens or when expanded.

## 7. Out of scope

- Statistics beyond the standings (v2).
- Other point systems (e.g. points per hole won) and other formats (M2).
- Exporting or printing the standings.

## 8. Acceptance criteria

Domain
- [ ] AC-1 The official view counts only `COMPLETED` events; the live view adds decided matches of `STARTED` events; `DRAFT` and `CANCELLED` never count.
- [ ] AC-2 Points use the series' `MatchPoints`; byes give 0 points and are counted separately, not as matches.
- [ ] AC-3 Hole difference: "3&2" → +3/−3, "1 up" → +1/−1, "2 up" → +2/−2, halved → 0.
- [ ] AC-4 Ranking: points, wins, head-to-head among the tied players (also with three or more), hole difference, then shared rank (1, 2, 2, 4).
- [ ] AC-5 Team values are the sums of their players' values, only for matches in which the player belonged to the team at the event start; teammates playing each other count both results.
- [ ] AC-6 Team ranking uses the same order of tie-breaks.
- [ ] AC-7 Changing `MatchPoints` or correcting a score changes the standings on the next request.

Service & API
- [ ] AC-8 Non-members get 404 for the standings and a player's matches.
- [ ] AC-9 The standings endpoint returns `ETag` and 304 when nothing changed.
- [ ] AC-10 Removed and deleted golfers stay in the standings; deleted golfers as "Deleted golfer".
- [ ] AC-11 The participant's team is stored at the event start (in `event_participant`, migration V5).
- [ ] AC-12 All endpoints are in the OpenAPI spec.

## 9. Decisions

Decided on 2026-10-11:
- Official standings count completed events; a live view adds decided matches of running events as provisional.
- Tie-breaks: points, wins, head-to-head, hole difference, then shared rank.
- Total points, no average; missed events give nothing.
- Team points are the sum of their players' match points, ranked with the same tie-breaks.
