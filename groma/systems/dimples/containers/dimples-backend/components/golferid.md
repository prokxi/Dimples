---
type: C4 Component
title: Golfer ID
status: stable
groma:
  id: golferid
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/GolferId.java
      symbol: GolferId
description: Application-generated UUID that identifies a golfer; other aggregates reference golfers by it
---
