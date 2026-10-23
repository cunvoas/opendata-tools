# Geoservice Isochrone v2 Summary

## Document Status

- **Status:** Draft for review
- **Profile:** API service (Spring MVC + REST) with embedded scheduled worker and async publisher
- **Intended owner:** cunvoas (single maintainer, AGPL-3.0 association project)
- **Scope:** `geoservice-isochrone-v2/` directory inside the [cunvoas/opendata-tools](https://github.com/cunvoas/opendata-tools) repository (Maven artifact `com.github.cunvoas:geoservice-isochrone:2.0.11`)
- **Completeness:** Complete; per-city ingestion parsers (MEL, Lyon, Nantes, Toulouse) treated at catalog level, not inventoried
- **Last reviewed:** 2026-10-22
- **Evidence baseline:** Working tree on branch `feat/overpass`, including uncommitted overpass-related changes. Default branch `main` points at `d64b850`; the reviewed branch is ahead of or diverged from it, so some findings may be newer than `main`.
- **Validated commit:** `e2d64544eca5683e2b591c850a03063efd03b5ab`

## Purpose and Boundaries

This service computes and publishes how well city residents can reach parks and gardens, checked against WHO recommendations (a large green area within a short walk). It ingests French open data:

- INSEE 200m population grid (`InseeCarre200m`) and IRIS districts,
- IGN "topo végétal" and IGN isochrone API,
- OpenStreetMap parks via Overpass, and Cadastre boundaries,
- Parks/gardens datasets loaded per city (CSV/JSON parsers in `extern/mel`).

It then computes, per 200m grid cell, the accessibility score, proposes park modifications (6 resolution algorithms) so the WHO threshold is met, allows manual proposals, and exports final GeoJSON + OMS statistics as static files for consumption by a related static front (GitHub Pages `geoservice-data`).

Owns: its PostgreSQL/PostGIS schema (Flyway), compute job queue, park/accessibility domain, compute code, serving UI/API.

Does not own: source open-data portals, the published static website, the PostgreSQL instance itself, Matomo analytics server, Mailjet SMTP service.

## Component Context

| Relationship | Component or system | Purpose |
| --- | --- | --- |
| Upstream data | INSEE (200m grid, IRIS, density), IGN (topo végétal, isochrone API v2), OpenStreetMap/Overpass, Cadastre, city open-data portals | Reference shapes and isochrone computations |
| Data store | PostgreSQL + PostGIS | All reference and computed geodata, job queue, flyway migrations from v1.0.0 to v2.1.2 |
| Downstream | Static data export (GitHub Pages `geoservice-data` folder via `application.admin.export-path`) | Published GeoJSON + OMS statistics per EPCI/year |
| Outbound services | IGN isochrone API v2, adresse.data.gouv.fr (geocoding), Mailjet (email) | Isochrone computation, address search, contributor emails |
| Outbound analytics | Matomo (rest analytics client), Prometheus/Micrometer, optional Zipkin, Loki logback appender | Tracking and observability |
| Consumer | Internal users: contributors (park creation/edit), administrators (admin console); anonymous public users via consultation map | Thymeleaf + Leaflet/Vue pages served by this app |
| Secret providers | Environment variables (e.g. DB credentials, Mailjet keys in prod profile) | Never hardcoded in prod profile |

Runtime model: one Spring Boot 4.1.1 / Java 21 jar (Dockerized, non-root uid 1001), Tomcat on port 8980, single-threaded scheduler pool, Hikari pool max 50 connections to PostGIS.

## Architecture

| Area | Responsibility | Authoritative source |
| --- | --- | --- |
| Entry point | Boot application, Jackson customization, component scan | `src/main/java/com/github/cunvoas/geoserviceisochrone/GeoserviceIsochroneApplication.java` |
| MVC controllers | Thymeleaf pages: park list/new/edit/check, consultation map, entrances, admin (cities, EPCI, contributors, compute requests), manual proposals, project simulator | `src/main/java/com/github/cunvoas/geoserviceisochrone/controller/mvc/` |
| REST/GeoJSON controllers | GeoJSON feeds for map layers (parks, IRIS, INSEE cells, cadastre, IGN topo), compute job AJAX endpoints, publish endpoints, file exporter, address REST | `src/main/java/com/github/cunvoas/geoserviceisochrone/controller/geojson/`, `controller/rest/` |
| Job orchestration | Batch computation queue for 200m cells and IRIS, recycling, shutdown handling | `src/main/java/com/github/cunvoas/geoserviceisochrone/service/compute/BatchJobService.java` |
| Geospatial compute | Strategy v3/v4 for carre-200m computation, IRIS computation, park areas | `src/main/java/com/github/cunvoas/geoserviceisochrone/service/park/` |
| Proposal solvers | 6 algorithms to propose park configurations (see `ProposalComputationTypeAlgo`) | `service/solver/` |
| Manual proposals | CIRCLE/POLYGON proposals, evaluation vs OMS thresholds, `ManualEvalWork` persistence | `service/proposal/` |
| Overpass integration | Raw and GeoJSON parsers for OSM park data (in progress, see branch `feat/overpass`) | `src/main/java/com/github/cunvoas/geoserviceisochrone/extern/overpass/` |
| Persistence | Spring Data JPA + Hibernate Spatial; repositories split by reference/admin/proposal | `src/main/java/com/github/cunvoas/geoserviceisochrone/repo/` |
| Security config | Form login, Argon2, CSRF cookie, login attempt throttle, role-based method security | `src/main/java/com/github/cunvoas/geoserviceisochrone/config/security/` |
| Config | Business thresholds, feature flips (`application.business.*`), security props, hardening | `src/main/resources/application*.yml`, `config/property/` |
| Frontend views | Thymeleaf templates + Leaflet/Vue assets | `src/main/resources/templates/`, `static/` |
| Migrations | Flyway SQL v1.0.0 → v2.1.2, run automatically at startup | `src/main/resources/db/migration/` |

## Interfaces

Inbound:

- Thymeleaf MVC pages: park management (list/new/edit/check/export), consultation map, entrance management, admin console (contributors, cities, city↔EPCI association, compute requests, dashboard), project simulator, manual proposal editor.
- AJAX REST (under `/mvc/ajax/...`): `POST /mvc/ajax/jobs/api/request` (compute jobs; token + admin role for city/EPCI scope), `POST /mvc/ajax/publish/api/request` and `publish/api/status` (publication), file exporter endpoint (key + IP allowlist), analytics REST, contributor REST.
- GeoJSON feeds: parks, park overpass, park proposals, manual proposals, IRIS, INSEE 200m cells, cadastre, IGN topo vegetation, city/year selection.

Outbound:

- IGN isochrone API v2 (isochrone polygons around park entrances), configurable via `application.business.feature-flipping.isochrone-impl: ign-api-v2`.
- Overpass API (raw OSM JSON and GeoJSON parsers).
- adresse.data.gouv.fr (address-based auto-location of map).
- Mailjet (sender implemented in `extern/helper/EmailSender.java`; gated by feature flag `application.sendEmail`).
- Matomo (optional; blank URL and site-id 0 disable it).
- File system export: GeoJSON and statistics written under `application.admin.export-path` (published data repository).

Authoritative per-controller rules: [docs/workflow-rules/README.md](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/docs/workflow-rules).

## Core Domain Concepts

| Concept | Meaning or use | Ownership |
| --- | --- | --- |
| ParcEtJardin | A park/garden polygon loaded from city data or created by contributors | Produced |
| Entrance | Access point of a park; isochrone polygons are computed per entrance and merged | Produced |
| InseeCarre200m | 200m population grid cell (INSEE) | Consumed (INSEE data), computed values are Produced |
| IRIS | INSEE statistical district, second compute unit | Consumed (INSEE), computed values Produced |
| ComputeJob / ComputeIrisJob | Queue entries for one grid cell or IRIS + year, status TO_PROCESS/IN_PROCESS/PROCESSED/IN_ERROR | Produced |
| OMS criteria | WHO thresholds: urban (300s walk, ≥10ha site, 3ha within 300m), suburban (1200s, 45ha/25ha); empty-area threshold 5000 m² | Consumed (WHO), config Produced |
| ParkArea | Park-portion geometry relevant to accessibility (cleaned/cropped) | Produced |
| Proposal (auto/manual) | Park geometry proposal from one of 6 algorithms or drawn manually (CIRCLE/POLYGON) | Produced |
| Publication | Final static GeoJSON + OMS stats per EPCI and year | Produced |
| Contributeur | User account with roles; Argon2 passwords; token-protected compute triggers | Produced |

## Main Workflows

### Compute pipeline (carre 200m + IRIS)

1. On park modification: `BatchJobService.requestProcessParc(park)` or admin request for city/EPCI (`requestProcessCity`, `requestProcessCom2Co`) creates jobs (`compute_job`, `compute_iris_job`), reused/recycled when a newer park update exists.
2. A scheduler (`@Scheduled`, every 300s, pool size 1) processes `TO_PROCESS` jobs, throttled when CPU temperature exceeds `cpu-temp-threshold` (fetched from an external metrics URL).
3. Compute for a cell uses IGN isochrone data around entrances to determine the reachable park surface, stored as computed values per cell/year (carre-200m impl is pluggable, currently `v4`).
4. Jobs ending in error or left `IN_PROCESS` are recycled on the next pass; `DisposableBean` + graceful shutdown (`shutdown: graceful`, 60s grace, scheduler awaits termination 50s) protects long geospatial computations.

Effects: PostGIS rows per carre/IRIS/year updated; no side effects outside the database. Failure: job moves to `IN_ERROR` and is retried by recycling; no external data is mutated. Evidence: `BatchJobService`, `ComputeJobRestControler`, `config/app cores/services` profiles in `application.yml`, [BatchJobService doc](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/docs/BatchJobService.md).

### Auto / manual park proposals

1. Contributor opens proposal UI; `ParksPropositionController` runs one of 6 algorithms (iterative, least-squares-neighbour2, genetic1, chi-square, PPC/constraint solving via choco-solver, see `docs/*-strategy.md`).
2. Manual proposals (CIRCLE or POLYGON) are saved by `ManualProposalController` with meta mode MANUAL.
3. Impact evaluation via `ManualProposalGeoJsonController` computes accessibility gains against OMS bands and persists `ManualEvalWork` rows.

Effects: DB-only; its outputs feed later publication or real park edits. Partial results remain as `ManualEvalWork` and are not automatically applied. Evidence: `service/solver/`, `service/proposal/`, docs `proposal-computation-strategy.md`.

### Publication

1. `POST /publish/api/request` checks rights (`canPublish`: all compute jobs for the EPCI done), then delegates to `PublishService.publishAsync()`.
2. Heavy work runs async (the request returns `202`): GeoJSON export files to disk + OMS statistics bands for dense/suburban zones.
3. Status endpoint (`/publish/api/status`) lets the UI poll state.

Effects: writes files under `application.admin.export-path` (published static dataset) and DB rows; failure inside async job is not surfaced synchronously to the caller. Evidence: `PublishRestControler`, [publication.md](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/docs/publication.md).

## Configuration and Dependencies

- Runtime: Java 21, Spring Boot 4.1.1, Spring Security (form login + Argon2), Spring Data JPA/Hibernate Spatial, Thymeleaf, Micrometer Prometheus, optional Zipkin.
- Key config keys in `application.yml` (`application.business.*`: OMS thresholds, INSEE years, densities, feature flips `isochrone-impl`, `carre200m-impl`, `cpu-temp-threshold`, `cpu-metrics-url`); secrets are `${VAR}` placeholders in prod, no values in this summary.
- Database: PostgreSQL + PostGIS via `DB_URL`/`DB_USER`/`DB_PASSWORD`; Hikari pool (max 50, leak-detection 2s); `ddl-auto: none` with Flyway migrations at startup.
- Boundary limits: multipart 8MB, session cookie deliberately named `PHPSESSID` (security camouflage), CORS restricted to configured origin.
- Full dependency manifest: [pom.xml](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/pom.xml).

## Operational Characteristics

- Deployment: Docker image (non-root uid 1001), arbitrary runner; single node assumption; scheduler is single-threaded by design.
- Health/observability: actuator endpoints `health,info,prometheus,metrics` only (sensitive endpoints excluded); Loki logback appender ships logs; optional Zipkin tracing; CPU-temperature throttling via external metrics URL.
- Retry/backoff: batch job recycling treats `IN_PROCESS`/`IN_ERROR` as re-processable on next scheduled pass (300s); no exponential backoff.
- Database migrations: Flyway auto-run on startup (`baseline-on-migrate`, `clean-disabled`).
- Graceful shutdown: 60s grace period, scheduler waits up to 50s; long computations survive deploy only under that window.

## Constraints and Non-goals

### Verified Constraints

- Java 21 runtime; scheduler pool size 1 — geospatial work is serialized.
- Port 8980; CORS allows only configured origin; CSRF enforced on MVC flows.
- Public/anonymous role is read-only (consultation map); write actions require Contributeur role and some admin actions specifically `ADMINISTRATOR` (compute city/EPCI, city association, publication).
- Compute endpoints rely on a shared token (`TokenManagement`) plus authentication — callers must keep both valid.
- The app assumes its export path (static geodata repository) is accessible to users; deleting that folder disables the public data feed until republished.

### Non-goals

- Hosting the published public website or static geodata (that is the `gh_pages/geoservice-data` sibling project).
- Being the source of truth for INSEE/IGN/OSM data.
- Email deliverability, analytics storage (Matomo), or map tile hosting — relies on external services.

### Known Defects / Debt (loaded from repo security docs)

- `application-dev.yml` still contains real credentials (DB, JWT, Mailjet, admin password) per the committed audit — rotate, remove, and purge from Git history. Do not use dev-profile secrets anywhere outside throwaway local setups.
- `FileExporterControler` IP allowlist known buggy (workflow-rules doc).
- No rate limiting on compute endpoints (expensive isochrone calculations); only login brute-force throttling.
- `ErrorController` injects stack trace into the view model, toggled off by flag only.
- See [SECURITY_REVIEW.md](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/SECURITY_REVIEW.md), HowtoSecurityFix.md, depsBestPractices.md for the full remediation list.

## Current and Planned Behavior

### Implemented

- Cell/IRIS compute pipeline with job queue, recycling, CPU-based throttling, graceful shutdown.
- IGN isochrone API v2 client (isochrone impl flip `ign-api-v2`), address geocoding client.
- Six-algorithm proposal solver suite + manual proposal (circle/polygon) with OMS impact evaluation persisted as `ManualEvalWork`.
- Contributor management with role scoping, Argon2 password hashing, brute-force login throttling, method security on admin endpoints.
- Publication flow: preconditions (`canPublish`), async export of GeoJSON + stats, status polling.
- Notes/flags: `application.sendEmail=false` currently disables Mailjet sending; `matomo` URL empty disables tracking.

### Planned

- Overpass-based park ingestion: parsers exist (`extern/overpass`), repository layer worked on (`ParkOverpass*`), but the compute-jobs REST hook for overpass parks is still stubbed (`TODO OVERPASS`, returns `202` with 0 cells) on the current branch `feat/overpass`.
- Spring Boot 4 / hardened config imports (banner, whitelabel replacement page) referenced but not finalized.

### Unknown or To Verify

- Whether `main` has diverged from the reviewed `feat/overpass` changes (overpass compute integration, job model fields) — check `git diff main...feat/overpass` before relying on overpass behavior.
- The paper claim "six algorithms" — verify `ProposalComputationTypeAlgo` count against current enum in the branch you build on.
- Behavior of `FileExporterControler` allowlist under real traffic (docs call it buggy with no test evidence).

## Related Agent Instructions

| Instruction | Scope | Purpose | Status |
| --- | --- | --- | --- |
| docs/workflow-rules/README.md | docs/workflow-rules | Index of per-controller architectural rules (18 controllers/services) | Accepted |
| docs/workflow-rules/compute-job-rest-controler.md | ComputeJobRestControler | Queue entry points, token + admin gate | Accepted |
| docs/workflow-rules/batch-job-service.md | BatchJobService | Recycling, 300s cycle, CPU overheat gate | Accepted |
| docs/workflow-rules/manual-proposal-*, park-*, publish-*, consult-map, entrance, project-simulator, contributeur, city-association, file-exporter | Individual controllers | Behavior contracts per controller | Accepted |
| TESTING_GUIDE.md, QUICK_START_TESTS.md | testing | How to run unit/integration tests | Accepted |
| SECURITY_REVIEW.md (fr), HowtoSecurityFix.md (fr/en), depsBestPractices.md (fr/en) | security/deps | Audit results and remediation steps | Accepted |

## Authoritative References

- [Repository](https://github.com/cunvoas/opendata-tools) — component lives under `geoservice-isochrone-v2/`
- [Entry point](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/src/main/java/com/github/cunvoas/geoserviceisochrone/GeoserviceIsochroneApplication.java)
- [Workflow rules index](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/docs/workflow-rules)
- [Batch compute service doc](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/BatchJobService.md)
- [Publication flow doc](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/publication.md)
- [ComputeCarreServiceV4 doc](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/ComputeCarreServiceV4.md)
- [Config root](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/src/main/resources/application.yml)
- [Flyway migrations](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/src/main/resources/db/migration)
- [Security audit](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/SECURITY_REVIEW.md)
- [Tests](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/src/test)
- [pom.xml (dependencies)](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/pom.xml)
