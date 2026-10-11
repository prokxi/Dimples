---
type: C4 Component
title: Authentication API
status: stable
groma:
  id: authentication-api
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/inbound/AuthController.java
      symbol: AuthController
    - scanner: java
      file: src/main/java/com/prokxi/dimples/inbound/RegisterRequest.java
      symbol: RegisterRequest
    - scanner: java
      file: src/main/java/com/prokxi/dimples/inbound/LoginRequest.java
      symbol: LoginRequest
    - scanner: java
      file: src/main/java/com/prokxi/dimples/inbound/GolferResponse.java
      symbol: GolferResponse
  technology: Spring MVC, springdoc-openapi
description: REST endpoints for CSRF token, registration, login and logout
---

Exposes `/api/v1/auth/csrf`, `/register`, `/login` and `/logout` with the `RegisterRequest`, `LoginRequest` and `GolferResponse` DTO records and OpenAPI annotations. Registration goes to the account service and then logs the new golfer in; login and logout are delegated to Session login. Responses never contain domain objects; the email and passwords are hidden in DTO `toString`.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/inbound/AuthController.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/AuthController.java) | [src/main/java/com/prokxi/dimples/service/AccountService.java](../../../../../../src/main/java/com/prokxi/dimples/service/AccountService.java) | Registers golfers | Java method call |
| [src/main/java/com/prokxi/dimples/inbound/AuthController.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/AuthController.java) | [src/main/java/com/prokxi/dimples/inbound/SessionLogin.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/SessionLogin.java) | Logs golfers in and out | Java method call |
| [src/main/java/com/prokxi/dimples/inbound/AuthController.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/AuthController.java) | [src/main/java/com/prokxi/dimples/inbound/RequestFields.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/RequestFields.java) | Validates request fields | Java method call |
