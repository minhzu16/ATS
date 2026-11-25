package fptu.sba301.ats.config;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.InterviewScore;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.entity.ScorecardTemplate;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.enums.InterviewType;
import fptu.sba301.ats.enums.ParticipantRole;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds scorecard templates, interviews, participants, criteria, and scores.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
class InterviewScorecardSeedHelper {

    private final EntityManager entityManager;
    private final SeedQueryHelper seedQueryHelper;

    List<ScorecardTemplate> seedScorecardTemplates(Department itDept) {
        if (seedQueryHelper.isEntityEmpty(ScorecardTemplate.class)) {
            log.info("Seeding Scorecard Templates...");
            entityManager.persist(ScorecardTemplate.builder()
                    .name("General Engineering Interview")
                    .department(itDept)
                    .build());
            entityManager.flush();
            log.info("Scorecard Templates seeded.");
        }

        return seedQueryHelper.findAll(ScorecardTemplate.class);
    }

    List<Interview> seedInterviews(List<Application> applications, ScorecardTemplate defaultTemplate) {
        if (seedQueryHelper.isEntityEmpty(Interview.class) && !applications.isEmpty() && defaultTemplate != null) {
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

        if (seedQueryHelper.isEntityEmpty(Interview.class) && defaultTemplate == null) {
            log.warn("Skip seeding Interviews because no ScorecardTemplate is available.");
        }

        return seedQueryHelper.findAll(Interview.class);
    }

    void seedInterviewParticipants(List<Interview> interviews, User interviewer, User hrManager) {
        if (seedQueryHelper.isEntityEmpty(InterviewParticipant.class) && !interviews.isEmpty() && interviewer != null) {
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
    }

    List<ScorecardCriterion> seedScorecardCriteria(List<ScorecardTemplate> templates) {
        if (seedQueryHelper.isEntityEmpty(ScorecardCriterion.class) && !templates.isEmpty()) {
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

        return seedQueryHelper.findAll(ScorecardCriterion.class);
    }

    void seedInterviewScores(List<Interview> interviews, List<ScorecardCriterion> criteria, User interviewer) {
        if (seedQueryHelper.isEntityEmpty(InterviewScore.class) && !interviews.isEmpty() && !criteria.isEmpty() && interviewer != null) {
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
    }
}
