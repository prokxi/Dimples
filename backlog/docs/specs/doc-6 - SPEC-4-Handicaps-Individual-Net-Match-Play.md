---
id: doc-6
title: SPEC-4 Handicaps & Individual Net Match Play
type: specification
created_date: '2026-10-11 11:28'
updated_date: '2026-10-11 11:33'
---
# SPEC-4: Handicaps & Individual Net Match Play

Status: Accepted · Owner: prokxi · Last updated: 2026-10-11
Covers: PRD FR-30 · Milestone: M1 · Depends on: SPEC-1 (Handicap Index), SPEC-2 (tee ratings), SPEC-3 (event snapshot)

## 1. Goal

Dimples calculates Course Handicaps, Playing Handicaps and strokes received according to the World Handicap
System (WHS), and evaluates individual net match play hole by hole and for the whole match. Everything in this
spec is **pure domain logic** in `domain`, without Spring or a database, and is tested thoroughly, including
mutation testing.

The format is the first implementation of a **format strategy**, so later formats (stroke play, Stableford,
team formats) can be added without changing this one.

## 2. User stories

- **US-1** As a player, I see my Course Handicap, my Playing Handicap and on which holes I receive strokes in my match.
- **US-2** As a player, I see after every hole who won it and the match status (e.g. "2 up", "dormie", "all square").
- **US-3** As a player, I see the final result of a match in the usual notation (e.g. "3&2", "1 up", "halved").
- **US-4** As an organiser, I rely on the calculation following WHS, also for 9-hole events.

## 3. Handicap calculation

All values come from the event snapshot (SPEC-3): Handicap Index on the event date, Course Rating, Slope Rating and
par for the hole selection (SPEC-2).

### 3.1 Course Handicap (WHS Rule 6.1)

| Hole selection | Formula |
|---|---|
| 18 holes | `Handicap Index × (Slope Rating ÷ 113) + (Course Rating − Par)` |
| 9 holes (9-hole course, front or back nine) | `(Handicap Index ÷ 2) × (Slope Rating ÷ 113) + (Course Rating − Par)` |

The result is rounded to the nearest whole number. How exactly .5 is rounded (in particular for plus handicaps) is
checked against the official WHS rules and their worked examples during implementation and covered by tests.

### 3.2 Playing Handicap (WHS Rule 6.2)

`Playing Handicap = Course Handicap × handicap allowance`, rounded the same way. The allowance comes from the event
(default 100 % for singles match play, SPEC-3).

### 3.3 Strokes received in a match

- The player with the lower Playing Handicap plays off 0. The other player receives the **difference** `d`.
- The holes of the hole selection are ranked by stroke index: rank 1 is the hole with the lowest stroke index.
  On a nine of an 18-hole tee this is the order of the stroke indexes within that nine.
- With `n` holes, every hole gets `d ÷ n` strokes (integer division), and holes with rank ≤ `d mod n` get one more.
  Example: 20 strokes over 18 holes → 2 strokes on the holes with rank 1 and 2, 1 stroke on all others.
- Plus handicaps work the same way: only the difference counts.

## 4. Match evaluation

### 4.1 Input per hole

Per player and hole (entered in SPEC-5):
- **Gross strokes** (1–20), or
- **Picked up**: no score.

Per match and hole, optionally:
- **Conceded by** one of the players: that player gives up the hole, whether or not they have a score.

### 4.2 Hole result

In this order:
1. One player conceded the hole → the other player wins it.
2. Both players have gross strokes → **net = gross − strokes received on that hole**; the lower net wins; equal is halved.
3. Only one player has gross strokes (the other picked up) → that player wins the hole.
4. Both picked up → halved.
5. A score is missing → the hole has no result yet.

### 4.3 Match status and result

- Holes are played in the order of their numbers within the hole selection (1–18, 1–9 or 10–18).
- The **status** is calculated over the holes with a result, in that order, up to the first hole without a result.
  It shows the lead (e.g. "A 2 up", "all square") and the holes played and remaining.
