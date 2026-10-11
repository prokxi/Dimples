---
type: C4 Component
title: Clock configuration
status: stable
groma:
  id: clock-configuration
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/config/ClockConfig.java
      symbol: ClockConfig
  technology: java.time.Clock
description: Provides the injectable system clock
---

Single `Clock` bean in the server time zone. Services and adapters take time and today's date from it so tests can control time.
