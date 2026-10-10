# AGENTS.md

Guidance for AI coding agents working in this repository. Read it fully before making changes.
If something here conflicts with an explicit instruction from the user, the user wins — but point out the conflict.

## Project overview

**Dimples** is a web application for a golf community. Golfers use it to:

- **View and analyse their shot data**, aggregated from many sources: launch monitors and golf
  systems (e.g. Trackman, Garmin), round-tracking apps, and later our own tracking feature.
- **Connect with friends** and share rounds and stats with them.
- **Create and play tournaments** in several formats (stroke play, Stableford, match play, scramble/team).

The project is at an early stage: the package structure exists but is mostly empty. Expect to create
the first real classes of each kind — when you do, follow the conventions below so later code can copy them.

## Tech stack

| Area         | Choice                                                                       |
|--------------|------------------------------------------------------------------------------|
| Language     | Java 25                                                                      |
| Framework    | Spring Boot 4.1 (Spring MVC, Spring Security, Spring Data JDBC)              |
| Database     | PostgreSQL 17                                                                |
| Migrations   | Flyway (`src/main/resources/db/migration`)                                   |
| API docs     | springdoc-openapi 3 (OpenAPI 3 + Swagger UI)                                 |
| Build        | Maven via the wrapper (`./mvnw`)                                             |
| Local DB     | Docker Compose (`compose.yaml`), started automatically by Spring Boot        |
| Tests        | JUnit 5, Mockito, Spring Boot Test, Testcontainers (PostgreSQL)              |
| Boilerplate  | Java records first, Lombok only where records don't fit                      |
| Frontend     | React + TypeScript + Vite in `frontend/` (planned, not created yet)          |

Note: this is **Spring Data JDBC, not JPA**. There is no lazy loading, no dirty checking, no
`@Entity`/`@OneToMany`. Aggregates are loaded and saved as a whole; references between aggregates
are by ID. Don't add JPA/Hibernate.

## Commands

```bash
./mvnw spring-boot:run      # run the app; starts Postgres via compose.yaml (Docker must be running)
./mvnw test                 # unit + integration tests (Testcontainers needs Docker)
./mvnw verify               # full build — must pass before you call a task done
./mvnw -DskipTests package  # build the jar without tests
./mvnw test -Dtest='*ArchitectureTest'  # architecture rules only — fast, no Docker; run after every change
./mvnw spring-javaformat:apply          # format the code (the build fails on unformatted code)
docker compose down         # stop the local Postgres
```

### Guardrails

These turn the rules in this file into checks. When one fails, fix the code — never weaken, skip or
work around the check. Changing a check needs the user's approval.

- **ArchUnit** (`src/test/java/com/prokxi/dimples/*ArchitectureTest.java`): dependency rules, naming
  conventions and coding rules (no field injection, no `System.out`, no JPA, no `Instant.now()` without
  a `Clock`, `@Transactional` only in `service`, controllers return DTOs). Runs in every `./mvnw test`.
- **spring-javaformat**: `./mvnw verify` fails on unformatted code. Run `./mvnw spring-javaformat:apply`.
- **Maven Enforcer**: Java 25+, Maven 3.9+, and bans JPA/Hibernate ORM, JUnit 4/TestNG, Joda-Time.
- **JaCoCo**: `./mvnw verify` fails if `domain` or `service` (each package, including sub-packages) drops
  below 90% line or 80% branch coverage. See the Testing section.
- **PIT mutation testing** (`./mvnw verify -Pmutation`): mutates `domain` code and fails if the
  `domain` unit tests kill fewer than 80% of the mutants. See the Testing section.
