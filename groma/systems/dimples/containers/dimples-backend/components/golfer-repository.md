---
type: C4 Component
title: Golfer repository
status: stable
groma:
  id: golfer-repository
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/service/GolferRepository.java
      symbol: GolferRepository
  technology: Java interface
description: Service-side contract for loading and saving golfers
---

Interface the service layer defines for storing golfers: save (insert or update with optimistic locking), find by ID (active or deleted) and find an active golfer by email. Services depend only on this interface; the implementation lives in outbound.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/service/GolferRepository.java](../../../../../../src/main/java/com/prokxi/dimples/service/GolferRepository.java) | [src/main/java/com/prokxi/dimples/domain/Golfer.java](../../../../../../src/main/java/com/prokxi/dimples/domain/Golfer.java) | Loads and saves golfers | Java types |
