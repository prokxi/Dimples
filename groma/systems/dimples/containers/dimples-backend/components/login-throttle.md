---
type: C4 Component
title: Login throttle
status: stable
groma:
  id: login-throttle
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/service/LoginThrottle.java
  technology: In-memory, java.time.Clock
description: Blocks an email for 15 minutes after 5 failed logins
---

Counts failed logins per normalised email in memory (enough for a single instance). After 5 failures within 15 minutes, further attempts for that email get 429 for 15 minutes; a successful login resets the count. Stale entries are dropped once many emails are tracked.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/service/LoginThrottle.java](../../../../../../src/main/java/com/prokxi/dimples/service/LoginThrottle.java) | [src/main/java/com/prokxi/dimples/exception/GlobalExceptionHandler.java](../../../../../../src/main/java/com/prokxi/dimples/exception/GlobalExceptionHandler.java) | Reports blocked logins | Java exception |
