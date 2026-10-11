---
id: TASK-4
title: 'Registration, login, logout and the security configuration'
status: Done
assignee:
  - '@claude'
created_date: '2026-10-11 11:39'
updated_date: '2026-10-11 12:27'
labels:
  - backend
  - security
milestone: m-0
dependencies:
  - TASK-1
  - TASK-3
references:
  - authentication-api
  - session-login
  - security-configuration
  - account-service
  - login-throttle
  - golfer-login-lookup
  - clock-configuration
  - request-field-mapping
  - error-handling
  - golfer
documentation:
  - backlog/docs/specs/doc-3 - SPEC-1-Accounts-Profile.md
modified_files:
  - .claude/worktrees/agent-af5357e2f849103bc
  - groma/actors/player.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/account-service.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/authentication-api.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/clock-configuration.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/error-handling.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/golfer-login-lookup.md
  - groma/systems/dimples/containers/dimples-backend/components/golfer.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/login-throttle.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/request-field-mapping.md
  - >-
    groma/systems/dimples/containers/dimples-backend/components/security-configuration.md
  - groma/systems/dimples/containers/dimples-backend/components/session-login.md
  - src/main/java/com/prokxi/dimples/config/.gitkeep
  - src/main/java/com/prokxi/dimples/config/ClockConfig.java
  - src/main/java/com/prokxi/dimples/config/SecurityConfig.java
  - src/main/java/com/prokxi/dimples/domain/Password.java
  - src/main/java/com/prokxi/dimples/exception/GlobalExceptionHandler.java
  - src/main/java/com/prokxi/dimples/exception/InvalidFieldException.java
  - src/main/java/com/prokxi/dimples/exception/TooManyRequestsException.java
  - src/main/java/com/prokxi/dimples/inbound/.gitkeep
  - src/main/java/com/prokxi/dimples/inbound/AuthController.java
  - src/main/java/com/prokxi/dimples/inbound/GolferResponse.java
  - src/main/java/com/prokxi/dimples/inbound/LoginRequest.java
  - src/main/java/com/prokxi/dimples/inbound/RegisterRequest.java
  - >-
    src/main/java/com/prokxi/dimples/inbound/RequestAttributeRememberMeServices.java
  - src/main/java/com/prokxi/dimples/inbound/RequestFields.java
  - src/main/java/com/prokxi/dimples/inbound/SecurityProblemHandler.java
  - src/main/java/com/prokxi/dimples/inbound/SessionLogin.java
  - src/main/java/com/prokxi/dimples/service/AccountService.java
  - src/main/java/com/prokxi/dimples/service/GolferUserDetails.java
  - src/main/java/com/prokxi/dimples/service/GolferUserDetailsService.java
  - src/main/java/com/prokxi/dimples/service/LoginThrottle.java
  - src/main/resources/application.properties
  - src/main/resources/db/migration/V2__create_remember_me_token.sql
  - src/test/java/com/prokxi/dimples/domain/PasswordTest.java
  - src/test/java/com/prokxi/dimples/exception/GlobalExceptionHandlerTest.java
  - src/test/java/com/prokxi/dimples/inbound/.gitkeep
  - src/test/java/com/prokxi/dimples/inbound/AuthenticationFlowTest.java
  - src/test/java/com/prokxi/dimples/service/.gitkeep
  - src/test/java/com/prokxi/dimples/service/AccountServiceTest.java
  - src/test/java/com/prokxi/dimples/service/GolferUserDetailsServiceTest.java
  - src/test/java/com/prokxi/dimples/service/LoginThrottleTest.java
  - src/test/java/com/prokxi/dimples/service/MutableClock.java
