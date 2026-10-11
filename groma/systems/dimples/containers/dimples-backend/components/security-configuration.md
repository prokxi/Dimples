---
type: C4 Component
title: Security configuration
status: stable
groma:
  id: security-configuration
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/config/SecurityConfig.java
      symbol: SecurityConfig
  technology: Spring Security 7
description: 'Security filter chain: session login, CSRF, remember-me'
---

Defines the only `SecurityFilterChain` (approved in SPEC-1 sections 4.2, 4.3 and 5): everything is authenticated except the CSRF token, registration and login endpoints; CSRF protection uses a readable `XSRF-TOKEN` cookie for the SPA; there is no form or basic login; 401/403 come as ProblemDetail. Also provides the password encoder, the authentication manager, the session strategy and the persistent-token remember-me services.

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/config/SecurityConfig.java](../../../../../../src/main/java/com/prokxi/dimples/config/SecurityConfig.java) | [src/main/java/com/prokxi/dimples/inbound/SessionLogin.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/SessionLogin.java) | Wires remember-me and problem responses | Spring beans |
| [src/main/java/com/prokxi/dimples/config/SecurityConfig.java](../../../../../../src/main/java/com/prokxi/dimples/config/SecurityConfig.java) | [src/main/java/com/prokxi/dimples/service/GolferUserDetailsService.java](../../../../../../src/main/java/com/prokxi/dimples/service/GolferUserDetailsService.java) | Authenticates with golfer lookup | Spring beans |
