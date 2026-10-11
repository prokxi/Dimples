---
id: TASK-1
title: Add Bean Validation and the global ProblemDetail error handler
status: Done
assignee:
  - '@claude'
created_date: '2026-10-11 11:39'
updated_date: '2026-10-11 11:55'
labels:
  - backend
milestone: m-0
dependencies: []
documentation:
  - backlog/docs/product/doc-2 - Product-Requirements-PRD.md
modified_files:
  - pom.xml
  - src/main/java/com/prokxi/dimples/exception/.gitkeep
  - src/main/java/com/prokxi/dimples/exception/NotFoundException.java
  - src/main/java/com/prokxi/dimples/exception/AccessDeniedException.java
  - src/main/java/com/prokxi/dimples/exception/ConflictException.java
  - src/main/java/com/prokxi/dimples/exception/GlobalExceptionHandler.java
  - src/test/java/com/prokxi/dimples/exception/GlobalExceptionHandlerTest.java
priority: high
type: chore
ordinal: 1000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Every M1 endpoint needs input validation and RFC 9457 error responses, but the project has neither yet. spring-boot-starter-validation is missing from the pom.xml although AGENTS.md requires Bean Validation on DTOs; the owner approved adding it on 2026-10-11 (SPEC-1, section 10). The single @RestControllerAdvice in the exception package is the place all later features report errors through.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 spring-boot-starter-validation is in the pom.xml (version managed by Spring Boot)
- [x] #2 Invalid request bodies return 400 as ProblemDetail with the invalid fields listed
- [x] #3 NotFoundException, AccessDeniedException and a conflict exception map to 404, 403 and 409 ProblemDetail responses
- [x] #4 Unexpected errors return 500 ProblemDetail without stack traces or personal data
- [x] #5 Tests cover every mapping
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Add spring-boot-starter-validation to pom.xml (version from Spring Boot BOM).
2. Add NotFoundException, AccessDeniedException and ConflictException to the exception package.
3. Add GlobalExceptionHandler (@RestControllerAdvice extending ResponseEntityExceptionHandler): 400 with invalid fields list (property 'errors'), 404/403/409 for the shared exceptions, 500 generic without details. Spring Security exceptions are rethrown so the security filter chain still handles them.
4. @WebMvcTest with a test-only controller covering every mapping (incl. unauthenticated security path left untouched).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Invalid fields are returned as 'errors' [{field, message}] sorted by field; rejected values are left out (may be passwords). Spring Security's AccessDeniedException/AuthenticationException are rethrown from the advice so the security filter chain still answers 401/403 instead of the 500 catch-all. Validation: ./mvnw -q verify green (GlobalExceptionHandlerTest 8 tests, architecture tests).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Added spring-boot-starter-validation, the shared NotFoundException/AccessDeniedException/ConflictException and GlobalExceptionHandler (RFC 9457 ProblemDetail: 400 with invalid fields, 404/403/409, generic 500 without internals; Spring MVC errors via ResponseEntityExceptionHandler). Verified with GlobalExceptionHandlerTest (@WebMvcTest, one test per mapping) and a green ./mvnw -q verify.
<!-- SECTION:FINAL_SUMMARY:END -->
