---
type: C4 Component
title: Account service
status: stable
groma:
  id: account-service
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/service/AccountService.java
      symbol: AccountService
  technology: Spring service
description: Use cases of a golfer's own account
---

Registers golfers (password hashed with the delegating bcrypt encoder, 409 if an active golfer already uses the email, including a lost race on the unique index) and returns active golfers. Later account use cases from SPEC-1 (profile, Handicap Index, password change, deletion) belong here too.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/service/AccountService.java](../../../../../../src/main/java/com/prokxi/dimples/service/AccountService.java) | [src/main/java/com/prokxi/dimples/service/GolferRepository.java](../../../../../../src/main/java/com/prokxi/dimples/service/GolferRepository.java) | Loads and saves golfers | Java interface |
| [src/main/java/com/prokxi/dimples/service/AccountService.java](../../../../../../src/main/java/com/prokxi/dimples/service/AccountService.java) | [src/main/java/com/prokxi/dimples/domain/Golfer.java](../../../../../../src/main/java/com/prokxi/dimples/domain/Golfer.java) | Creates golfers | Java method call |
