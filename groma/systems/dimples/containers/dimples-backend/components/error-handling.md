---
type: C4 Component
title: Error handling
status: stable
groma:
  id: error-handling
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/exception/GlobalExceptionHandler.java
    - scanner: java
      file: src/main/java/com/prokxi/dimples/exception/NotFoundException.java
      symbol: NotFoundException
    - scanner: java
      file: src/main/java/com/prokxi/dimples/exception/AccessDeniedException.java
      symbol: AccessDeniedException
    - scanner: java
      file: src/main/java/com/prokxi/dimples/exception/ConflictException.java
      symbol: ConflictException
  technology: Spring MVC @RestControllerAdvice, RFC 9457 ProblemDetail
description: Shared exceptions and their mapping to ProblemDetail responses
---

Services signal failures with `NotFoundException` (404, also used for resources the caller may not see), `AccessDeniedException` (403) and `ConflictException` (409); their messages become the problem detail, so they must not contain personal data. `GlobalExceptionHandler` is the only controller advice: it maps these, returns 400 with the list of invalid fields for Bean Validation errors (without the rejected values), lets Spring MVC map its own exceptions, hands Spring Security exceptions back to the security filter chain and answers anything unexpected with a generic 500 while logging the cause.
