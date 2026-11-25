package fptu.sba301.ats.config;

import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.CandidateDocument;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.JobApproval;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApprovalStatus;
import fptu.sba301.ats.enums.JobStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds jobs, job approvals, candidates, and candidate documents.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
class RecruitmentSeedHelper {

    private final EntityManager entityManager;
    private final SeedQueryHelper seedQueryHelper;

    List<Job> seedJobs(Department itDept, User hrManager) {
        if (seedQueryHelper.isEntityEmpty(Job.class)) {
            log.info("Seeding Jobs...");
            entityManager.persist(Job.builder()
                    .title("Senior Frontend Developer")
                    .description("Build and maintain ATS frontend with React and TypeScript")
                    .department(itDept)
                    .hiringManager(hrManager)
                    .status(JobStatus.APPROVED)
                    .headcount(2)
                    .build());

            entityManager.persist(Job.builder()
                    .title("Backend Java Developer")
                    .description("Develop Spring Boot APIs and integrations")
                    .department(itDept)
                    .hiringManager(hrManager)
                    .status(JobStatus.APPROVED)
                    .headcount(1)
                    .build());
            entityManager.flush();
            log.info("Jobs seeded.");
        }

        return seedQueryHelper.findAll(Job.class);
    }

    void seedJobApprovals(List<Job> jobs, User admin) {
        if (seedQueryHelper.isEntityEmpty(JobApproval.class) && !jobs.isEmpty()) {
            log.info("Seeding Job Approvals...");
            entityManager.persist(JobApproval.builder()
                    .job(jobs.get(0))
                    .approvedBy(admin)
                    .status(ApprovalStatus.APPROVED)
                    .comment("Approved for urgent hiring")
                    .build());
            if (jobs.size() > 1) {
                entityManager.persist(JobApproval.builder()
                        .job(jobs.get(1))
                        .approvedBy(admin)
                        .status(ApprovalStatus.APPROVED)
                        .comment("Approved for Q2 hiring plan")
                        .build());
            }
            entityManager.flush();
            log.info("Job Approvals seeded.");
        }
    }

    Map<String, Candidate> seedCandidates() {
        log.info("Ensuring Candidates seed data...");
        Map<String, Candidate> candidatesByEmail = new LinkedHashMap<>();

        candidatesByEmail.put("sarah.j@example.com", ensureCandidate(
                "sarah.j@example.com",
                "Sarah Jenkins",
                "+84-912345678",
                "TechNova",
                "LinkedIn",
                "Ho Chi Minh City",
                5,
                "Frontend engineer focused on React and UX"
        ));
        candidatesByEmail.put("alice.lee@example.com", ensureCandidate(
                "alice.lee@example.com",
                "Alice Lee",
                "+84-987654321",
                "CloudSoft",
                "Referral",
                "Da Nang",
                4,
                "Fullstack developer with Java and React background"
        ));
        candidatesByEmail.put("bob.nguyen@example.com", ensureCandidate(
                "bob.nguyen@example.com",
                "Bob Nguyen",
                "+84-938112233",
                "DataScale",
                "LinkedIn",
                "Ha Noi",
                3,
                "Backend-focused Java engineer"
        ));
        candidatesByEmail.put("clara.tran@example.com", ensureCandidate(
                "clara.tran@example.com",
                "Clara Tran",
                "+84-901998877",
                "Nexa Labs",
                "Career Site",
                "Ho Chi Minh City",
                6,
                "Senior fullstack engineer with team-leading experience"
        ));
        candidatesByEmail.put("david.pham@example.com", ensureCandidate(
                "david.pham@example.com",
                "David Pham",
                "+84-909556677",
                "ByteWorks",
                "Referral",
                "Can Tho",
                7,
                "Strong architecture and delivery track record"
        ));
        candidatesByEmail.put("emma.ho@example.com", ensureCandidate(
                "emma.ho@example.com",
                "Emma Ho",
                "+84-945667788",
                "Skyline Tech",
                "LinkedIn",
                "Da Nang",
                2,
                "Junior candidate with strong growth potential"
        ));

        entityManager.flush();
        log.info("Candidates seed data ensured.");
        return candidatesByEmail;
    }

    void seedCandidateDocuments() {
        List<Candidate> candidates = seedQueryHelper.findAll(Candidate.class);

        if (seedQueryHelper.isEntityEmpty(CandidateDocument.class) && !candidates.isEmpty()) {
            log.info("Seeding Candidate Documents...");
            entityManager.persist(CandidateDocument.builder()
                    .candidate(candidates.get(0))
                    .fileName("sarah_jenkins_cv.pdf")
                    .fileUrl("https://example.com/docs/sarah_jenkins_cv.pdf")
                    .fileType("application/pdf")
                    .fileSizeBytes(245_000L)
                    .uploadedAt(LocalDateTime.now().minusDays(10))
                    .build());

            if (candidates.size() > 1) {
                entityManager.persist(CandidateDocument.builder()
                        .candidate(candidates.get(1))
                        .fileName("alice_lee_cv.pdf")
                        .fileUrl("https://example.com/docs/alice_lee_cv.pdf")
                        .fileType("application/pdf")
                        .fileSizeBytes(210_000L)
                        .uploadedAt(LocalDateTime.now().minusDays(8))
                        .build());
            }
            entityManager.flush();
            log.info("Candidate Documents seeded.");
        }
    }

    private Candidate ensureCandidate(
            String email,
            String fullName,
            String phone,
            String currentCompany,
            String source,
            String location,
            Integer experienceYears,
            String summary
    ) {
        List<Candidate> existing = entityManager.createQuery(
                        "select c from Candidate c where c.email = :email",
                        Candidate.class
                )
                .setParameter("email", email)
                .getResultList();

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        Candidate candidate = Candidate.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .currentCompany(currentCompany)
                .source(source)
                .location(location)
                .experienceYears(experienceYears)
                .summary(summary)
                .build();

        entityManager.persist(candidate);
        return candidate;
    }
}