priority: high
type: feature
ordinal: 4000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Golfers need accounts before they can do anything in Dimples (PRD FR-1). This task defines the first SecurityFilterChain; the owner approved the configuration in SPEC-1 sections 4.2, 4.3 and 5. Remember-me keeps golfers logged in on their phone during a round and across deploys, because sessions are in memory.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 POST /api/v1/auth/register creates a golfer with a bcrypt hash, logs them in and returns 201 with Location (SPEC-1 AC-7)
- [x] #2 Duplicate active email returns 409; passwords outside 10 to 64 characters or over 72 bytes return 400 (AC-8)
- [x] #3 Wrong email and wrong password return the same 401; the 6th failed attempt within 15 minutes returns 429 (AC-9)
- [x] #4 Remember-me with persistent tokens (migration V2) keeps a golfer logged in after session expiry and restart (AC-10)
- [x] #5 CSRF uses a cookie readable by the SPA; GET /api/v1/auth/csrf issues it; changing requests without a token get 403 (AC-11)
- [x] #6 Unauthenticated API requests return 401, not a redirect (AC-12)
- [x] #7 Logout ends the session and the remember-me token
- [x] #8 No passwords, hashes or emails are logged (AC-17)
- [x] #9 Endpoints are documented in OpenAPI
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. domain.Password (10-64 characters, max 72 UTF-8 bytes, hidden toString).
2. exception: InvalidFieldException (400 with the same errors list as Bean Validation) and TooManyRequestsException (429), mapped in GlobalExceptionHandler.
3. service: AccountService (register with bcrypt hash, 409 for an active email incl. unique-index race; getGolfer), GolferUserDetailsService + GolferUserDetails (principal carries the golfer ID, username = email), LoginThrottle (in memory, Clock-based: 5 failures in 15 min block the email for 15 min).
4. config: SecurityConfig (stateful session, csrf.spa() with XSRF-TOKEN cookie, JSON 401/403 ProblemDetail instead of redirects, no form/basic login, DaoAuthenticationProvider with DelegatingPasswordEncoder, persistent-token remember-me 30 days with a random in-memory key, explicit SecurityContextRepository), ClockConfig.
5. inbound: AuthController (GET /auth/csrf, POST /auth/register, /auth/login, /auth/logout) with DTO records and OpenAPI annotations; SessionLogin helper (authenticate, change session ID, save context, remember-me, logout); request field mapping turns domain validation errors into 400 field errors.
6. V2__create_remember_me_token.sql (persistent_logins, username = email up to 254 chars).
7. application.properties: session timeout 2h, cookie HttpOnly/Secure/SameSite=Lax.
8. Tests: unit tests for Password, LoginThrottle, AccountService, GolferUserDetailsService; @SpringBootTest + Testcontainers flow tests for every acceptance criterion, including logs without personal data and the OpenAPI spec.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Deviation from plan: the session strategy also rotates the CSRF token on login (CsrfAuthenticationStrategy, as Spring's form login does); the SPA fetches a new token via GET /api/v1/auth/csrf after login/registration. Tests do not use spring-security-test csrf(): it permanently swaps the shared context's CsrfFilter repository for a session-based one, so the flow test uses the real XSRF-TOKEN cookie + X-XSRF-TOKEN header. Decisions to know: /v3/api-docs and Swagger UI require a login (not opened up, security config changes need approval); session and remember-me cookies are always Secure (browsers accept them on http://localhost); the Clock uses the server time zone; persistent_logins.username holds the email (up to 254 chars). Validation: ./mvnw -q verify -Pmutation green; AuthenticationFlowTest 14 tests on Postgres; domain/service line coverage 100%; PIT 84/84.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Added registration, JSON login/logout and the first SecurityFilterChain: session login with session-ID change and CSRF rotation, SPA cookie CSRF, 401/403 as ProblemDetail, 30-day persistent remember-me (V2 migration), in-memory login throttle (429 after 5 failures in 15 min), Password value object, InvalidFieldException/TooManyRequestsException mappings and OpenAPI docs. Verified with unit tests (Password, LoginThrottle, AccountService, GolferUserDetailsService, handler) and a @SpringBootTest Testcontainers flow test covering every AC, plus a green ./mvnw -q verify -Pmutation.
<!-- SECTION:FINAL_SUMMARY:END -->
