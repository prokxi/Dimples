---
type: C4 Container
title: Dimples backend
status: stable
groma:
  id: dimples-backend
  parent: dimples
  technology: Java 25, Spring Boot 4.1 (Spring MVC, Spring Security, Spring Data JDBC)
description: Spring Boot application serving the JSON API under /api/v1
---

The single deployable application. It is built as ports and adapters: `inbound` (REST controllers and DTOs) calls `service` (use cases and the interfaces they need), which works on the pure-Java `domain`; `outbound` implements the service interfaces (persistence with Spring Data JDBC, external clients). `config` wires everything and `exception` holds the shared error contract. Data is stored in PostgreSQL 17 through Flyway migrations; locally Postgres starts from compose.yaml. Authentication is session-based with CSRF protection. The planned React SPA in `frontend/` will be served by this application. ArchUnit tests enforce the layer rules.
