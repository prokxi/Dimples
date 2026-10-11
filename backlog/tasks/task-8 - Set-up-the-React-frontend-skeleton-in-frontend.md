---
id: TASK-8
title: Set up the React frontend skeleton in frontend/
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - frontend
  - build
milestone: m-0
dependencies: []
documentation:
  - backlog/docs/product/doc-2 - Product-Requirements-PRD.md
priority: high
type: chore
ordinal: 8000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
M1 is a responsive web app, but frontend/ does not exist yet (AGENTS.md, Frontend). All screen tasks depend on it. New npm packages and changes to the pom.xml need the owner's approval before implementation: list them in the plan and wait for the OK.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 The owner approved the list of npm packages and the pom.xml changes before implementation
- [ ] #2 React, TypeScript and Vite in frontend/; mobile-first layout shell with routing; English UI
- [ ] #3 The Maven build builds the SPA (frontend-maven-plugin) and Spring Boot serves it; SPA routes fall back to index.html
- [ ] #4 The Vite dev server proxies /api to localhost:8080
- [ ] #5 API types are generated from the OpenAPI spec
- [ ] #6 The API client sends the CSRF header and handles 401 by showing the login page
- [ ] #7 ./mvnw verify builds and tests backend and frontend
<!-- AC:END -->
