---
type: C4 Component
title: Session login
status: stable
groma:
  id: session-login
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/inbound/SessionLogin.java
      symbol: SessionLogin
    - scanner: java
      file: src/main/java/com/prokxi/dimples/inbound/RequestAttributeRememberMeServices.java
      symbol: RequestAttributeRememberMeServices
    - scanner: java
      file: src/main/java/com/prokxi/dimples/inbound/SecurityProblemHandler.java
      symbol: SecurityProblemHandler
  technology: Spring Security 7 (session, persistent-token remember-me)
description: Logs golfers in and out for the JSON endpoints
---

Does for the JSON API what Spring Security form login and logout would do: checks the login throttle, authenticates with the authentication manager, changes the session ID and rotates the CSRF token, stores the security context in the session and, on request, issues a 30-day persistent remember-me token (`persistent_logins`, migration V2). Logout deletes the token, invalidates the session and clears the cookies. Also answers security failures in the filter chain as ProblemDetail JSON (401 instead of a redirect, 403 e.g. for a missing CSRF token).

## Relationships

| Source | Target | Description | Technology |
| --- | --- | --- | --- |
| [src/main/java/com/prokxi/dimples/inbound/SessionLogin.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/SessionLogin.java) | [src/main/java/com/prokxi/dimples/service/LoginThrottle.java](../../../../../../src/main/java/com/prokxi/dimples/service/LoginThrottle.java) | Checks failed logins | Java method call |
| [src/main/java/com/prokxi/dimples/inbound/SessionLogin.java](../../../../../../src/main/java/com/prokxi/dimples/inbound/SessionLogin.java) | [src/main/java/com/prokxi/dimples/service/GolferUserDetailsService.java](../../../../../../src/main/java/com/prokxi/dimples/service/GolferUserDetailsService.java) | Loads registered golfers | Java method call |
