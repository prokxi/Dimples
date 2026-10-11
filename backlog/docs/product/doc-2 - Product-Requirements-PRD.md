---
id: doc-2
title: Product Requirements (PRD)
type: specification
created_date: '2026-10-11 09:56'
updated_date: '2026-10-11 11:33'
---
# Product Requirements (PRD)

Status: Accepted · Owner: prokxi · Last updated: 2026-10-11 · Based on: doc-1 Product Vision

## 1. Goal

Casual golfers can organise and play their own tournaments and tournament series with friends.
Later they can also see their golf data from several sources in one place.

**v1 success:** the owner's friend group runs its 2027 tournament series in Dimples.
**Target:** M1 is ready before the friend group's 2027 season. No fixed date yet.

## 2. Personas

| Persona | Description | Main needs |
|---|---|---|
| **Player** | Casual golfer, plays with a fixed group of friends, has an official Handicap Index. | Join a series, enter scores easily (also on the course), follow results and standings. |
| **Organiser** | A player who also runs a tournament or series. A series can have several organisers. | Set up series and events, choose formats, invite players, make pairings, correct scores. |
| **Data golfer** (later) | Uses launch monitors, golf watches or tracking apps. | See all shots and rounds in one place, compare across sources. |

## 3. Core concepts

- **Series (season)**: a set of events over a period of time chosen by the organisers, with
  overall standings. A single tournament is a series with one event.
- **Event**: one competition within a series, played on a course and tee on a date, in a
  **format** (e.g. match play net, Stableford, stroke play).
- **Format**: the scoring rules of an event. Formats are pluggable: a new format must not
  require changes to existing ones (a scoring strategy per format).
- **Competitors**: individual players or **teams** of players, depending on the format.
- **Organiser role**: per series. One or more players can hold it.
- **Score entry mode**: per series or event, controls who may enter and confirm scores
  (e.g. *casual*: anyone in the flight enters scores; *strict*: a marker has to confirm them).

## 4. Functional requirements

Priority: **M1** = needed for the first usable version, **later** = planned for a later milestone (see section 6).

### Accounts & profile
| ID | Requirement | Priority |
|---|---|---|
| FR-1 | Golfers register and log in with email and password. | M1 |
| FR-2 | Golfers keep a profile with name and their **official Handicap Index**, which they update themselves. The history of values is kept. | M1 |
| FR-3 | Golfers send, accept and decline **friend requests**. | later |
| FR-4 | Golfers change their password. Without email in M1, a forgotten password is reset by the operator. | M1 |
| FR-5 | Golfers delete their account. Their personal data is removed; past series results stay but no longer show who played. | M1 |
| FR-6 | Email sending: "forgot password" and invitations by email. | later |

### Courses
| ID | Requirement | Priority |
|---|---|---|
| FR-10 | Golfers enter a course with tees: Course Rating, Slope Rating, and par and stroke index per hole. Entered courses can be reused by everyone. | M1 |
| FR-11 | Import course data from an external course database. | later |