- **gitlint** (`.gitlint`, `.githooks/commit-msg`): every commit message must be a Conventional Commit
  (allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`,
  `revert`), with a title of at most 72 characters. If a commit is rejected, fix the message and commit again.
- **Claude Code hooks** (`.claude/settings.json`, `.claude/hooks/`): block edits to committed migrations and
  badly named migrations, force pushes, pushes to `main`, commits on `main`, `--no-verify`, changes to
  `core.hooksPath` and skip flags for the checks above. When you finish with changed Java files, the code is formatted and the architecture
  tests run; if they fail, you have to fix the violations before you are done.

- Docker Compose support only runs with `spring-boot:run` / from the IDE. It is **disabled in tests**;
  tests get their database from `TestcontainersConfiguration` via `@ServiceConnection`.
- The app listens on `http://localhost:8080`. Swagger UI: `/swagger-ui.html`, spec: `/v3/api-docs`.
  Both are behind Spring Security until the security config permits them.
- Local Postgres data is not persisted across `docker compose down`.

## Architecture

Ports & adapters (hexagonal), organised **by type** at the top level:

```
src/main/java/com/prokxi/dimples/
├── DimplesApplication.java
├── config/      Spring configuration & wiring (security, OpenAPI, beans). No business logic.
├── domain/      Domain model: aggregates, value objects, domain logic. Pure Java.
├── service/     Use cases (application services) + the interfaces they need (ports).
├── inbound/     Driving adapters: REST controllers, request/response DTOs, file upload endpoints.
├── outbound/    Driven adapters: persistence (Spring Data JDBC), external API clients, importers.
└── exception/   Shared exception types and the global exception handler.
```

### Dependency rules (must hold)

```
inbound  ──▶ service ──▶ domain
                 ▲
                 │ implements interfaces defined in service
outbound ────────┘
```

- `domain` depends on **nothing** else in the project and on no Spring/framework classes
  (plain Java, records, enums, `java.*`). Lombok is not used in `domain`.
- `service` depends only on `domain` (and `exception`). It **never imports** from `inbound` or `outbound`.
  It defines interfaces for everything it needs from the outside world (persistence, external APIs, clock, …).
- `inbound` depends on `service` and `domain`. Never on `outbound`.
- `outbound` implements interfaces from `service` and maps to/from `domain`. Never depends on `inbound`.
- `config` may depend on everything — it wires things together.
- Domain objects must not leak out of the API: controllers return DTOs, not domain objects.
  Persistence rows (`outbound`) are not domain objects either: map at the adapter boundary.

If a change seems to require breaking a rule, stop and ask instead of working around it.

### Naming conventions

Plain names, no `Port`/`Adapter` suffixes:

| Kind                                     | Package     | Example                                  |
|------------------------------------------|-------------|------------------------------------------|
| Domain aggregate / value object          | `domain`    | `Round`, `Shot`, `Distance`, `Tournament`|
| Use case / application service          | `service`   | `RoundService`, `TournamentService`      |
| Interface the service needs (port)       | `service`   | `RoundRepository`, `GarminClient`        |
| Implementation of that interface         | `outbound`  | `RoundRepositoryImpl`, `GarminClientImpl`|
| Spring Data JDBC repository (internal)   | `outbound`  | `RoundJdbcRepository`                    |
| Persistence row type                     | `outbound`  | `RoundRow`                               |
| REST controller                          | `inbound`   | `RoundController`                        |
| Request / response DTO                   | `inbound`   | `CreateRoundRequest`, `RoundResponse`    |
| Configuration                            | `config`    | `SecurityConfig`, `OpenApiConfig`        |
| Exception                                | `exception` | `NotFoundException`, `AccessDeniedException` |

- Spring Data `CrudRepository`/`ListCrudRepository` interfaces live in `outbound` and are only used by
  the matching `…Impl`. The service never sees them.
- Keep packages flat for now. When one gets crowded, introduce sub-packages by feature or
  technology (e.g. `outbound/persistence`, `outbound/garmin`, `inbound/rest`) and mirror them in tests.
- Empty packages contain a `.gitkeep`; delete it once the package has a real file.

## Coding conventions

- **English** for everything: code, comments, docs, commit messages, API error messages.
- **Indentation: tabs**, matching the existing Spring Initializr files. Keep the style of surrounding code.
- **Records first**: DTOs, value objects, events, commands and immutable domain types are records.
  Validate invariants in the compact constructor.
- **Lombok only where records don't fit**: `@RequiredArgsConstructor` on Spring beans, `@Slf4j` for logging.
  Avoid `@Data`, `@Setter`, `@Builder` on domain types. Never use Lombok in `domain`.
- **Constructor injection only** (via `final` fields + `@RequiredArgsConstructor` or an explicit constructor).
  No field injection (`@Autowired` on fields).
- Prefer immutability; return unmodifiable collections; avoid `null` in APIs — use `Optional` for
  "may be absent" return values (not for fields or parameters).
- Use `java.time` (`Instant` for timestamps, `LocalDate` for dates). Inject a `Clock` where time matters
  so tests can control it.
- Use Bean Validation (`@Valid`, `@NotNull`, `@Size`, …) on inbound DTOs; enforce business rules in `domain`/`service`.
- Use `@Transactional` on service methods that write, not on controllers or repositories.
- No commented-out code, no `System.out`, no unused code "for later". Keep comments for the *why*.
- Do not introduce new frameworks or architectural patterns without asking.

## Domain guidelines

### Glossary

| Term             | Meaning                                                                                  |
|------------------|------------------------------------------------------------------------------------------|
| Golfer           | A registered user of Dimples.                                                            |
| Friendship       | A confirmed connection between two golfers (request → accept).                          |
| Course / Tee     | A golf course and its tee sets; each tee has Course Rating, Slope Rating and par per hole.|
| Round            | One golfer playing a course from a tee on a date; consists of hole scores and shots.     |
| Shot             | A single stroke, normalised from any source (see below).                                 |
| Shot source      | Where data came from: e.g. `TRACKMAN`, `GARMIN`, a tracking app, `MANUAL`, `DIMPLES_TRACKER`. |
| Session          | A practice session (e.g. on a launch monitor/range) — shots without a course round.      |
| Tournament       | A competition with a format, participants/teams, rounds and a leaderboard.               |
| Handicap Index   | WHS handicap of a golfer.                                                                 |
| Course/Playing Handicap | Handicap for a specific course/tee/format, derived from the Handicap Index.        |

### Shot data ingestion

Shot data comes from many sources and must be **normalised into one source-independent model**.

- `domain` has a single `Shot` model (club, carry/total distance, ball speed, launch angle, spin,
  start/end location, lie, … — whatever fields the source provides; most are optional).
- **Each source gets its own adapter**:
  - Pulled from an external API (e.g. Garmin) → client in `outbound` implementing an interface from `service`.
  - Uploaded by the user (e.g. a Trackman CSV export) → upload endpoint in `inbound`, parser/mapper per format.
  - Our own tracker → its own inbound endpoint.
- Adapters map source data into the domain model and **convert units on the way in** (see Units).
- **Keep the original raw payload** (e.g. JSONB column or stored file reference) together with the
  source, source ID and import time, so data can be re-processed when mappings improve.
- Imports must be **idempotent**: importing the same source data twice must not create duplicate shots
  (deduplicate on source + source ID).
- Adding a new source must not require changes to existing sources' code.

### Units

- **Store SI units only**: meters, meters/second, degrees, revolutions per minute (rpm) for spin.
- **Convert for display per user preference** (yards/mph vs. meters/km/h). Conversion happens at the
  edge (inbound DTO mapping or frontend), never in persistence.
- Make units explicit in names or types (e.g. `carryMeters`, or a `Distance` value object) —
  never a bare `double distance`.

### Handicap & scoring

- Use the **World Handicap System (WHS)**: Handicap Index, Course Rating, Slope Rating,
  Course Handicap, Playing Handicap (with format-specific allowances), Net Double Bogey adjustment.
- Put handicap and scoring calculations in `domain` as pure functions/classes with thorough unit tests
  (use worked examples from the WHS rules as test cases).
- Tournament formats to support: **stroke play** (gross and net), **Stableford**, **match play**,
  **scramble / team formats**. Model the format so new ones can be added without rewriting existing ones
  (e.g. a scoring strategy per format).

### Privacy & ownership

- Shot data, rounds and stats are **private by default**. Only the owner can see them unless they
  explicitly share with friends or a tournament they take part in.
- **Every endpoint that returns or changes golfer data must check ownership/visibility in the service
  layer.** Never rely on the frontend hiding things. Return 404 (not 403) for resources the caller may not
  see, so their existence isn't revealed.
- Add tests for the "other user tries to access it" case for every such endpoint.
- Never log personal data, shot payloads or credentials.

## Persistence (Spring Data JDBC + Flyway)

- Schema changes are made **only** through Flyway migrations in `src/main/resources/db/migration`,
  named `V<n>__<description>.sql` (e.g. `V1__create_golfer.sql`, `V2__create_round.sql`).
- **Never edit a migration that has been committed** — add a new one instead.
- Write migrations in PostgreSQL SQL. Use `snake_case` table/column names, plural or singular
  consistently (prefer singular: `golfer`, `round`, `shot`), `bigint generated always as identity` or
  `uuid` primary keys (be consistent once chosen), `timestamptz` for timestamps, foreign keys and
  indexes on foreign-key columns.
- Don't use `spring.sql.init` / `schema.sql`.
- Spring Data JDBC: one repository per aggregate root; reference other aggregates by ID
  (`AggregateReference` or plain ID), not by object.
- Map persistence rows ↔ domain objects in the `…Impl` class.

## REST API

- All endpoints under **`/api/v1/...`**, resource-oriented, plural nouns: `/api/v1/rounds/{id}`.
- JSON only. Requests and responses are **DTO records** in `inbound`; never expose domain or persistence types.
- Use proper status codes: `201 Created` + `Location` header for creates, `204` for deletes,
  `400` for validation errors, `404` for missing/not-visible resources, `409` for conflicts.
- Errors are returned as **RFC 9457 `ProblemDetail`** from a single `@RestControllerAdvice` in `exception`.
  Don't build ad-hoc error bodies in controllers.
- Paginate list endpoints (`page`, `size`, sensible max size).
- Document endpoints with OpenAPI annotations where the generated docs aren't clear
  (`@Operation`, `@Tag`, `@Schema` on DTOs). Keep the spec accurate — the SPA relies on it.
- Controllers are thin: validate input, call one service method, map the result to a DTO.

## Security

- **Session-based login** with Spring Security (form/JSON login, users stored in the database,
  passwords hashed with the default `DelegatingPasswordEncoder` / bcrypt).
- The SPA authenticates with the **session cookie**; **CSRF protection stays enabled**
  (cookie-based CSRF token readable by the SPA). Never disable CSRF.
- Endpoints are authenticated by default; explicitly permit only what must be public
  (login, registration, static SPA assets, OpenAPI docs if desired).
- **Ask before changing security configuration** (`SecurityFilterChain`, auth rules, CORS, CSRF,
  password handling). Never loosen security to make a test pass — use `@WithMockUser` /
  `spring-security-test` helpers in tests instead.
- Never commit secrets. The credentials in `compose.yaml` are for local development only.

## Frontend (planned)

- A React + TypeScript + Vite SPA will live in **`frontend/`** in this repository. It does not exist yet;
  don't create it unless asked.
- Plan: built with `frontend-maven-plugin` during the Maven build, output copied into the Spring Boot
  static resources so the app serves the SPA; Vite dev server proxies `/api` to `localhost:8080` during development.
- The SPA talks to the backend only through `/api/v1` and should use types generated from the OpenAPI spec.

## Testing

Every code change comes with tests. `./mvnw verify` must pass.

- **Coverage** is measured by JaCoCo during `./mvnw verify`. When the coverage check fails, look up the
  uncovered lines and branches in `target/site/jacoco/jacoco.csv` (per class) or `jacoco.xml` (per line),
  or open `target/site/jacoco/index.html`, and add tests that assert real behaviour. Never write tests
  that only execute code to raise the number.
- If you configure Surefire's `argLine`, keep `@{argLine}` in it, otherwise the JaCoCo agent is dropped
  and coverage silently becomes 0%.
- **Mutation testing**: when you change code in `domain`, run `./mvnw verify -Pmutation` before you call
  the task done. PIT makes small changes to the code (e.g. `<` → `<=`, `+` → `-`, returning `0`) and checks
  that a test fails for each one. Surviving mutants are listed in `target/pit-reports/mutations.xml` and
  `target/pit-reports/index.html`: add assertions or cases that kill them. Only `domain` unit tests are used,
  so keep them free of Spring and Docker.

- **Unit tests** (fast, no Spring context) for `domain` and `service`. Mock the service's
  interfaces with Mockito. Domain logic (handicap, scoring, unit conversion, import mapping) should
  be covered thoroughly, including edge cases.
- **Integration tests** with Testcontainers PostgreSQL:
  - `outbound` persistence: `@DataJdbcTest` + `@Import(TestcontainersConfiguration.class)`; verifies
    mappings and Flyway migrations against real Postgres.
  - `inbound` REST: `@WebMvcTest` with mocked services for controller/validation/error mapping;
    `@SpringBootTest` for a few end-to-end flows. Include security (unauthenticated → 401, other user → 404).
- Importers: test each source adapter with **real sample files/payloads** stored under
  `src/test/resources/<source>/`.
- Test package structure mirrors main (`src/test/java/com/prokxi/dimples/<package>`).
- Test names describe behaviour: `rejectsShotWithNegativeCarry()`, `stablefordAwardsTwoPointsForNetPar()`.
- Use AssertJ for assertions. Don't use `Thread.sleep` — inject a `Clock`.
- Don't disable, skip or delete failing tests to get a green build. Fix the cause or ask.

## Git workflow

- Branches: `main` (released state), `develop` (integration). Work on **feature branches off `develop`**:
  `feature/<short-name>`, `fix/<short-name>`, `chore/<short-name>`.
- PRs go into `develop`. `main` only receives merges from `develop`.
- **Never push to `main`. Never force-push shared branches. Only commit or push when the user asks.**
- Commit messages follow **Conventional Commits**, in English, imperative:
  `feat(service): add Stableford scoring`, `fix(outbound): dedupe Garmin shots by source id`,
  `test: …`, `refactor: …`, `docs: …`, `chore: …`, `build: …`. gitlint checks this in a `commit-msg` hook.
  One-time setup per clone: install gitlint (`pipx install gitlint` or `uv tool install gitlint-core`) and
  run `git config core.hooksPath .githooks`.
- Keep commits focused; don't mix refactoring with behaviour changes.

## What agents may do — and what needs approval

**Allowed without asking:**
- Add/modify code and tests within the architecture rules above.
- Write **new** Flyway migrations.
- Run builds, tests, and the app locally.

**Ask first:**
- Adding, removing or upgrading **Maven dependencies** (or npm packages, once the frontend exists).
- Any change to **security configuration**.
- Changes to **`compose.yaml`, `pom.xml` build/plugin settings**, or the Maven wrapper.
- Breaking the dependency rules, changing the package structure, or introducing new architectural patterns.
- Changing a public API contract in a breaking way.
- Deleting files you didn't create in the current task.

## Definition of done

- [ ] Code follows the architecture rules and naming conventions.
- [ ] Tests added/updated; `./mvnw verify` passes.
- [ ] If `domain` changed: `./mvnw verify -Pmutation` passes.
- [ ] Schema changes are in a new Flyway migration.
- [ ] Endpoints check ownership/visibility and are documented in the OpenAPI spec.
- [ ] No secrets, no personal data in logs, no disabled tests.
- [ ] Summarise what changed and anything you couldn't verify.
