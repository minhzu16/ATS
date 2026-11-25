package fptu.sba301.ats.config;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.Offer;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.entity.ScorecardTemplate;
import fptu.sba301.ats.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    static final int MIN_BOOTSTRAP_PASSWORD_LENGTH = 12;

    private final UserDepartmentSeedHelper userDepartmentSeedHelper;
    private final RecruitmentSeedHelper recruitmentSeedHelper;
    private final ApplicationPipelineSeedHelper applicationPipelineSeedHelper;
    private final InterviewScorecardSeedHelper interviewScorecardSeedHelper;
    private final OfferTokenSeedHelper offerTokenSeedHelper;

    /** Every seeded account shares this password, so there is no built-in default. */
    static String requireBootstrapPassword(String value) {
        if (value == null || value.isBlank() || value.length() < MIN_BOOTSTRAP_PASSWORD_LENGTH) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must be set (at least "
                    + MIN_BOOTSTRAP_PASSWORD_LENGTH + " characters) to seed the dev accounts; see backend/.env.example");
        }
        return value;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Database Seeder...");

        userDepartmentSeedHelper.seedDepartments();
        userDepartmentSeedHelper.seedUsers();
        seedDomainData();

        log.info("Database Seeder finished.");
    }

    private void seedDomainData() {
        Department itDept = userDepartmentSeedHelper.getDepartmentByName("IT");
        User admin = userDepartmentSeedHelper.getUserByEmail("admin@ats.com");
        User hrManager = userDepartmentSeedHelper.getUserByEmail("hrmanager@ats.com");
        User hrUser = userDepartmentSeedHelper.getUserByEmail("hr@ats.com");
        User interviewer = userDepartmentSeedHelper.getUserByEmail("interviewer@ats.com");

        List<Job> jobs = recruitmentSeedHelper.seedJobs(itDept, hrManager);
        recruitmentSeedHelper.seedJobApprovals(jobs, admin);

        Map<String, Candidate> candidatesByEmail = recruitmentSeedHelper.seedCandidates();
        recruitmentSeedHelper.seedCandidateDocuments();

        List<Application> applications = applicationPipelineSeedHelper.seedApplications(jobs, candidatesByEmail);
        applicationPipelineSeedHelper.seedCandidateStageHistory(applications);
        applicationPipelineSeedHelper.seedCandidateNotes(applications);

        List<ScorecardTemplate> templates = interviewScorecardSeedHelper.seedScorecardTemplates(itDept);
        ScorecardTemplate defaultTemplate = templates.isEmpty() ? null : templates.get(0);
        List<Interview> interviews = interviewScorecardSeedHelper.seedInterviews(applications, defaultTemplate);
        interviewScorecardSeedHelper.seedInterviewParticipants(interviews, interviewer, hrManager);
        List<ScorecardCriterion> criteria = interviewScorecardSeedHelper.seedScorecardCriteria(templates);
        interviewScorecardSeedHelper.seedInterviewScores(interviews, criteria, interviewer);

        List<Offer> offers = offerTokenSeedHelper.seedOffers(applications);
        offerTokenSeedHelper.seedOfferApprovals(offers, admin);
        offerTokenSeedHelper.seedRefreshTokens(hrUser);
    }
}
