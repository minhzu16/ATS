# PJ7 — Applicant Tracking System (ATS)

Spring Boot / Java 21 backend (`backend/`) + React 19 Vite frontend (`frontend/`).

## Common Commands
- Backend verify (Spotless, Checkstyle, JUnit, JaCoCo): `mvn -f backend/pom.xml -B verify jacoco:report` *(requires JDK 21)*
- Backend format apply: `mvn -f backend/pom.xml spotless:apply`
- Frontend tests: `cd frontend && npm test`
- Frontend lint: `cd frontend && npm run lint`
- Frontend build: `cd frontend && npm run build`
