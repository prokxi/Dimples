---
type: C4 Actor
title: Operator
status: stable
groma:
  id: operator
description: Runs the installation, resets passwords
---

Runs the Dimples installation and holds the OPERATOR role. Resets forgotten passwords (there is no email in M1) and can change or delete any course.

## Draft relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [operator](operator.md) | [dimples-backend](../systems/dimples/containers/dimples-backend/container.md) | Resets passwords | HTTPS, JSON |
