---
id: TASK-1
title: Add Bean Validation and the global ProblemDetail error handler
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - backend
milestone: m-0
dependencies: []
documentation:
  - backlog/docs/product/doc-2 - Product-Requirements-PRD.md
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
- [ ] #1 spring-boot-starter-validation is in the pom.xml (version managed by Spring Boot)
- [ ] #2 Invalid request bodies return 400 as ProblemDetail with the invalid fields listed
- [ ] #3 NotFoundException, AccessDeniedException and a conflict exception map to 404, 403 and 409 ProblemDetail responses
- [ ] #4 Unexpected errors return 500 ProblemDetail without stack traces or personal data
- [ ] #5 Tests cover every mapping
<!-- AC:END -->
