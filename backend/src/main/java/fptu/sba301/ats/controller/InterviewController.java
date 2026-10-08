package fptu.sba301.ats.controller;

import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.dto.request.SubmitFeedbackRequest;
import fptu.sba301.ats.dto.response.*;
import fptu.sba301.ats.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.*;

@RestController
@RequestMapping(BASE_URL + INTERVIEW_CONTROLLER_URL)
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    @PreAuthorize("hasAnyRole('INTERVIEWER', 'HR', 'HR_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<InterviewResponse>> getAllInterviews(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(interviewService.getAllInterviews(email));
    }

    @PostMapping("/feedback")
    @PreAuthorize("hasAnyRole('INTERVIEWER')")
    @LogAudit(action = "SUBMIT_INTERVIEW_FEEDBACK", resource = "INTERVIEW")
    public ResponseEntity<Map<String, String>> submitFeedback(
            @RequestBody SubmitFeedbackRequest request,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        interviewService.submitFeedback(request, email);
        return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
    }

    @GetMapping("/{interviewId}")
    @PreAuthorize("hasAnyRole('INTERVIEWER', 'HR', 'HR_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<InterviewDetailResponse> getInterviewDetail(
            @PathVariable UUID interviewId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(interviewService.getInterviewDetail(interviewId, email));
    }

    @GetMapping("/{interviewId}/final-score")
    @PreAuthorize("hasAnyRole('INTERVIEWER', 'HR', 'HR_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<BigDecimal> getFinalScore(
            @PathVariable UUID interviewId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        BigDecimal result = interviewService.getInterviewFinalScore(interviewId, email);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{interviewId}/template")
    @PreAuthorize("hasAnyRole('INTERVIEWER', 'HR', 'HR_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ScorecardTemplateResponse> getInterviewTemplate(
            @PathVariable UUID interviewId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(interviewService.getTemplateByInterviewId(interviewId, email));
    }

    @GetMapping("/{interviewId}/evaluation")
    @PreAuthorize("hasAnyRole('INTERVIEWER', 'HR', 'HR_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<InterviewEvaluationDetailResponse> getCandidateEvaluation(
            @PathVariable UUID interviewId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(interviewService.getInterviewEvaluationSummary(interviewId, email));
    }

    @PatchMapping("/{interviewId}/cancel")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @LogAudit(action = "CANCEL_INTERVIEW", resource = "INTERVIEW")
    public ResponseEntity<Map<String, String>> cancelInterview(
            @PathVariable UUID interviewId,
            @RequestParam(required = false, defaultValue = "Cancelled by HR") String reason,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        interviewService.cancelInterview(interviewId, email, reason);
        return ResponseEntity.ok(Map.of("message", "Interview cancelled successfully"));
    }

    @PatchMapping("/{interviewId}/reschedule")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @LogAudit(action = "RESCHEDULE_INTERVIEW", resource = "INTERVIEW")
    public ResponseEntity<Map<String, String>> rescheduleInterview(
            @PathVariable UUID interviewId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        interviewService.rescheduleInterview(interviewId, email, scheduledAt);
        return ResponseEntity.ok(Map.of("message", "Interview rescheduled successfully"));
    }

    @GetMapping("/applications/{applicationId}/evaluation")
    @PreAuthorize("hasAnyRole('INTERVIEWER', 'HR', 'HR_MANAGER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ApplicationEvaluationResponse> getApplicationEvaluation(
            @PathVariable UUID applicationId,
            Authentication authentication
    ) {
        String email = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(interviewService.getApplicationEvaluation(applicationId, email));
    }
}