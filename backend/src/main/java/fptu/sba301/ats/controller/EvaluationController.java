package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.response.CandidateEvaluationResponse;
import fptu.sba301.ats.dto.response.CandidateRankingResponse;
import fptu.sba301.ats.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL)
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @GetMapping("/applications/{applicationId}/evaluation")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'INTERVIEWER')")
    public ResponseEntity<CandidateEvaluationResponse> getEvaluationSummary(
            @PathVariable UUID applicationId) {
        return ResponseEntity.ok(evaluationService.getEvaluationSummary(applicationId));
    }

    @GetMapping("/jobs/{jobId}/ranking")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<CandidateRankingResponse>> getCandidateRanking(
            @PathVariable UUID jobId) {
        return ResponseEntity.ok(evaluationService.getCandidateRanking(jobId));
    }
}
