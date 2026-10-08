package fptu.sba301.ats.service;

import fptu.sba301.ats.entity.*;
import fptu.sba301.ats.enums.*;
import fptu.sba301.ats.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeederService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final ScorecardTemplateRepository scorecardTemplateRepository;
    private final ScorecardCriterionRepository scorecardCriterionRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final InterviewScoreRepository interviewScoreRepository;
    private final OfferRepository offerRepository;
    private final OnboardingChecklistRepository onboardingChecklistRepository;
    private final OnboardingTaskRepository onboardingTaskRepository;
    private final SystemConfigRepository systemConfigRepository;

    @Transactional
    public String seedDemoData() {
        try {
            // Seed system configs if empty
            if (systemConfigRepository.count() == 0) {
                SystemConfig config = new SystemConfig();
                config.setKey("NOTIFICATIONS_ENABLED");
                config.setValue("true");
                config.setUpdatedAt(java.time.Instant.now());
                systemConfigRepository.save(config);

                SystemConfig sessionConfig = new SystemConfig();
                sessionConfig.setKey("SESSION_TIMEOUT_MINUTES");
                sessionConfig.setValue("30");
                sessionConfig.setUpdatedAt(java.time.Instant.now());
                systemConfigRepository.save(sessionConfig);
            }
            // Check if data already exists to avoid duplication
            if (jobRepository.count() > 0) {
                return "Demo data already exists.";
            }

            // 1. Get HR user (assumed to be seeded by admin seeder)
            // 1. Get HR and Admin users
            User hr = userRepository.findByEmailAndDeletedFalse("hr@ats.com")
                    .orElseThrow(() -> new RuntimeException("HR user not found. Please run /api/v1/seed-admin first."));
            User hrManager = userRepository.findByEmailAndDeletedFalse("hr.manager@ats.com").orElse(hr);
            User admin = userRepository.findByEmailAndDeletedFalse("admin@ats.com").orElse(hr);

            // 2. Department
            Department itDept = Department.builder()
                .id(java.util.UUID.randomUUID())
                .name("Information Technology")
                .description("IT and Software Engineering")
                .build();
            itDept = departmentRepository.save(itDept);

            Department marketingDept = new Department();
            marketingDept.setName("Marketing");
            marketingDept.setCreatedBy(hr.getId());
            marketingDept = departmentRepository.save(marketingDept);

            // ====================== 1. SCORECARD TEMPLATES ======================
            // Template 1: Frontend Developer
            ScorecardTemplate template1 = new ScorecardTemplate();
            template1.setName("Frontend Developer Evaluation");
            template1.setDepartment(itDept);
            template1.setCreatedBy(hr.getId());
            template1 = scorecardTemplateRepository.save(template1);

            List<ScorecardCriterion> criteria1 = new ArrayList<>();
            ScorecardCriterion c1_1 = new ScorecardCriterion();
            c1_1.setTemplate(template1);
            c1_1.setName("React JS & TypeScript Knowledge");
            c1_1.setWeight(new BigDecimal("5.00"));
            c1_1.setCreatedBy(hr.getId());
            criteria1.add(c1_1);

            ScorecardCriterion c1_2 = new ScorecardCriterion();
            c1_2.setTemplate(template1);
            c1_2.setName("CSS/UX Understanding");
            c1_2.setWeight(new BigDecimal("3.00"));
            c1_2.setCreatedBy(hr.getId());
            criteria1.add(c1_2);

            ScorecardCriterion c1_3 = new ScorecardCriterion();
            c1_3.setTemplate(template1);
            c1_3.setName("Communication Skills");
            c1_3.setWeight(new BigDecimal("2.00"));
            c1_3.setCreatedBy(hr.getId());
            criteria1.add(c1_3);

            scorecardCriterionRepository.saveAll(criteria1);

            // Template 2: Backend Developer
            ScorecardTemplate template2 = new ScorecardTemplate();
            template2.setName("Backend Developer Evaluation");
            template2.setDepartment(itDept);
            template2.setCreatedBy(hr.getId());
            template2 = scorecardTemplateRepository.save(template2);

            List<ScorecardCriterion> criteria2 = new ArrayList<>();
            ScorecardCriterion c2_1 = new ScorecardCriterion();
            c2_1.setTemplate(template2);
            c2_1.setName("Java/Spring Boot Expertise");
            c2_1.setWeight(new BigDecimal("5.00"));
            c2_1.setCreatedBy(hr.getId());
            criteria2.add(c2_1);

            ScorecardCriterion c2_2 = new ScorecardCriterion();
            c2_2.setTemplate(template2);
            c2_2.setName("Database Design (SQL/NoSQL)");
            c2_2.setWeight(new BigDecimal("4.00"));
            c2_2.setCreatedBy(hr.getId());
            criteria2.add(c2_2);

            ScorecardCriterion c2_3 = new ScorecardCriterion();
            c2_3.setTemplate(template2);
            c2_3.setName("API Design & RESTful");
            c2_3.setWeight(new BigDecimal("3.00"));
            c2_3.setCreatedBy(hr.getId());
            criteria2.add(c2_3);

            scorecardCriterionRepository.saveAll(criteria2);

            // Template 3: Marketing Manager
            ScorecardTemplate template3 = new ScorecardTemplate();
            template3.setName("Marketing Manager Evaluation");
            template3.setDepartment(marketingDept);
            template3.setCreatedBy(hr.getId());
            template3 = scorecardTemplateRepository.save(template3);

            List<ScorecardCriterion> criteria3 = new ArrayList<>();
            ScorecardCriterion c3_1 = new ScorecardCriterion();
            c3_1.setTemplate(template3);
            c3_1.setName("Digital Marketing Strategy");
            c3_1.setWeight(new BigDecimal("5.00"));
            c3_1.setCreatedBy(hr.getId());
            criteria3.add(c3_1);

            ScorecardCriterion c3_2 = new ScorecardCriterion();
            c3_2.setTemplate(template3);
            c3_2.setName("Data Analytics");
            c3_2.setWeight(new BigDecimal("4.00"));
            c3_2.setCreatedBy(hr.getId());
            criteria3.add(c3_2);

            scorecardCriterionRepository.saveAll(criteria3);

            // ====================== 2. JOBS ======================
            Job frontendJob = new Job();
            frontendJob.setTitle("Senior Frontend Developer");
            frontendJob.setDescription("Looking for an experienced React developer to join our team.");
            frontendJob.setDepartment(itDept);
            frontendJob.setHiringManager(hr);
            frontendJob.setStatus(JobStatus.APPROVED);
            frontendJob.setHeadcount(2);
            frontendJob.setCreatedBy(hr.getId());
            frontendJob = jobRepository.save(frontendJob);

            Job backendJob = new Job();
            backendJob.setTitle("Java Backend Developer");
            backendJob.setDescription("Seeking a Java Spring Boot expert.");
            backendJob.setDepartment(itDept);
            backendJob.setHiringManager(hr);
            backendJob.setStatus(JobStatus.APPROVED);
            backendJob.setHeadcount(1);
            backendJob.setCreatedBy(hr.getId());
            backendJob = jobRepository.save(backendJob);

            // ====================== 3. CANDIDATES & APPLICATIONS (For Ranking/Evaluation) ======================
            // Candidate 1: High score
            Candidate candidate1 = new Candidate();
            candidate1.setFullName("Nguyen Van A");
            candidate1.setEmail("nguyenvana@example.com");
            candidate1.setPhone("+84987654321");
            candidate1.setCurrentCompany("Tech Corp");
            candidate1.setSource("LinkedIn");
            candidate1.setLocation("Hanoi, Vietnam");
            candidate1.setExperienceYears(5);
            candidate1.setSummary("Experienced Frontend Developer.");
            candidate1.setCreatedBy(hr.getId());
            candidate1 = candidateRepository.save(candidate1);

            Application app1 = new Application();
            app1.setCandidate(candidate1);
            app1.setJob(frontendJob);
            app1.setStage(ApplicationStage.INTERVIEW);
            app1.setStatus(ApplicationStatus.ACTIVE);
            app1.setCreatedBy(hr.getId());
            app1 = applicationRepository.save(app1);

            // Candidate 2: Medium score
            Candidate candidate2 = new Candidate();
            candidate2.setFullName("Tran Thi B");
            candidate2.setEmail("tranthib@example.com");
            candidate2.setPhone("+84912345678");
            candidate2.setCurrentCompany("Startup XYZ");
            candidate2.setSource("Referral");
            candidate2.setLocation("Ho Chi Minh City, Vietnam");
            candidate2.setExperienceYears(3);
            candidate2.setSummary("Junior Frontend Developer.");
            candidate2.setCreatedBy(hr.getId());
            candidate2 = candidateRepository.save(candidate2);

            Application app2 = new Application();
            app2.setCandidate(candidate2);
            app2.setJob(frontendJob);
            app2.setStage(ApplicationStage.OFFER);
            app2.setStatus(ApplicationStatus.ACTIVE);
            app2.setCreatedBy(hr.getId());
            app2 = applicationRepository.save(app2);

            // Candidate 3: Low score
            Candidate candidate3 = new Candidate();
            candidate3.setFullName("Le Van C");
            candidate3.setEmail("levanc@example.com");
            candidate3.setPhone("+84911223344");
            candidate3.setCurrentCompany("Freelancer");
            candidate3.setSource("Job Fair");
            candidate3.setLocation("Da Nang, Vietnam");
            candidate3.setExperienceYears(1);
            candidate3.setSummary("Entry-level Developer.");
            candidate3.setCreatedBy(hr.getId());
            candidate3 = candidateRepository.save(candidate3);

            Application app3 = new Application();
            app3.setCandidate(candidate3);
            app3.setJob(frontendJob);
            app3.setStage(ApplicationStage.HIRED);
            app3.setStatus(ApplicationStatus.ACTIVE);
            app3.setCreatedBy(hr.getId());
            app3 = applicationRepository.save(app3);

            // ====================== 4. INTERVIEWS WITH SCORES (For Evaluation) ======================
            // Interview for Candidate 1 (High scores)
            Interview interview1 = new Interview();
            interview1.setApplication(app1);
            interview1.setScheduledAt(LocalDateTime.now().minus(2, ChronoUnit.DAYS));
            interview1.setLocation("Google Meet");
            interview1.setMeetingLink("https://meet.google.com/abc-defg-hij");
            interview1.setDurationMinutes(60);
            interview1.setType(InterviewType.ONLINE);
            interview1.setStatus(InterviewStatus.COMPLETED);
            interview1.setCreatedBy(hr.getId());
            interview1 = interviewRepository.save(interview1);

            // Participant for Interview 1
            InterviewParticipant participant1 = new InterviewParticipant();
            InterviewParticipant.InterviewParticipantId partId1 = new InterviewParticipant.InterviewParticipantId(interview1.getId(), hr.getId());
            participant1.setId(partId1);
            participant1.setInterview(interview1);
            participant1.setUser(hr);
            participant1.setRole(ParticipantRole.INTERVIEWER);
            interviewParticipantRepository.save(participant1);

            // Scores for Interview 1 (High scores: 9, 8, 9)
            InterviewScore score1_1 = new InterviewScore();
            score1_1.setInterview(interview1);
            score1_1.setInterviewer(hr);
            score1_1.setCriterion(criteria1.get(0)); // React JS
            score1_1.setScore(9);
            score1_1.setCreatedBy(hr.getId());
            interviewScoreRepository.save(score1_1);

            InterviewScore score1_2 = new InterviewScore();
            score1_2.setInterview(interview1);
            score1_2.setInterviewer(hr);
            score1_2.setCriterion(criteria1.get(1)); // CSS/UX
            score1_2.setScore(8);
            score1_2.setCreatedBy(hr.getId());
            interviewScoreRepository.save(score1_2);

            InterviewScore score1_3 = new InterviewScore();
            score1_3.setInterview(interview1);
            score1_3.setInterviewer(hr);
            score1_3.setCriterion(criteria1.get(2)); // Communication
            score1_3.setScore(9);
            score1_3.setCreatedBy(hr.getId());
            interviewScoreRepository.save(score1_3);

            // Interview for Candidate 2 (Medium scores)
            Interview interview2 = new Interview();
            interview2.setApplication(app2);
            interview2.setScheduledAt(LocalDateTime.now().minus(1, ChronoUnit.DAYS));
            interview2.setLocation("Zoom");
            interview2.setMeetingLink("https://zoom.us/j/123456789");
            interview2.setDurationMinutes(45);
            interview2.setType(InterviewType.ONLINE);
            interview2.setStatus(InterviewStatus.COMPLETED);
            interview2.setCreatedBy(hr.getId());
            interview2 = interviewRepository.save(interview2);

            InterviewParticipant participant2 = new InterviewParticipant();
            InterviewParticipant.InterviewParticipantId partId2 = new InterviewParticipant.InterviewParticipantId(interview2.getId(), hr.getId());
            participant2.setId(partId2);
            participant2.setInterview(interview2);
            participant2.setUser(hr);
            participant2.setRole(ParticipantRole.INTERVIEWER);
            interviewParticipantRepository.save(participant2);

            // Scores for Interview 2 (Medium: 7, 6, 7)
            InterviewScore score2_1 = new InterviewScore();
            score2_1.setInterview(interview2);
            score2_1.setInterviewer(hr);
            score2_1.setCriterion(criteria1.get(0));
            score2_1.setScore(7);
            score2_1.setCreatedBy(hr.getId());
            interviewScoreRepository.save(score2_1);

            InterviewScore score2_2 = new InterviewScore();
            score2_2.setInterview(interview2);
            score2_2.setInterviewer(hr);
            score2_2.setCriterion(criteria1.get(1));
            score2_2.setScore(6);
            score2_2.setCreatedBy(hr.getId());
            interviewScoreRepository.save(score2_2);

            InterviewScore score2_3 = new InterviewScore();
            score2_3.setInterview(interview2);
            score2_3.setInterviewer(hr);
            score2_3.setCriterion(criteria1.get(2));
            score2_3.setScore(7);
            score2_3.setCreatedBy(hr.getId());
            interviewScoreRepository.save(score2_3);

            // ====================== 5. OFFERS (Draft, Pending, Approved) ======================
            // Offer 1: Draft (for testing create/edit)
            Offer offer1 = new Offer();
            offer1.setApplication(app1);
            offer1.setSalary(new BigDecimal("3500.00"));
            offer1.setPositionTitle("Senior Frontend Developer");
            offer1.setStartDate(LocalDate.now().plusDays(30));
            offer1.setBenefits("Health insurance, 13th month salary, remote work options");
            offer1.setNotes("Strong candidate with excellent React skills");
            offer1.setStatus(OfferStatus.DRAFT);
            offer1.setCreatedBy(hr.getId());
            offer1 = offerRepository.save(offer1);

            // Offer 2: Pending Approval
            Offer offer2 = new Offer();
            offer2.setApplication(app2);
            offer2.setSalary(new BigDecimal("2800.00"));
            offer2.setPositionTitle("Frontend Developer");
            offer2.setStartDate(LocalDate.now().plusDays(25));
            offer2.setBenefits("Health insurance, team building");
            offer2.setNotes("Promising candidate");
            offer2.setStatus(OfferStatus.PENDING_APPROVAL);
            offer2.setCreatedBy(hr.getId());
            offer2 = offerRepository.save(offer2);

            // Offer 3: Approved (for PDF preview)
            Offer offer3 = new Offer();
            offer3.setApplication(app3);
            offer3.setSalary(new BigDecimal("4000.00"));
            offer3.setPositionTitle("Senior Frontend Developer");
            offer3.setStartDate(LocalDate.now().plusDays(14));
            offer3.setBenefits("Full benefits package");
            offer3.setStatus(OfferStatus.APPROVED);
            offer3.setCreatedBy(hr.getId());
            offer3 = offerRepository.save(offer3);

            // ====================== 6. ONBOARDING CHECKLISTS ======================
            // Onboarding for Candidate 1 (In Progress)
            OnboardingChecklist onboarding1 = new OnboardingChecklist();
            onboarding1.setApplication(app1);
            onboarding1.setTitle("Frontend Developer Onboarding");
            onboarding1.setStatus(OnboardingStatus.IN_PROGRESS);
            onboarding1.setCreatedBy(hr.getId());
            onboarding1 = onboardingChecklistRepository.save(onboarding1);

            // Onboarding for Candidate 2 (Not Started)
            OnboardingChecklist onboarding2 = new OnboardingChecklist();
            onboarding2.setApplication(app2);
            onboarding2.setTitle("Junior Frontend Onboarding");
            onboarding2.setStatus(OnboardingStatus.NOT_STARTED);
            onboarding2.setCreatedBy(hr.getId());
            onboarding2 = onboardingChecklistRepository.save(onboarding2);

            List<OnboardingTask> tasks2 = new ArrayList<>();
            OnboardingTask t2_1 = new OnboardingTask();
            t2_1.setChecklist(onboarding2);
            t2_1.setTitle("Setup Workstation");
            t2_1.setAssignedTo(hr);
            t2_1.setDueDate(LocalDate.now().plusDays(3));
            t2_1.setSortOrder(1);
            t2_1.setCreatedBy(hr.getId());
            tasks2.add(t2_1);

            OnboardingTask t2_2 = new OnboardingTask();
            t2_2.setChecklist(onboarding2);
            t2_2.setTitle("Codebase Walkthrough");
            t2_2.setAssignedTo(hr);
            t2_2.setDueDate(LocalDate.now().plusDays(5));
            t2_2.setSortOrder(2);
            t2_2.setCreatedBy(hr.getId());
            tasks2.add(t2_2);

            onboardingTaskRepository.saveAll(tasks2);

            return "Prioritized demo data seeded successfully!";
        } catch (Exception e) {
            log.error("Error seeding demo data", e);
            throw new RuntimeException("Failed to seed demo data: " + e.getMessage());
        }
    }
}