### Series & events
| ID | Requirement | Priority |
|---|---|---|
| FR-20 | A golfer creates a series with a name, start and end date, and the planned number of events. The creator becomes an organiser. | M1 |
| FR-21 | Organisers make other participants organisers too. A series always has at least one organiser. | M1 |
| FR-22 | Organisers invite players to a series (shareable invite link). Invited friends can join directly. **Only registered golfers can take part**; there are no guest players. | M1 (link), later (friends) |
| FR-23 | Organisers create events in a series: course, tee, date, format, and **9 or 18 holes** (for 9 holes on an 18-hole course: front or back nine). Handicaps for 9-hole events follow the WHS 9-hole rules. | M1 |
| FR-24 | Organisers create the matches of an event with a **pairing mode**: **random**, **round robin** (over the events of the series), or **team nominations** (each team ranks its players; number 1 plays the other team's number 1, and so on). Organisers can adjust the pairings by hand and set the flights. | M1 |
| FR-25 | Match results count towards the **series standings** with configurable points. Default: **win 3, halved 1, loss 0**. | M1 |
| FR-26 | Organisers form **teams** of players for a series. Team points are the sum of their players' match points. Needed for team nominations (FR-24). | M1 |
| FR-27 | Team formats where team members play together (see FR-33). | later |

### Formats
| ID | Requirement | Priority |
|---|---|---|
| FR-30 | **Individual match play, net**: strokes received according to WHS (Playing Handicap = Course Handicap × allowance, default 100 %; the lower-handicap player plays off 0, the other receives the difference, allocated by stroke index), result per hole and per match (e.g. "3&2"). | M1 |
| FR-31 | Stroke play gross and net (with Net Double Bogey). | later |
| FR-32 | Stableford (net). | later |
| FR-33 | Team formats: four-ball and foursomes (match play and stroke play), scramble. | later |

### Scoring
| ID | Requirement | Priority |
|---|---|---|
| FR-40 | Players enter strokes per hole **live during the round** on a phone. | M1 |
| FR-41 | Scores can also be entered or completed **after the round** (e.g. after bad reception). | M1 |
| FR-42 | **Casual** score mode: every player in a flight (and every organiser) can enter and correct the scores of that flight. | M1 |
| FR-43 | **Strict** score mode: a marker has to confirm a player's scores before they count; only organisers can change confirmed scores. | later |
| FR-44 | Score entry keeps working without a connection and syncs later (offline-first). | later |

### Results
| ID | Requirement | Priority |
|---|---|---|
| FR-50 | Event results and match status are visible to all participants and update while scores are entered. | M1 |
| FR-51 | Series standings are calculated automatically from the event results. | M1 |
| FR-52 | Live updates by real-time push instead of refreshing every minute. | later |

### Data & statistics (later milestones)
| ID | Requirement | Priority |
|---|---|---|
| FR-60 | Import shot data from Trackman (CSV upload). | later |
| FR-61 | Import data from Garmin and round-tracking apps. | later |
| FR-62 | Statistics across sources, shareable with friends. | later |
| FR-63 | Show a **calculated handicap** from rounds played in Dimples, next to the official one. | later |
| FR-64 | Dimples' own round and shot tracker. | later |

## 5. Non-functional requirements

- **Responsive web app** (desktop and phone). A native app is not planned for v1.
- **UI language: English only.** Units and number formats are still shown as the user prefers.
- **Legal basics**: legal notice (Impressum) and privacy policy pages, reachable without login.
  Account deletion: see FR-5.
- **No email in M1** (see FR-6).
- **Scale**: designed for small groups. A series has up to about 10 players. No special scaling work for v1.
- **Privacy**: rounds and data are private by default. Series data is visible only to its
  participants. Resources the caller may not see return 404.
- **Handicap rules** follow WHS. Dimples is not the official handicap authority.
- **Units**: SI storage, displayed according to the user's preference.
- **Security**: session login, CSRF protection, passwords hashed with bcrypt (see AGENTS.md).
- Architecture, tests and quality gates as described in AGENTS.md.

## 6. Milestones

| Milestone | Content | Target |
|---|---|---|
| **M1 – Series MVP** | FR-1, 2, 4, 5, 10, 20–26, 30, 40–42, 50, 51; responsive SPA; deployment | before the 2027 season |
| **M2 – Formats & friends** | FR-3, 6, 27, 52, 31–33, 43, friend invitations | 2027 |
| **M3 – Shot data** | FR-60, 61 | 2027 |
| **M4 – Stats & handicap** | FR-62, 63 | 2027 |
| **M5 – Own tracking** | FR-44, 64 | later |

## 7. Out of scope

Tee-time booking, booking or marketplace features, club administration, official federation
competitions, native apps (for now).

## 8. Decisions & open questions

Answered on 2026-10-11:
- Series points: win 3, halved 1, loss 0 (configurable per series).
- Pairing modes: random, round robin, team nominations (FR-24).
- Size: up to about 10 players per series.
- UI language: English only.
- Only registered golfers can play; no guest players.
- No email sending in M1.
- Events can have 9 or 18 holes.
- Legal notice, privacy policy and account deletion are part of M1.
- Hosting (decision-1): Hetzner Cloud VPS in Germany, Docker Compose with the app, PostgreSQL
  and Caddy (automatic HTTPS), daily `pg_dump` backups to Hetzner storage, deployment from
  GitHub Actions through GitHub Container Registry. Deploy early in M1.

Open:
1. **Handicap allowance** for singles match play: 100 % of the Course Handicap is assumed
   (WHS recommendation), configurable per event.
