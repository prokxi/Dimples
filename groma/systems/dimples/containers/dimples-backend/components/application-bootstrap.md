---
type: C4 Component
title: Application bootstrap
status: stable
groma:
  id: application-bootstrap
  parent: dimples-backend
  code:
    - scanner: java
      file: src/main/java/com/prokxi/dimples/DimplesApplication.java
      symbol: DimplesApplication
  technology: Spring Boot
description: Starts the Spring Boot application
---

Entry point of the backend. `@SpringBootApplication` component scanning and auto-configuration bring up the web layer, security, Flyway and data access; the beans of all other packages are discovered from here.
