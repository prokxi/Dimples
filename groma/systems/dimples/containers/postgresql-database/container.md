---
type: C4 Container
title: PostgreSQL database
status: draft
groma:
  id: postgresql-database
  parent: dimples
---

PostgreSQL 17 database owned by the backend. The schema is managed only by Flyway migrations in src/main/resources/db/migration (none exist yet); aggregates are stored with Spring Data JDBC, IDs are application-generated UUIDs. Locally started from compose.yaml.
