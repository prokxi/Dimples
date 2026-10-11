---
type: C4 Component
title: Golfer login lookup
status: stable
groma:
  id: golfer-login-lookup
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/service/GolferUserDetailsService.java
      symbol: GolferUserDetailsService
    - scanner: java
      file: src/main/java/com/prokxi/dimples/service/GolferUserDetails.java
      symbol: GolferUserDetails
  technology: Spring Security UserDetailsService
description: Loads active golfers by email for login and remember-me
---

Spring Security `UserDetailsService` backed by the golfer repository. Only active golfers can log in; unknown, invalid or deleted emails look like a wrong password. The principal `GolferUserDetails` carries the golfer ID that controllers pass to services, the email as username and the role as authority (`ROLE_GOLFER` or `ROLE_OPERATOR`).

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/service/GolferUserDetailsService.java](../../../../../../src/main/java/com/prokxi/dimples/service/GolferUserDetailsService.java) | [src/main/java/com/prokxi/dimples/service/GolferRepository.java](../../../../../../src/main/java/com/prokxi/dimples/service/GolferRepository.java) | Finds active golfers | Java interface |
