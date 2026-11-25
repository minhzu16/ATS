# Domain Glossary

Canonical terms used across the Applicant Tracking System (`fptu.sba301.ats`).

## Core Domain Entities

- **User (`User`, `Role`)**: An authenticated system actor (`SYSTEM_ADMIN`, `HR_MANAGER`, `HR`, `INTERVIEWER`) belonging to a `Department`. Accounts support soft-deletion (`deleted`) and administrative locking (`accountLocked`).
- **Department (`Department`)**: Organizational unit owning `Job` requisitions and `ScorecardTemplate` definitions.
- **Job (`Job`, `JobStatus`)**: A hiring requisition created by `HR` (`PENDING_APPROVAL`) and reviewed by `HR_MANAGER` (`APPROVED`, `REJECTED`, or `CLOSED`).
- **Candidate (`Candidate`)**: An external applicant profile with contact details, CV/document attachments, and experience metadata.
- **Application (`Application`, `ApplicationStage`, `ApplicationStatus`)**: The link between a `Candidate` and a `Job`. Tracks progression through `ApplicationStage` (`APPLIED`, `SCREENING`, `INTERVIEW`, `OFFER`, `HIRED`, `REJECTED`) and `ApplicationStatus` (`ACTIVE`, `WITHDRAWN`, `REJECTED`, `HIRED`).
- **Interview (`Interview`, `InterviewParticipant`, `InterviewStatus`, `InterviewType`)**: A scheduled `ONLINE` or `OFFLINE` evaluation session for an `Application`, with assigned interviewers and `InterviewScore` entries.
- **Scorecard (`ScorecardTemplate`, `ScorecardCriterion`)**: Reusable department-scoped evaluation rubric with weighted criteria used during interviews.
- **Offer (`Offer`, `OfferStatus`)**: Compensation and start-date proposal for a candidate in the `OFFER` stage (`DRAFT` -> `PENDING_APPROVAL` -> `APPROVED` -> `SENT` -> `ACCEPTED` / `DECLINED`).
- **Onboarding (`OnboardingChecklist`, `OnboardingTask`, `OnboardingStatus`)**: Post-hire task list assigned to a hired candidate's `Application`.
- **Audit Log (`AuditLog`, `@LogAudit`)**: Immutable record of privileged state changes captured via `AuditAspect`.
- **Refresh Token (`RefreshToken`)**: Database-persisted token enabling stateless JWT access token renewal and explicit revocation on logout.
