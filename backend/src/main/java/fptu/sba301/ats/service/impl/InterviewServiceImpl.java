package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.CriterionScoreRequest;
import fptu.sba301.ats.dto.request.SubmitFeedbackRequest;
import fptu.sba301.ats.dto.response.*;
import fptu.sba301.ats.entity.*;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.*;
import fptu.sba301.ats.service.EmailService;
import fptu.sba301.ats.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewScoreRepository scoreRepository;
    private final InterviewParticipantRepository participantRepository;
    private final InterviewScoreRepository interviewScoreRepository;
    private final ScorecardCriterionRepository criterionRepository;
    private final UserRepository userRepository;
    private final CandidateDocumentRepository candidateDocumentRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    private final InterviewMapper mapper = new InterviewMapper();
    private final InterviewNotificationHelper notificationHelper = new InterviewNotificationHelper();
    private final InterviewScheduleValidator scheduleValidator = new InterviewScheduleValidator();
    private final InterviewScoreCalculator scoreCalculator = new InterviewScoreCalculator();

    @Override
    public List<InterviewScorecardResponse> getAllScorecards(UUID interviewId) {
        List<InterviewParticipant> participants = participantRepository.findByIdInterviewId(interviewId);
        return participants.stream()
                .filter(p -> p.getUser() != null)
                .map(p -> mapper.toScorecardResponse(interviewId, p,
                        interviewScoreRepository.findByInterview_IdAndInterviewer_Id(interviewId, p.getUser().getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewResponse> getAllInterviews(String email) {
        List<Interview> interviews = interviewRepository.findAll();
        if (email != null) {
            User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
            if (user != null && user.getRole().name().equals("INTERVIEWER")) {
                interviews = interviews.stream()
                        .filter(i -> i.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(user.getId())))
                        .toList();
            }
        }
        return interviews.stream().map(mapper::toInterviewResponse).collect(Collectors.toList());
    }

    @Override
    public InterviewDetailResponse getInterviewDetail(UUID interviewId, String email) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new BusinessException("Interview not found", HttpStatus.NOT_FOUND));

        if (email != null) {
            User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
            if (user != null && user.getRole().name().equals("INTERVIEWER")) {
                boolean isParticipant = interview.getParticipants() != null && interview.getParticipants().stream()
                        .anyMatch(p -> p.getUser().getId().equals(user.getId()));
                if (!isParticipant) {
                    throw new BusinessException("You are not a participant of this interview", HttpStatus.FORBIDDEN);
                }
            }
        }

        String candidateResumeUrl = mapper.findCandidateResumeUrl(candidateDocumentRepository, interview.getApplication());
        return mapper.toInterviewDetailResponse(interview, candidateResumeUrl);
    }

    @Transactional
    @Override
    public void submitFeedback(SubmitFeedbackRequest req, String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        User authenticatedUser = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.UNAUTHORIZED));

        InterviewParticipant p = participantRepository
                .findByInterviewIdAndUserId(req.getInterviewId(), authenticatedUser.getId())
                .orElseThrow(() -> new BusinessException("You are not a participant in this interview", HttpStatus.FORBIDDEN));

        if (p.getRole() != ParticipantRole.INTERVIEWER) {
            throw new BusinessException("Only assigned interviewers can submit evaluation", HttpStatus.FORBIDDEN);
        }

        Interview interview = p.getInterview();
        ScorecardTemplate template = interview.getTemplate();
        if (template == null) {
            throw new BusinessException("Interview does not have an assigned scorecard template", HttpStatus.BAD_REQUEST);
        }

        List<CriterionScoreRequest> scoreReqs = req.getScores();
        if (scoreReqs == null || scoreReqs.isEmpty()) {
            throw new BusinessException("Scores cannot be empty", HttpStatus.BAD_REQUEST);
        }

        List<ScorecardCriterion> allowedCriteria = criterionRepository.findByTemplateId(template.getId());
        Set<UUID> allowedCriterionIds = allowedCriteria.stream()
                .map(ScorecardCriterion::getId)
                .collect(Collectors.toSet());

        for (CriterionScoreRequest sr : scoreReqs) {
            validate(sr.getScore());
            if (!allowedCriterionIds.contains(sr.getCriterionId())) {
                throw new BusinessException("Criterion " + sr.getCriterionId() + " does not belong to interview template", HttpStatus.BAD_REQUEST);
            }
        }

        scoreRepository.deleteOld(req.getInterviewId(), authenticatedUser.getId());

        User interviewer = authenticatedUser;
        List<InterviewScore> list = new ArrayList<>();
        for (CriterionScoreRequest sr : scoreReqs) {
            ScorecardCriterion criterion = criterionRepository.findById(sr.getCriterionId())
                    .orElseThrow(() -> new BusinessException("Criterion " + sr.getCriterionId() + " not found", HttpStatus.BAD_REQUEST));
            list.add(InterviewScore.builder()
                    .interview(interview).interviewer(interviewer).criterion(criterion).score(sr.getScore()).build());
        }
        scoreRepository.saveAll(list);

        BigDecimal overall = scoreCalculator.calculateWeightedInterviewerScore(list);
        p.setOverallScore(overall);
        p.setFeedback(req.getFeedback());
        p.setSubmittedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        participantRepository.save(p);

        List<InterviewParticipant> allParticipants = participantRepository.findByIdInterviewId(interview.getId());
        boolean allInterviewersDone = allParticipants.stream()
                .filter(part -> part.getRole() == ParticipantRole.INTERVIEWER)
                .allMatch(part -> part.getSubmittedAt() != null || part.getOverallScore() != null);

        if (allInterviewersDone) {
            interview.setStatus(InterviewStatus.COMPLETED);
        }
        interviewRepository.save(interview);
    }

    @Override
    public BigDecimal calculateFinalScore(Interview interview) {
        return scoreCalculator.calculateFinalScore(interview);
    }

    @Override
    public Interview getInterviewById(UUID interviewId) {
        return interviewRepository.findByIdWithParticipants(interviewId)
                .orElseThrow(() -> new BusinessException("Interview not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public ScorecardTemplateResponse getTemplateByInterviewId(UUID interviewId, String email) {
        Interview interview = getInterviewById(interviewId);
        assertCanViewInterview(interview, email);

        ScorecardTemplate template = interview.getTemplate();
        if (template == null) {
            throw new BusinessException("Interview does not have a template assigned", HttpStatus.BAD_REQUEST);
        }

        List<ScorecardCriterion> criteria = criterionRepository.findByTemplateId(template.getId());
        return mapper.toTemplateResponse(template, criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getInterviewFinalScore(UUID interviewId, String email) {
        Interview interview = getInterviewById(interviewId);
        assertCanViewInterview(interview, email);

        if (email != null) {
            User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
            if (user != null && user.getRole() == Role.INTERVIEWER) {
                boolean allSubmitted = interview.getParticipants().stream()
                        .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                        .allMatch(p -> p.getSubmittedAt() != null || p.getOverallScore() != null);
                if (!allSubmitted) {
                    return interview.getParticipants().stream()
                            .filter(p -> p.getUser().getId().equals(user.getId()))
                            .map(InterviewParticipant::getOverallScore)
                            .filter(Objects::nonNull)
                            .findFirst().orElse(BigDecimal.ZERO);
                }
            }
        }

        return calculateFinalScore(interview);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewEvaluationDetailResponse getInterviewEvaluationSummary(UUID interviewId, String email) {
        Interview interview = getInterviewById(interviewId);
        assertCanViewInterview(interview, email);

        User currentUser = email != null ? userRepository.findByEmailAndDeletedFalse(email).orElse(null) : null;
        boolean isInterviewer = currentUser != null && currentUser.getRole() == Role.INTERVIEWER;

        boolean allSubmitted = interview.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                .allMatch(p -> p.getSubmittedAt() != null || p.getOverallScore() != null);

        boolean blindMode = isInterviewer && !allSubmitted;

        List<InterviewerEvaluationResponse> interviewers = mapper.toInterviewerEvaluationResponses(
                interview.getParticipants(), blindMode, currentUser != null ? currentUser.getId() : null);

        List<InterviewScore> scoresToAggregate = interview.getScores();
        if (blindMode && currentUser != null) {
            scoresToAggregate = scoresToAggregate.stream()
                    .filter(s -> s.getInterviewer().getId().equals(currentUser.getId()))
                    .toList();
        }

        List<CriterionEvaluationResponse> criteria = scoreCalculator.calculateCriterionEvaluations(scoresToAggregate);

        BigDecimal finalScore = blindMode
                ? interviewers.stream().map(InterviewerEvaluationResponse::getOverallScore).filter(Objects::nonNull).findFirst().orElse(BigDecimal.ZERO)
                : calculateFinalScore(interview);

        return InterviewEvaluationDetailResponse.builder()
                .interviewId(interviewId)
                .finalScore(finalScore)
                .criteria(criteria)
                .interviewers(interviewers)
                .build();
    }

    @Override
    @Transactional
    public void cancelInterview(UUID interviewId, String email, String reason) {
        Interview interview = getInterviewById(interviewId);
        interview.setStatus(InterviewStatus.CANCELLED);
        interviewRepository.save(interview);

        notificationHelper.sendCancellationEmails(emailService, interview, reason);
    }

    @Override
    @Transactional
    public void rescheduleInterview(UUID interviewId, String email, LocalDateTime newTime) {
        scheduleValidator.validateRescheduleTime(newTime);
        Interview interview = getInterviewById(interviewId);
        interview.setScheduledAt(newTime);
        interview.setStatus(InterviewStatus.SCHEDULED);
        interviewRepository.save(interview);

        notificationHelper.sendRescheduleEmails(emailService, interview, newTime);
    }

    @Override
    public ApplicationEvaluationResponse getApplicationEvaluation(UUID applicationId, String email) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found", HttpStatus.NOT_FOUND));

        List<Interview> interviews = interviewRepository.findByApplicationId(applicationId);
        if (email != null) {
            User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
            if (user != null && user.getRole().name().equals("INTERVIEWER")) {
                boolean canAccess = interviews.stream().anyMatch(i ->
                        participantRepository.findByInterviewIdAndUserId(i.getId(), user.getId()).isPresent());
                if (!canAccess) {
                    throw new BusinessException("You are not allowed to view this evaluation", HttpStatus.FORBIDDEN);
                }
            }
        }

        List<InterviewStageEvaluationResponse> stages = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        int completedCount = 0;
        for (Interview interview : interviews) {
            BigDecimal score = calculateFinalScore(interview);
            boolean isCompleted = interview.getStatus() == InterviewStatus.COMPLETED || score.compareTo(BigDecimal.ZERO) > 0;
            if (isCompleted) {
                totalScore = totalScore.add(score);
                completedCount++;
            }
            stages.add(mapper.toStageEvaluationResponse(interview, score, isCompleted));
        }

        BigDecimal overallScore = scoreCalculator.calculateApplicationOverallScore(totalScore, completedCount);
        String recommendation = scoreCalculator.calculateRecommendation(completedCount, overallScore);
        return mapper.toApplicationEvaluationResponse(
                app, applicationId, overallScore, recommendation, completedCount, interviews.size(), stages
        );
    }

    private void validate(Integer score) {
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException("Score must be 1-5", HttpStatus.BAD_REQUEST);
        }
    }

    private void assertCanViewInterview(Interview interview, String email) {
        if (email == null) return;
        User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
        if (user != null && user.getRole() == Role.INTERVIEWER) {
            boolean isParticipant = interview.getParticipants() != null && interview.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(user.getId()));
            if (!isParticipant) {
                throw new BusinessException("You are not a participant in this interview", HttpStatus.FORBIDDEN);
            }
        }
    }
}
