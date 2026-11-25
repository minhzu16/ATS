package fptu.sba301.ats.config;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.CandidateNote;
import fptu.sba301.ats.entity.CandidateStageHistory;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Seeds applications, candidate stage history, and candidate notes.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
class ApplicationPipelineSeedHelper {

    private final EntityManager entityManager;
    private final SeedQueryHelper seedQueryHelper;

    List<Application> seedApplications(List<Job> jobs, Map<String, Candidate> candidatesByEmail) {
        if (!jobs.isEmpty()) {
            log.info("Ensuring Applications seed data...");
            Job frontendJob = jobs.stream()
                    .filter(job -> "Senior Frontend Developer".equalsIgnoreCase(job.getTitle()))
                    .findFirst()
                    .orElse(jobs.get(0));

            Job backendJob = jobs.stream()
                    .filter(job -> "Backend Java Developer".equalsIgnoreCase(job.getTitle()))
                    .findFirst()
                    .orElse(jobs.get(0));

            ensureApplication(candidatesByEmail.get("sarah.j@example.com"), frontendJob, ApplicationStage.SCREENING, ApplicationStatus.ACTIVE, LocalDateTime.now().minusHours(2));
            ensureApplication(candidatesByEmail.get("alice.lee@example.com"), frontendJob, ApplicationStage.INTERVIEW, ApplicationStatus.ACTIVE, LocalDateTime.now().minusDays(5));
            ensureApplication(candidatesByEmail.get("bob.nguyen@example.com"), backendJob, ApplicationStage.APPLIED, ApplicationStatus.ACTIVE, LocalDateTime.now().minusHours(12));
            ensureApplication(candidatesByEmail.get("clara.tran@example.com"), backendJob, ApplicationStage.OFFER, ApplicationStatus.ACTIVE, LocalDateTime.now().minusDays(2));
            ensureApplication(candidatesByEmail.get("david.pham@example.com"), frontendJob, ApplicationStage.HIRED, ApplicationStatus.ACTIVE, LocalDateTime.now().minusDays(7));
            ensureApplication(candidatesByEmail.get("emma.ho@example.com"), backendJob, ApplicationStage.REJECTED, ApplicationStatus.ACTIVE, LocalDateTime.now().minusDays(10));

            entityManager.flush();
            log.info("Applications seed data ensured.");
        }

        return seedQueryHelper.findAll(Application.class);
    }

    void seedCandidateStageHistory(List<Application> applications) {
        if (seedQueryHelper.isEntityEmpty(CandidateStageHistory.class) && !applications.isEmpty()) {
            log.info("Seeding Candidate Stage History...");
            entityManager.persist(CandidateStageHistory.builder()
                    .application(applications.get(0))
                    .fromStage(ApplicationStage.APPLIED)
                    .toStage(ApplicationStage.SCREENING)
                    .build());

            if (applications.size() > 1) {
                entityManager.persist(CandidateStageHistory.builder()
                        .application(applications.get(1))
                        .fromStage(ApplicationStage.SCREENING)
                        .toStage(ApplicationStage.INTERVIEW)
                        .build());
            }
            entityManager.flush();
            log.info("Candidate Stage History seeded.");
        }
    }

    void seedCandidateNotes(List<Application> applications) {
        if (seedQueryHelper.isEntityEmpty(CandidateNote.class) && !applications.isEmpty()) {
            log.info("Seeding Candidate Notes...");
            entityManager.persist(CandidateNote.builder()
                    .application(applications.get(0))
                    .content("Strong communication and React fundamentals")
                    .build());

            if (applications.size() > 1) {
                entityManager.persist(CandidateNote.builder()
                        .application(applications.get(1))
                        .content("Good problem-solving, proceed to technical interview")
                        .build());
            }
            entityManager.flush();
            log.info("Candidate Notes seeded.");
        }
    }

    private Application ensureApplication(
            Candidate candidate,
            Job job,
            ApplicationStage stage,
            ApplicationStatus status,
            LocalDateTime appliedAt
    ) {
        List<Application> existing = entityManager.createQuery(
                        "select a from Application a where a.candidate.id = :candidateId and a.job.id = :jobId",
                        Application.class
                )
                .setParameter("candidateId", candidate.getId())
                .setParameter("jobId", job.getId())
                .getResultList();

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .stage(stage)
                .status(status)
                .appliedAt(appliedAt)
                .build();

        entityManager.persist(application);
        return application;
    }
}
