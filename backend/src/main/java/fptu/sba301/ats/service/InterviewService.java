package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.SubmitFeedbackRequest;
import fptu.sba301.ats.dto.response.*;
import fptu.sba301.ats.entity.Interview;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InterviewService {
    List<InterviewScorecardResponse> getAllScorecards(UUID interviewId);
    List<InterviewResponse> getAllInterviews(String email);
    InterviewDetailResponse getInterviewDetail(UUID interviewId, String email);
    void submitFeedback(SubmitFeedbackRequest req, String email);
    BigDecimal calculateFinalScore(Interview interview);
    Interview getInterviewById(UUID interviewId);
    ScorecardTemplateResponse getTemplateByInterviewId(UUID interviewId, String email);
    InterviewEvaluationDetailResponse getInterviewEvaluationSummary(UUID interviewId, String email);
    BigDecimal getInterviewFinalScore(UUID interviewId, String email);
    ApplicationEvaluationResponse getApplicationEvaluation(UUID applicationId, String email);
    void cancelInterview(UUID interviewId, String email, String reason);
    void rescheduleInterview(UUID interviewId, String email, LocalDateTime newTime);
}
