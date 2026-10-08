# Applicant Tracking System (ATS)

An enterprise-ready, full-stack Applicant Tracking System built with **Spring Boot 3.5 (Java 21)**, **MySQL 8.0**, and **React 19 (TypeScript, Vite 7, TailwindCSS)**.

---

## 🚀 Key Features & Architecture

- **Role-Based Access Control (RBAC):** `SYSTEM_ADMIN`, `HR_MANAGER`, `HR` (Recruiter), `INTERVIEWER`.
- **Pipeline & Stage Machine:** `APPLIED -> SCREENING -> INTERVIEW -> OFFER -> HIRED / REJECTED / WITHDRAWN` with mandatory rejection reasons and withdrawal guards.
- **Fair & Blind Interview Scoring:**
  - Blind evaluation prevents interviewer anchoring bias before submissions are in.
  - Multi-interviewer completion checks: interviews only transition to `COMPLETED` once all interviewers have submitted.
  - Strict scorecard template validation preventing criterion injection.
  - Principal-based score submission & deletion (fixing IDOR).
- **Full Offer Lifecycle with Separation of Duties:**
  - Workflow: `DRAFT -> PENDING_APPROVAL -> APPROVED -> SENT -> ACCEPTED / DECLINED / EXPIRED`.
  - Separation of duties: Offer creators cannot approve or reject their own offers.
  - Optimistic locking (`@Version`) prevents concurrent approval race conditions.
  - Real onboarding: Candidates only transition to `HIRED` once the offer is accepted.
- **Candidate Notes & Documents:**
  - Multi-application support per candidate with unique email constraint.
  - Magic-byte file validation (`%PDF-`, `PK\x03\x04`, JPEG, PNG, WEBP) and size limits.
  - Internal candidate notes API with author attribution and role guards.
- **Enterprise Security & Audit Trail:**
  - JPA Auditing (`AuditorAware<UUID>`, `@CreatedBy`, `@LastModifiedBy`) fixing "System" audit records.
  - Comprehensive `@LogAudit` tracking on job, offer, stage, and export actions.
  - Proxy-aware IP resolution (`X-Forwarded-For`) and automatic redaction of sensitive credentials.
  - Token reuse detection for refresh tokens: presenting a revoked token immediately invalidates the entire user token chain.
  - CSV Formula Injection sanitization (`=`, `+`, `-`, `@`, tabs).
- **Reliable Operations & Migration:**
  - Version-controlled schema migrations with **Flyway** (`V1__baseline.sql`).
  - Production-ready `docker-compose.yml` orchestrating MySQL 8.0, Backend, and Frontend.

---

## 🛠 Tech Stack

- **Backend:** Spring Boot 3.5, Java 21, Spring Security, Spring Data JPA, Flyway, JJWT, Hibernate Validator, OpenPDF, Apache POI.
- **Frontend:** React 19, TypeScript 5, Vite 7, TailwindCSS, Radix UI / shadcn/ui, Axios, Lucide Icons, Sonner.
- **Database:** MySQL 8.0 with InnoDB and utf8mb4.

---

## 📦 Getting Started

### 1. Prerequisites
- Docker & Docker Compose **or**:
- Java 21 JDK + Maven 3.9+
- Node.js 20+ & npm

### 2. Environment Setup
Copy the environment template in `backend/`:
```bash
cp backend/.env.example backend/.env
```
Ensure you set a secure 64+ byte secret for `JWT_SECRET_KEY`:
```properties
JWT_SECRET_KEY=c3VwZXItc2VjdXJlLWp3dC1zZWNyZXQta2V5LXNob3VsZC1iZS1hdC1sZWFzdC02NC1ieXRlcy1sb25nLWZvci1oczUxMi1hbGdvcml0aG0=
```

### 3. Run with Docker Compose (Recommended)
```bash
docker compose up -d --build
```
- Frontend: http://localhost:5173
- Backend API: http://localhost:8386/api/v1
- MySQL: localhost:3306

### 4. Run Locally (Development Mode)

#### Start MySQL:
```bash
docker run -d --name ats-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=rootpassword -e MYSQL_DATABASE=applicant_tracking mysql:8.0
```

#### Run Backend:
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Run Frontend:
```bash
cd frontend
npm install
npm run dev
```

---

## 👥 Demo Accounts (Active in `dev` Profile)

| Email | Role | Default Password |
|---|---|---|
| `admin@ats.com` | `SYSTEM_ADMIN` | `Password123!` |
| `hrmanager@ats.com` | `HR_MANAGER` | `Password123!` |
| `hr@ats.com` | `HR` (Recruiter) | `Password123!` |
| `interviewer@ats.com` | `INTERVIEWER` | `Password123!` |

> **Security Note:** In production (`SPRING_PROFILES_ACTIVE=prod`), automatic seeding is disabled. Admin accounts must be initialized using `BOOTSTRAP_ADMIN_EMAIL` and `BOOTSTRAP_ADMIN_PASSWORD` environment variables.

---

## 🧪 Testing

### Backend Unit & Security Tests:
```bash
cd backend
mvn test
```
Includes dedicated security test suites:
- `FileValidatorTest`: Magic bytes & extension validation
- `StageTransitionStateMachineTest`: Pipeline transition matrix & rejection reasons
- `OfferWorkflowSecurityTest`: Separation of duties & optimistic locking
- `InterviewEvaluationSecurityTest`: IDOR prevention & multi-interviewer completion
- `RefreshTokenSecurityTest`: Token reuse detection & revocation

### Frontend Type-Check & Build:
```bash
cd frontend
npm run build
```
Builds cleanly with Vite and TypeScript strict mode enabled.
