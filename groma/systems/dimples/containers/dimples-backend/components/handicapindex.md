---
type: C4 Component
title: Handicap Index
status: stable
groma:
  id: handicapindex
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/domain/HandicapIndex.java
      symbol: HandicapIndex
  technology: Java record, BigDecimal
description: WHS Handicap Index value from +10.0 to 54.0
---

Value object for a World Handicap System Handicap Index with one decimal place. Plus handicaps are negative (+2.3 is stored as -2.3). Kept separate from the golfer because handicap calculations (Course and Playing Handicap, strokes received) and event snapshots use it too.
