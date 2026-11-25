# ADR-0002: Application Stage State Machine and Role-Based Access Control

## Status
Accepted

## Context
Recruitment pipelines involve multiple handoffs between `HR`, `HR_MANAGER`, `INTERVIEWER`, and `SYSTEM_ADMIN`. Allowing arbitrary stage jumps or unapproved job/offer transitions would corrupt hiring metrics and bypass governance controls.

## Decision
1. **Explicit Stage Transitions**: `ApplicationStage` progression (`APPLIED -> SCREENING -> INTERVIEW -> OFFER -> HIRED` and terminal `REJECTED`) is validated in service-layer transition logic rather than trusting raw client updates.
2. **Approval Workflows**: `JobCommandServiceImpl` requires `HR` to create requisitions in `PENDING_APPROVAL` and restricts approval to `HR_MANAGER`. `OfferServiceImpl` similarly enforces `DRAFT -> PENDING_APPROVAL -> APPROVED` before candidate delivery.
3. **Method-Level RBAC**: `@EnableMethodSecurity` in `SecurityConfig` pairs with `@PreAuthorize` annotations and service-level ownership checks (e.g., `CandidateNoteServiceImpl` and `InterviewEvaluationSecurityTest`).

## Consequences
- Invalid stage transitions fail fast with `400 Bad Request` / `409 Conflict` via `BusinessException`.
- Departmental and role boundaries are enforced both at the HTTP layer and inside service methods.
