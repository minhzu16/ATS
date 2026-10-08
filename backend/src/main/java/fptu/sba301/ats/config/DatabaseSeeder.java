package fptu.sba301.ats.config;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.CandidateDocument;
import fptu.sba301.ats.entity.CandidateNote;
import fptu.sba301.ats.entity.CandidateStageHistory;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.InterviewScore;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.JobApproval;
import fptu.sba301.ats.entity.Offer;
import fptu.sba301.ats.entity.OfferApproval;
import fptu.sba301.ats.entity.RefreshToken;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.entity.ScorecardTemplate;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.ApprovalStatus;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.enums.InterviewType;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.enums.OfferStatus;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.repository.DepartmentRepository;
import fptu.sba301.ats.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Database Seeder...");

        seedDepartments();
        seedUsers();
        seedDomainData();

        log.info("Database Seeder finished.");
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) {
            return;
        }

        log.info("Seeding Departments...");
        departmentRepository.save(Department.builder()
                .name("IT")
                .description("Information Technology Department")
                .build());

        departmentRepository.save(Department.builder()
                .name("HR")
                .description("Human Resources Department")
                .build());

        departmentRepository.save(Department.builder()
                .name("Sales")
                .description("Sales and Marketing Department")
                .build());

        log.info("Departments seeded.");
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Seeding Users...");
        Department itDept = getDepartmentByName("IT");
        Department hrDept = getDepartmentByName("HR");
        String adminEmail = Optional.ofNullable(System.getenv("BOOTSTRAP_ADMIN_EMAIL")).filter(s -> !s.isBlank()).orElse("admin@ats.com");
        String devPassword = Optional.ofNullable(System.getenv("BOOTSTRAP_ADMIN_PASSWORD")).filter(s -> !s.isBlank()).orElse("Password123!");
        String encodedPassword = passwordEncoder.encode(devPassword);

        userRepository.save(User.builder()
                .email(adminEmail)
                .passwordHash(encodedPassword)
                .fullName("System Administrator")
                .role(Role.SYSTEM_ADMIN)
                .department(itDept)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("hrmanager@ats.com")
                .passwordHash(encodedPassword)
                .fullName("HR Manager")
                .role(Role.HR_MANAGER)
                .department(hrDept)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("hr@ats.com")
                .passwordHash(encodedPassword)
                .fullName("Human Resources Staff")
                .role(Role.HR)
                .department(itDept)
                .active(true)
                .build());

        userRepository.save(User.builder()
                .email("interviewer@ats.com")
                .passwordHash(encodedPassword)
                .fullName("Technical Interviewer")
                .role(Role.INTERVIEWER)
                .department(itDept)
                .active(true)
                .build());

        log.info("Users seeded.");
    }

    private void seedDomainData() {
        Department itDept = getDepartmentByName("IT");
        Department hrDept = getDepartmentByName("HR");
        User admin = getUserByEmail("admin@ats.com");
        User hrManager = getUserByEmail("hrmanager@ats.com");
        User hrUser = getUserByEmail("hr@ats.com");
        User interviewer = getUserByEmail("interviewer@ats.com");

        if (isEntityEmpty(Job.class)) {
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

        List<Job> jobs = findAll(Job.class);

        if (isEntityEmpty(JobApproval.class) && !jobs.isEmpty()) {
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

        log.info("Ensuring Candidates seed data...");
        Candidate sarah = ensureCandidate(
                "sarah.j@example.com",
                "Sarah Jenkins",
                "+84-912345678",
                "TechNova",
                "LinkedIn",
                "Ho Chi Minh City",
                5,
                "Frontend engineer focused on React and UX"
        );
        Candidate alice = ensureCandidate(
                "alice.lee@example.com",
                "Alice Lee",
                "+84-987654321",
                "CloudSoft",
                "Referral",
                "Da Nang",
                4,
                "Fullstack developer with Java and React background"
        );
        Candidate bob = ensureCandidate(
                "bob.nguyen@example.com",
                "Bob Nguyen",
                "+84-938112233",
                "DataScale",
                "LinkedIn",
                "Ha Noi",
                3,
                "Backend-focused Java engineer"
        );
        Candidate clara = ensureCandidate(
                "clara.tran@example.com",
                "Clara Tran",
                "+84-901998877",
                "Nexa Labs",
                "Career Site",
                "Ho Chi Minh City",
                6,
                "Senior fullstack engineer with team-leading experience"
        );
        Candidate david = ensureCandidate(
                "david.pham@example.com",
                "David Pham",
                "+84-909556677",
                "ByteWorks",
                "Referral",
                "Can Tho",
                7,
                "Strong architecture and delivery track record"
        );
        Candidate emma = ensureCandidate(
                "emma.ho@example.com",
                "Emma Ho",
                "+84-945667788",
                "Skyline Tech",
                "LinkedIn",
                "Da Nang",
                2,
                "Junior candidate with strong growth potential"
        );
        entityManager.flush();
        log.info("Candidates seed data ensured.");

        List<Candidate> candidates = findAll(Candidate.class);

        if (isEntityEmpty(CandidateDocument.class) && !candidates.isEmpty()) {
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

            ensureApplication(sarah, frontendJob, ApplicationStage.SCREENING, ApplicationStatus.ACTIVE, LocalDateTime.now().minusHours(2));
            ensureApplication(alice, frontendJob, ApplicationStage.INTERVIEW, ApplicationStatus.ACTIVE, LocalDateTime.now().minusDays(5));
            ensureApplication(bob, backendJob, ApplicationStage.APPLIED, ApplicationStatus.ACTIVE, LocalDateTime.now().minusHours(12));
            ensureApplication(clara, backendJob, ApplicationStage.OFFER, ApplicationStatus.ACTIVE, LocalDateTime.now().minusDays(2));
            ensureApplication(david, frontendJob, ApplicationStage.HIRED, ApplicationStatus.ACTIVE, LocalDateTime.now().minusDays(7));
            ensureApplication(emma, backendJob, ApplicationStage.REJECTED, ApplicationStatus.ACTIVE, LocalDateTime.now().minusDays(10));

            entityManager.flush();
            log.info("Applications seed data ensured.");
        }

        List<Application> applications = findAll(Application.class);

        if (isEntityEmpty(CandidateStageHistory.class) && !applications.isEmpty()) {
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

        if (isEntityEmpty(CandidateNote.class) && !applications.isEmpty()) {
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

        if (isEntityEmpty(ScorecardTemplate.class)) {
            log.info("Seeding Scorecard Templates...");
            entityManager.persist(ScorecardTemplate.builder()
                    .name("General Engineering Interview")
                    .department(itDept)
                    .build());
            entityManager.flush();
            log.info("Scorecard Templates seeded.");
        }

        List<ScorecardTemplate> templates = findAll(ScorecardTemplate.class);
        ScorecardTemplate defaultTemplate = templates.isEmpty() ? null : templates.get(0);

        if (isEntityEmpty(Interview.class) && !applications.isEmpty() && defaultTemplate != null) {
            log.info("Seeding Interviews...");
            entityManager.persist(Interview.builder()
                    .application(applications.get(0))
                    .template(defaultTemplate)
                    .scheduledAt(LocalDateTime.now().plusDays(1))
                    .location("Google Meet")
                    .type(InterviewType.ONLINE)
                    .status(InterviewStatus.SCHEDULED)
                    .build());

            if (applications.size() > 1) {
                entityManager.persist(Interview.builder()
                        .application(applications.get(1))
                        .template(defaultTemplate)
                        .scheduledAt(LocalDateTime.now().minusDays(1))
                        .location("FPT Tower - Meeting Room 5")
                        .type(InterviewType.OFFLINE)
                        .status(InterviewStatus.COMPLETED)
                        .build());
            }
            entityManager.flush();
            log.info("Interviews seeded.");
        }

        if (isEntityEmpty(Interview.class) && defaultTemplate == null) {
            log.warn("Skip seeding Interviews because no ScorecardTemplate is available.");
        }

        List<Interview> interviews = findAll(Interview.class);

        if (isEntityEmpty(InterviewParticipant.class) && !interviews.isEmpty() && interviewer != null) {
            log.info("Seeding Interview Participants...");
            for (Interview interview : interviews) {
                entityManager.persist(InterviewParticipant.builder()
                        .id(new InterviewParticipant.InterviewParticipantId(interview.getId(), interviewer.getId()))
                        .interview(interview)
                        .user(interviewer)
                        .role(ParticipantRole.INTERVIEWER)
                        .build());
                if (hrManager != null) {
                    entityManager.persist(InterviewParticipant.builder()
                            .id(new InterviewParticipant.InterviewParticipantId(interview.getId(), hrManager.getId()))
                            .interview(interview)
                            .user(hrManager)
                            .role(ParticipantRole.OBSERVER)
                            .build());
                }
            }
            entityManager.flush();
            log.info("Interview Participants seeded.");
        }

        if (isEntityEmpty(ScorecardCriterion.class) && !templates.isEmpty()) {
            log.info("Seeding Scorecard Criteria...");
            ScorecardTemplate template = templates.get(0);
            entityManager.persist(ScorecardCriterion.builder()
                    .template(template)
                    .name("Technical")
                    .weight(new BigDecimal("0.60"))
                    .build());
            entityManager.persist(ScorecardCriterion.builder()
                    .template(template)
                    .name("Communication")
                    .weight(new BigDecimal("0.40"))
                    .build());
            entityManager.flush();
            log.info("Scorecard Criteria seeded.");
        }

        List<ScorecardCriterion> criteria = findAll(ScorecardCriterion.class);

        if (isEntityEmpty(InterviewScore.class) && !interviews.isEmpty() && !criteria.isEmpty() && interviewer != null) {
            log.info("Seeding Interview Scores...");
            for (Interview interview : interviews) {
                for (ScorecardCriterion criterion : criteria) {
                    entityManager.persist(InterviewScore.builder()
                            .interview(interview)
                            .interviewer(interviewer)
                            .criterion(criterion)
                            .score(4)
                            .comment("Good overall performance")
                            .build());
                }
            }
            entityManager.flush();
            log.info("Interview Scores seeded.");
        }

        Application offerStageApplication = null;
        for (Application a : applications) {
            if (a.getStage() == ApplicationStage.OFFER) {
                offerStageApplication = a;
                break;
            }
        }

        if (isEntityEmpty(Offer.class) && offerStageApplication != null) {
            log.info("Seeding Offers...");
            entityManager.persist(Offer.builder()
                    .application(offerStageApplication)
                    .salary(new BigDecimal("2500.00"))
                    .positionTitle("Senior Frontend Developer")
                    .status(OfferStatus.PENDING_APPROVAL)
                    .build());
            entityManager.flush();
            log.info("Offers seeded.");
        }

        List<Offer> offers = findAll(Offer.class);

        if (isEntityEmpty(OfferApproval.class) && !offers.isEmpty() && admin != null) {
            Offer pendingOffer = null;
            for (Offer o : offers) {
                if (o.getStatus() == OfferStatus.PENDING_APPROVAL) {
                    pendingOffer = o;
                    break;
                }
            }
            if (pendingOffer != null) {
                log.info("Seeding Offer Approvals...");
                entityManager.persist(OfferApproval.builder()
                        .offer(pendingOffer)
                        .approvedBy(admin)
                        .status(ApprovalStatus.APPROVED)
                        .comment("Compensation approved by System Admin")
                        .build());
                pendingOffer.setStatus(OfferStatus.APPROVED);
                Application approvedApp = pendingOffer.getApplication();
                if (approvedApp != null) {
                    approvedApp.setStage(ApplicationStage.HIRED);
                    entityManager.merge(approvedApp);
                }
                entityManager.merge(pendingOffer);
                entityManager.flush();
                log.info("Offer Approvals seeded.");
            }
        }

        if (isEntityEmpty(RefreshToken.class) && hrUser != null) {
            log.info("Seeding Refresh Tokens...");
            entityManager.persist(RefreshToken.builder()
                    .token(UUID.randomUUID() + ".seed-token")
                    .user(hrUser)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .build());
            entityManager.flush();
            log.info("Refresh Tokens seeded.");
        }
    }

    private Department getDepartmentByName(String name) {
        return departmentRepository.findAll().stream()
                .filter(department -> department.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private User getUserByEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmailAndDeletedFalse(email);
        return userOptional.orElse(null);
    }

    private <T> boolean isEntityEmpty(Class<T> entityClass) {
        try {
            Long count = entityManager.createQuery(
                    "select count(e) from " + entityClass.getSimpleName() + " e",
                    Long.class
            ).getSingleResult();
            return count == 0;
        } catch (Exception ex) {
            log.warn("Skip seeding {} because table/entity is not ready: {}", entityClass.getSimpleName(), ex.getMessage());
            return false;
        }
    }

    private <T> List<T> findAll(Class<T> entityClass) {
        return entityManager.createQuery(
                "select e from " + entityClass.getSimpleName() + " e",
                entityClass
        ).getResultList();
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