- **Dormie**: a player leads by exactly as many holes as remain.
- The match is **decided** as soon as the lead is greater than the number of remaining holes.
  - Decided before the last hole: result "*lead* & *remaining*", e.g. 3 up with 2 to play → "3&2".
  - Decided on the last hole: "*lead* up", e.g. "1 up" or "2 up".
  - All square after the last hole: **halved**. There is no playoff.
- **Holes after the decision** are still played and their scores are entered. They are kept for statistics but do not
  change the result.
- Result values: `PLAYER_A_WON`, `PLAYER_B_WON`, `HALVED`, plus the notation. Points per result come from the series
  (SPEC-3, SPEC-6).

## 5. Format strategy

- `domain` defines a format interface. Given the event snapshot, the players and the hole entries, a format
  calculates the playing handicaps, the strokes per hole, the hole results, and the result of a match or competitor.
- `INDIVIDUAL_MATCH_PLAY_NET` is the first implementation. The event's `format` field (SPEC-3) selects it.
- Adding a format means adding an implementation and an enum value. Existing formats stay unchanged.
- The exact Java signatures are designed during implementation, based on what this format and the next one
  (stroke play / Stableford, M2) need. Nothing is built "for later" that this format does not use.

## 6. Integration

- **SPEC-3 (start)**: when an event starts, Course Handicap and Playing Handicap of every match player are
  calculated with this spec and stored in the snapshot.
- **API**: `EventResponse` (SPEC-3) gets per match player: `handicapIndex`, `courseHandicap`, `playingHandicap`,
  `strokesReceived` per hole. The match status and result are delivered with the scores (SPEC-5).
- No own database tables or endpoints.

## 7. Out of scope

- Net Double Bogey, score differentials and calculating a Handicap Index from rounds (stroke play M2, FR-63).
- Other formats and team formats (M2).
- Extra holes / playoffs.
- Shotgun starts (starting on a hole other than the first of the selection).
- Women's ratings (SPEC-2).

## 8. Acceptance criteria

All criteria are unit tests in `domain` without Spring. `./mvnw verify -Pmutation` passes.

Handicaps
- [ ] AC-1 Course Handicap for 18 holes, e.g. Handicap Index 12.4, CR 71.3, SR 128, par 72 → 13; Handicap Index 20.0 → 22.
- [ ] AC-2 Course Handicap for 9 holes with half the Handicap Index, e.g. Handicap Index 12.4 on a front nine with derived rating CR 35.7, SR 128, par 36 → 7; Handicap Index 20.0 → 11.
- [ ] AC-3 Rounding, including .5 and plus handicaps, matches the official WHS examples.
- [ ] AC-4 Playing Handicap applies the allowance and rounds, e.g. Course Handicap 22 at 90 % → 20.
- [ ] AC-5 Test cases include the worked examples from the official WHS rules.

Strokes
- [ ] AC-6 The lower Playing Handicap plays off 0; the other receives the difference (e.g. 13 vs 22 → 9 strokes on stroke index 1–9).
- [ ] AC-7 More strokes than holes: 20 strokes over 18 holes → 2 strokes on rank 1–2, 1 stroke on the rest; works the same for 9 holes.
- [ ] AC-8 On a nine of an 18-hole tee, strokes go to the holes ranked by stroke index within that nine.
- [ ] AC-9 Equal Playing Handicaps → no strokes; plus handicaps → only the difference counts.

Match
- [ ] AC-10 Hole results follow the rules in 4.2, including concession, picked up on one or both sides, and missing scores.
- [ ] AC-11 Status: lead, all square, dormie, holes played and remaining; stops at the first hole without a result.
- [ ] AC-12 Results: decided early ("3&2", "2&1"), decided on the last hole ("1 up", "2 up"), halved after the last hole; also for 9 holes (front and back nine).
- [ ] AC-13 Scores after the decision are accepted and do not change the result.
- [ ] AC-14 The format strategy selects `INDIVIDUAL_MATCH_PLAY_NET` from the event's format.

## 9. Decisions

Decided on 2026-10-11:
- WHS standard: the lower Playing Handicap plays off 0, the other receives the difference.
- 9-hole events use half the Handicap Index; strokes follow the stroke index order within the nine.
- Remaining holes are played after a match is decided, and their scores are kept for statistics.
- Holes can be conceded or picked up.
