---
type: C4 Actor
title: Organiser
status: stable
groma:
  id: organiser
description: Player who runs a series
---

A player who also runs a series; each series has at least one. Sets up the series, events, teams and pairings, invites players and can correct scores.

## Draft relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [organiser](organiser.md) | [dimples-backend](../systems/dimples/containers/dimples-backend/container.md) | Runs series and events | HTTPS, JSON |
