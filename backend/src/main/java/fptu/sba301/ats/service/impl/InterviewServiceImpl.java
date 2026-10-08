package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.CriterionScoreRequest;
import fptu.sba301.ats.dto.request.SubmitFeedbackRequest;
import fptu.sba301.ats.dto.request.SubmitInterviewScoreRequest;
import fptu.sba301.ats.dto.response.InterviewResponse;
import fptu.sba301.ats.dto.response.InterviewScorecardResponse;
import fptu.sba301.ats.dto.response.*;
import fptu.sba301.ats.entity.*;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.*;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.InterviewScore;
import fptu.sba301.ats.repository.InterviewParticipantRepository;
import fptu.sba301.ats.repository.InterviewScoreRepository;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.*;
import fptu.sba301.ats.repository.ScorecardCriterionRepository;
import fptu.sba301.ats.repository.UserRepository;
import org.springframework.http.HttpStatus;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.UUID;
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
    private final fptu.sba301.ats.service.EmailService emailService;


    @Override
    public List<InterviewScorecardResponse> getAllScorecards(UUID interviewId) {
        // Use participants as the primary source — they store overallScore and feedback per interviewer
        List<InterviewParticipant> participants = participantRepository.findByIdInterviewId(interviewId);

        return participants.stream()
                .filter(p -> p.getUser() != null)
                .map(p -> {
                    UUID userId = p.getUser().getId();

                    // Get per-criterion scores for this participant
                    List<InterviewScore> userScores = interviewScoreRepository
                            .findByInterview_IdAndInterviewer_Id(interviewId, userId);
                    List<InterviewScorecardResponse.ScoreDetail> details = userScores.stream()
                            .filter(s -> s.getCriterion() != null)
                            .map(s -> InterviewScorecardResponse.ScoreDetail.builder()
                                    .criterionId(s.getCriterion().getId())
                                    .criterionName(s.getCriterion().getName())
                                    .weight(s.getCriterion().getWeight())
                                    .score(s.getScore())
                                    .comment(s.getComment())
                                    .build())
                            .collect(Collectors.toList());

                    return InterviewScorecardResponse.builder()
                            .interviewId(interviewId)
                            .participantUserId(userId)
                            .participantName(p.getUser().getFullName())
                            .overallScore(p.getOverallScore())
                            .feedback(p.getFeedback())
                            .scores(details)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewResponse> getAllInterviews(String email) {
        List<Interview> interviews = interviewRepository.findAll();

        if (email != null) {
            User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
            if (user != null && user.getRole().name().equals("INTERVIEWER")) {
                interviews = interviews.stream()
                        .filter(i -> i.getParticipants().stream()
                                .anyMatch(p -> p.getUser().getId().equals(user.getId())))
                        .toList();
            }
        }

        return interviews.stream()
                .map(interview -> {
                    String candidateName = null;
                    String jobTitle = null;
                    if (interview.getApplication() != null) {
                        if (interview.getApplication().getCandidate() != null) {
                            candidateName = interview.getApplication().getCandidate().getFullName();
                        }
                        if (interview.getApplication().getJob() != null) {
                            jobTitle = interview.getApplication().getJob().getTitle();
                        }
                    }
                    return InterviewResponse.builder()
                        .id(interview.getId())
                        .scheduledAt(interview.getScheduledAt())
                        .startedAt(interview.getStartedAt())
                        .endedAt(interview.getEndedAt())
                        .location(interview.getLocation())
                        .meetingLink(interview.getMeetingLink())
                        .type(interview.getType())
                        .status(interview.getStatus())
                        .applicationId(
                                interview.getApplication() != null ? interview.getApplication().getId() : null
                        )
                        .templateId(
                                interview.getTemplate() != null ? interview.getTemplate().getId() : null
                        )
                        .participantCount(
                                interview.getParticipants() != null ? interview.getParticipants().size() : 0
                        )
                        .candidateName(candidateName)
                        .jobTitle(jobTitle)
                        .scoreCount(
                                interview.getScores() != null ? interview.getScores().size() : 0
                        )
                        .build();
                })
                .collect(Collectors.toList());
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

        UUID candidateId = null;
        String candidateName = null;
        String candidateEmail = null;
        String candidatePhone = null;
        String candidateResumeUrl = null;

        UUID jobId = null;
        String jobTitle = null;
        String jobDepartment = null;

        Application app = interview.getApplication();
        if (app != null) {
            Candidate c = app.getCandidate();
            if (c != null) {
                candidateId = c.getId();
                candidateName = c.getFullName();
                candidateEmail = c.getEmail();
                candidatePhone = c.getPhone();

                List<CandidateDocument> docs = candidateDocumentRepository.findByCandidateIdOrderByUploadedAtDesc(candidateId);
                candidateResumeUrl = docs.stream()
                        .filter(d -> "RESUME".equalsIgnoreCase(d.getFileType()))
                        .map(CandidateDocument::getFileUrl)
                        .findFirst()
                        .orElse(docs.isEmpty() ? null : docs.get(0).getFileUrl());
            }

            Job j = app.getJob();
            if (j != null) {
                jobId = j.getId();
                jobTitle = j.getTitle();
                if (j.getDepartment() != null) {
                    jobDepartment = j.getDepartment().getName();
                }
            }
        }

        List<ParticipantResponse> participants = new ArrayList<>();
        if (interview.getParticipants() != null) {
            participants = interview.getParticipants().stream()
                    .map(p -> ParticipantResponse.builder()
                            .userId(p.getUser().getId())
                            .fullName(p.getUser().getFullName())
                            .avatarUrl(null)
                            .role(p.getRole())
                            .build())
                    .toList();
        }

        return InterviewDetailResponse.builder()
                .id(interview.getId())
                .scheduledAt(interview.getScheduledAt())
                .startedAt(interview.getStartedAt())
                .endedAt(interview.getEndedAt())
                .location(interview.getLocation())
                .meetingLink(interview.getMeetingLink())
                .type(interview.getType())
                .status(interview.getStatus())
                .applicationId(app != null ? app.getId() : null)
                .templateId(interview.getTemplate() != null ? interview.getTemplate().getId() : null)
                .candidateId(candidateId)
                .candidateName(candidateName)
                .candidateEmail(candidateEmail)
                .candidatePhone(candidatePhone)
                .candidateResumeUrl(candidateResumeUrl)
                .jobId(jobId)
                .jobTitle(jobTitle)
                .jobDepartment(jobDepartment)
                .participants(participants)
                .build();
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

        // Validate that all submitted criteria belong to the template assigned to this interview
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

        // Always delete using authenticated user ID, never from untrusted client DTO
        scoreRepository.deleteOld(req.getInterviewId(), authenticatedUser.getId());

        User interviewer = authenticatedUser;

        // Save raw scores
        List<InterviewScore> list = new ArrayList<>();
        for (CriterionScoreRequest sr : scoreReqs) {
            ScorecardCriterion criterion = criterionRepository.findById(sr.getCriterionId())
                    .orElseThrow(() -> new BusinessException("Criterion " + sr.getCriterionId() + " not found", HttpStatus.BAD_REQUEST));

            list.add(InterviewScore.builder()
                    .interview(interview)
                    .interviewer(interviewer)
                    .criterion(criterion)
                    .score(sr.getScore())
                    .build());
        }

        scoreRepository.saveAll(list);

        // Calculate overall score for this interviewer
        BigDecimal overall = calculateWeightedInterviewerScore(list);
        p.setOverallScore(overall);
        p.setFeedback(req.getFeedback());
        p.setSubmittedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        participantRepository.save(p);

        // Check if ALL participants with role INTERVIEWER have submitted
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
        List<InterviewParticipant> interviewers =
                interview.getParticipants().stream()
                        .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                        .filter(p -> p.getOverallScore() != null)
                        .toList();

        if (interviewers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = interviewers.stream()
                .map(InterviewParticipant::getOverallScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(
                BigDecimal.valueOf(interviewers.size()),
                2,
                RoundingMode.HALF_UP
        );
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

        List<ScorecardCriterionResponse> criterionResponses =
                criterionRepository.findByTemplateId(template.getId())
                        .stream()
                        .map(c -> ScorecardCriterionResponse.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .weight(c.getWeight())
                                .build())
                        .toList();

        return ScorecardTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .departmentId(template.getDepartment() != null ? template.getDepartment().getId() : null)
                .departmentName(template.getDepartment() != null ? template.getDepartment().getName() : null)
                .criteria(criterionResponses)
                .build();
    }

    private BigDecimal calculateWeightedInterviewerScore(List<InterviewScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalScoreWeight = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (InterviewScore s : scores) {
            BigDecimal weight = s.getCriterion().getWeight();
            if (weight == null) {
                weight = BigDecimal.ONE;
            }
            BigDecimal score = BigDecimal.valueOf(s.getScore());

            totalScoreWeight = totalScoreWeight.add(score.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalScoreWeight.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    private void validate(Integer score) {
        if (score == null || score < 1 || score > 5) {
            throw new BusinessException("Score must be 1-5", HttpStatus.BAD_REQUEST);
        }
    }

    private void assertCanViewInterview(Interview interview, String email) {
        if (email == null) return;
        User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
        if (user != null && user.getRole() == fptu.sba301.ats.enums.Role.INTERVIEWER) {
            boolean isParticipant = interview.getParticipants() != null && interview.getParticipants().stream()
                    .anyMatch(p -> p.getUser().getId().equals(user.getId()));
            if (!isParticipant) {
                throw new BusinessException("You are not a participant in this interview", HttpStatus.FORBIDDEN);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getInterviewFinalScore(UUID interviewId, String email) {
        Interview interview = getInterviewById(interviewId);
        assertCanViewInterview(interview, email);

        if (email != null) {
            User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
            if (user != null && user.getRole() == fptu.sba301.ats.enums.Role.INTERVIEWER) {
                boolean allSubmitted = interview.getParticipants().stream()
                        .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                        .allMatch(p -> p.getSubmittedAt() != null || p.getOverallScore() != null);
                if (!allSubmitted) {
                    // Blind scoring: return only own overall score if submitted, or ZERO
                    return interview.getParticipants().stream()
                            .filter(p -> p.getUser().getId().equals(user.getId()))
                            .map(InterviewParticipant::getOverallScore)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(BigDecimal.ZERO);
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
        boolean isInterviewer = currentUser != null && currentUser.getRole() == fptu.sba301.ats.enums.Role.INTERVIEWER;

        boolean allSubmitted = interview.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                .allMatch(p -> p.getSubmittedAt() != null || p.getOverallScore() != null);

        // Blind scoring: if current user is INTERVIEWER and not all interviewers have submitted,
        // interviewer only sees their own evaluation, avoiding anchoring bias
        boolean blindMode = isInterviewer && !allSubmitted;

        List<InterviewerEvaluationResponse> interviewers = interview.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                .filter(p -> !blindMode || p.getUser().getId().equals(currentUser.getId()))
                .map(p -> InterviewerEvaluationResponse.builder()
                        .interviewerId(p.getUser().getId())
                        .interviewerName(p.getUser().getFullName())
                        .feedback(p.getFeedback())
                        .overallScore(p.getOverallScore())
                        .build())
                .toList();

        List<InterviewScore> scoresToAggregate = interview.getScores();
        if (blindMode) {
            scoresToAggregate = scoresToAggregate.stream()
                    .filter(s -> s.getInterviewer().getId().equals(currentUser.getId()))
                    .toList();
        }

        Map<ScorecardCriterion, List<InterviewScore>> scoresByCriterion =
                scoresToAggregate.stream()
                        .collect(Collectors.groupingBy(InterviewScore::getCriterion));

        List<CriterionEvaluationResponse> criteria = scoresByCriterion.entrySet().stream()
                .map(entry -> {
                    ScorecardCriterion criterion = entry.getKey();
                    List<InterviewScore> userScores = entry.getValue();

                    BigDecimal sum = userScores.stream()
                            .map(s -> BigDecimal.valueOf(s.getScore()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal avg = userScores.isEmpty() ? BigDecimal.ZERO : sum.divide(
                            BigDecimal.valueOf(userScores.size()),
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

                    return CriterionEvaluationResponse.builder()
                            .criterionId(criterion.getId())
                            .criterionName(criterion.getName())
                            .weight(criterion.getWeight())
                            .averageScore(avg)
                            .build();
                })
                .toList();

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

        // Send cancellation emails
        String candidateName = (interview.getApplication() != null && interview.getApplication().getCandidate() != null)
                ? interview.getApplication().getCandidate().getFullName() : "Candidate";
        String candidateEmail = (interview.getApplication() != null && interview.getApplication().getCandidate() != null)
                ? interview.getApplication().getCandidate().getEmail() : null;
        String jobTitle = (interview.getApplication() != null && interview.getApplication().getJob() != null)
                ? interview.getApplication().getJob().getTitle() : "Position";

        if (candidateEmail != null && !candidateEmail.isBlank()) {
            emailService.sendInterviewCancellation(candidateEmail, candidateName, candidateName, jobTitle, interview.getScheduledAt(), reason);
        }
        if (interview.getParticipants() != null) {
            for (InterviewParticipant p : interview.getParticipants()) {
                if (p.getUser() != null && p.getUser().getEmail() != null) {
                    emailService.sendInterviewCancellation(p.getUser().getEmail(), p.getUser().getFullName(), candidateName, jobTitle, interview.getScheduledAt(), reason);
                }
            }
        }
    }

    @Override
    @Transactional
    public void rescheduleInterview(UUID interviewId, String email, LocalDateTime newTime) {
        if (newTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot reschedule interview to a past time", HttpStatus.BAD_REQUEST);
        }
        Interview interview = getInterviewById(interviewId);
        interview.setScheduledAt(newTime);
        interview.setStatus(InterviewStatus.SCHEDULED);
        interviewRepository.save(interview);

        // Send reschedule invitation emails with new .ics
        String candidateName = (interview.getApplication() != null && interview.getApplication().getCandidate() != null)
                ? interview.getApplication().getCandidate().getFullName() : "Candidate";
        String candidateEmail = (interview.getApplication() != null && interview.getApplication().getCandidate() != null)
                ? interview.getApplication().getCandidate().getEmail() : null;
        String jobTitle = (interview.getApplication() != null && interview.getApplication().getJob() != null)
                ? interview.getApplication().getJob().getTitle() : "Position";

        if (candidateEmail != null && !candidateEmail.isBlank()) {
            emailService.sendInterviewInvitation(candidateEmail, candidateName, candidateName, jobTitle, newTime, interview.getDurationMinutes(), interview.getLocation(), interview.getMeetingLink());
        }
        if (interview.getParticipants() != null) {
            for (InterviewParticipant p : interview.getParticipants()) {
                if (p.getUser() != null && p.getUser().getEmail() != null) {
                    emailService.sendInterviewInvitation(p.getUser().getEmail(), p.getUser().getFullName(), candidateName, jobTitle, newTime, interview.getDurationMinutes(), interview.getLocation(), interview.getMeetingLink());
                }
            }
        }
    }

    @Override
        public ApplicationEvaluationResponse getApplicationEvaluation(UUID applicationId, String email) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found", HttpStatus.NOT_FOUND));

        List<Interview> interviews = interviewRepository.findByApplicationId(applicationId);

                if (email != null) {
                        User user = userRepository.findByEmailAndDeletedFalse(email).orElse(null);
                        if (user != null && user.getRole().name().equals("INTERVIEWER")) {
                                boolean canAccess = interviews.stream().anyMatch(interview ->
                                                participantRepository.findByInterviewIdAndUserId(interview.getId(), user.getId()).isPresent()
                                );

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
            String interviewerName = "N/A";
            String feedback = "";

            if (interview.getParticipants() != null) {
                // Get first interviewer name for summary
                interviewerName = interview.getParticipants().stream()
                        .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                        .map(p -> p.getUser().getFullName())
                        .findFirst()
                        .orElse("N/A");

                // Get feedback from first interviewer who provided it
                feedback = interview.getParticipants().stream()
                        .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                        .map(InterviewParticipant::getFeedback)
                        .filter(f -> f != null && !f.isEmpty())
                        .findFirst()
                        .orElse("");
            }

            boolean isCompleted = interview.getStatus() == InterviewStatus.COMPLETED || score.compareTo(BigDecimal.ZERO) > 0;

            if (isCompleted) {
                totalScore = totalScore.add(score);
                completedCount++;
            }

            stages.add(InterviewStageEvaluationResponse.builder()
                    .interviewId(interview.getId())
                    .type(interview.getType())
                    .status(isCompleted ? InterviewStatus.COMPLETED : interview.getStatus())
                    .score(score)
                    .interviewerName(interviewerName)
                    .scheduledAt(interview.getScheduledAt())
                    .feedbackSnippet(feedback)
                    .build());
        }

        BigDecimal overallScore = completedCount > 0
                ? totalScore.divide(BigDecimal.valueOf(completedCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String recommendation = "Pending";
        if (completedCount > 0) {
            recommendation = overallScore.compareTo(new BigDecimal("3.5")) >= 0 ? "Hire" : "No Hire";
        }

        return ApplicationEvaluationResponse.builder()
                .applicationId(applicationId)
                .candidateName(app.getCandidate() != null ? app.getCandidate().getFullName() : "Unknown")
                .jobTitle(app.getJob() != null ? app.getJob().getTitle() : "Unknown")
                .overallScore(overallScore)
                .recommendation(recommendation)
                .interviewsCompleted(completedCount)
                .totalInterviews(interviews.size())
                .stages(stages)
                .build();
    }
}
