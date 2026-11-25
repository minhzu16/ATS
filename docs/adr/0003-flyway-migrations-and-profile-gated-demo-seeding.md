# ADR-0003: Flyway Schema Migrations and Profile-Gated Demo Seeding

## Status
Accepted

## Context
Running `hibernate.ddl-auto=update` in production risks unintended schema drift, while seeding demo accounts (`hr@ats.com`, `admin@ats.com`) unconditionally would expose predictable credentials in live environments.

## Decision
1. **Flyway Migrations + Hibernate Validation**: Production configuration (`application.properties`) sets `spring.flyway.enabled=true` with `classpath:db/migration/V1__baseline.sql` and `spring.jpa.hibernate.ddl-auto=validate`. Test suites override this with H2 in-memory `create-drop` and Flyway disabled.
2. **Profile-Gated Demo Data**: `DemoDataSeederService`, `DemoScorecardSeedHelper`, and `DemoPipelineSeedHelper` are annotated with `@Profile("dev")` so demo data is only seeded when `SPRING_PROFILES_ACTIVE=dev` is explicitly enabled.

## Consequences
- Production startup fails fast if the database schema diverges from JPA entities.
- Demo accounts and sample pipelines never exist in non-`dev` deployments.
