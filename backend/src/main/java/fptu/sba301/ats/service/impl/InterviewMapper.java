package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.*;
import fptu.sba301.ats.entity.*;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.repository.CandidateDocumentRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class InterviewMapper {

    public InterviewScorecardResponse toScorecardResponse(
            UUID interviewId,
            InterviewParticipant participant,
            List<InterviewScore> userScores) {
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
                .participantUserId(participant.getUser().getId())
                .participantName(participant.getUser().getFullName())
                .overallScore(participant.getOverallScore())
                .feedback(participant.getFeedback())
                .scores(details)
                .build();
    }

    public InterviewResponse toInterviewResponse(Interview interview) {
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
                .applicationId(interview.getApplication() != null ? interview.getApplication().getId() : null)
                .templateId(interview.getTemplate() != null ? interview.getTemplate().getId() : null)
                .participantCount(interview.getParticipants() != null ? interview.getParticipants().size() : 0)
                .candidateName(candidateName)
                .jobTitle(jobTitle)
                .scoreCount(interview.getScores() != null ? interview.getScores().size() : 0)
                .build();
    }

    public InterviewDetailResponse toInterviewDetailResponse(Interview interview, String candidateResumeUrl) {
        UUID candidateId = null;
        String candidateName = null;
        String candidateEmail = null;
        String candidatePhone = null;

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

    public String findCandidateResumeUrl(CandidateDocumentRepository repository, Application app) {
        if (app != null && app.getCandidate() != null) {
            List<CandidateDocument> docs = repository.findByCandidateIdOrderByUploadedAtDesc(app.getCandidate().getId());
            return docs.stream()
                    .filter(d -> "RESUME".equalsIgnoreCase(d.getFileType()))
                    .map(CandidateDocument::getFileUrl)
                    .findFirst()
                    .orElse(docs.isEmpty() ? null : docs.get(0).getFileUrl());
        }
        return null;
    }

    public ScorecardTemplateResponse toTemplateResponse(ScorecardTemplate template, List<ScorecardCriterion> criteria) {
        List<ScorecardCriterionResponse> criterionResponses = criteria.stream()
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

    public List<InterviewerEvaluationResponse> toInterviewerEvaluationResponses(
            List<InterviewParticipant> participants,
            boolean blindMode,
            UUID currentUserId) {
        return participants.stream()
                .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                .filter(p -> !blindMode || (currentUserId != null && p.getUser().getId().equals(currentUserId)))
                .map(p -> InterviewerEvaluationResponse.builder()
                        .interviewerId(p.getUser().getId())
                        .interviewerName(p.getUser().getFullName())
                        .feedback(p.getFeedback())
                        .overallScore(p.getOverallScore())
                        .build())
                .toList();
    }

    public InterviewStageEvaluationResponse toStageEvaluationResponse(
            Interview interview,
            BigDecimal score,
            boolean isCompleted) {
        String interviewerName = "N/A";
        String feedback = "";

        if (interview.getParticipants() != null) {
            interviewerName = interview.getParticipants().stream()
                    .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                    .map(p -> p.getUser().getFullName())
                    .findFirst()
                    .orElse("N/A");

            feedback = interview.getParticipants().stream()
                    .filter(p -> p.getRole() == ParticipantRole.INTERVIEWER)
                    .map(InterviewParticipant::getFeedback)
                    .filter(f -> f != null && !f.isEmpty())
                    .findFirst()
                    .orElse("");
        }

        return InterviewStageEvaluationResponse.builder()
                .interviewId(interview.getId())
                .type(interview.getType())
                .status(isCompleted ? fptu.sba301.ats.enums.InterviewStatus.COMPLETED : interview.getStatus())
                .score(score)
                .interviewerName(interviewerName)
                .scheduledAt(interview.getScheduledAt())
                .feedbackSnippet(feedback)
                .build();
    }

    public ApplicationEvaluationResponse toApplicationEvaluationResponse(
            Application app,
            UUID applicationId,
            BigDecimal overallScore,
            String recommendation,
            int completedCount,
            int totalInterviews,
            List<InterviewStageEvaluationResponse> stages) {
        return ApplicationEvaluationResponse.builder()
                .applicationId(applicationId)
                .candidateName(app.getCandidate() != null ? app.getCandidate().getFullName() : "Unknown")
                .jobTitle(app.getJob() != null ? app.getJob().getTitle() : "Unknown")
                .overallScore(overallScore)
                .recommendation(recommendation)
                .interviewsCompleted(completedCount)
                .totalInterviews(totalInterviews)
                .stages(stages)
                .build();
    }
}
