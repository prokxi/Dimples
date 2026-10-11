---
id: TASK-9
title: Deploy Dimples to a Hetzner Cloud VPS
status: To Do
assignee: []
created_date: '2026-10-11 11:39'
labels:
  - infra
  - build
milestone: m-0
dependencies:
  - TASK-4
documentation:
  - backlog/docs/product/doc-2 - Product-Requirements-PRD.md
priority: high
type: chore
ordinal: 9000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Dimples runs on a Hetzner Cloud VPS with Docker Compose (decision-1, PRD section 8). Deploying early, as soon as login works, avoids surprises at the end of M1. Changes to the build, new GitHub Actions and the server setup need the owner's approval; the owner provides server access and the domain.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 The owner approved the build and infrastructure changes before implementation
- [ ] #2 A Docker image of the app is built and pushed to GitHub Container Registry by GitHub Actions
- [ ] #3 A production Compose file runs the app, PostgreSQL and Caddy with automatic HTTPS
- [ ] #4 Secrets are not in the repository; secure cookies are enabled in production
- [ ] #5 A daily pg_dump backup is stored outside the server, and a restore was tested once
- [ ] #6 A deployment is triggered from GitHub Actions and documented in a short runbook
<!-- AC:END -->
