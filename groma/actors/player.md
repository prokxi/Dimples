---
type: C4 Actor
title: Player
status: stable
groma:
  id: player
description: Golfer who plays series and enters scores
---

A registered golfer with an official Handicap Index who plays in series with friends. Joins series through invite links, enters scores on the course (often on a phone) and follows results and standings. Golfer data is private by default.

## Draft relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [player](player.md) | [dimples-backend](../systems/dimples/containers/dimples-backend/container.md) | Plays series and enters scores | HTTPS, JSON |
