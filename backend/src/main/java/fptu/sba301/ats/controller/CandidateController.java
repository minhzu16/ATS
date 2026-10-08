package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.request.CandidateStageUpdateRequest;
import fptu.sba301.ats.dto.request.CreateCandidateRequest;
import fptu.sba301.ats.dto.request.ScheduleCandidateInterviewsRequest;
import fptu.sba301.ats.dto.response.BulkImportResponse;
import fptu.sba301.ats.dto.response.CandidateDetailResponse;
import fptu.sba301.ats.dto.response.CandidateListResponse;
import fptu.sba301.ats.dto.response.InterviewerOptionResponse;
import fptu.sba301.ats.dto.response.ScheduleCandidateInterviewsResponse;
import fptu.sba301.ats.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;
import static fptu.sba301.ats.constant.AppConstant.CANDIDATE_CONTROLLER_URL;

@RestController
@RequestMapping(BASE_URL + CANDIDATE_CONTROLLER_URL)
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    // ───────────── Existing endpoints ─────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<CandidateListResponse>> getCandidateList() {
        return ResponseEntity.ok(candidateService.getCandidateList());
    }

    @GetMapping("/interviewers")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<InterviewerOptionResponse>> getInterviewerOptions() {
        return ResponseEntity.ok(candidateService.getInterviewerOptions());
    }

    @GetMapping("/{candidateId}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<CandidateDetailResponse> getCandidateDetail(@PathVariable UUID candidateId) {
        return ResponseEntity.ok(candidateService.getCandidateDetail(candidateId));
    }

    @PatchMapping("/{candidateId}/stage")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "UPDATE_STAGE", resource = "CANDIDATE")
    public ResponseEntity<CandidateDetailResponse> updateCandidateStage(
            @PathVariable UUID candidateId,
            @Valid @RequestBody CandidateStageUpdateRequest request) {
        return ResponseEntity.ok(candidateService.updateCandidateStage(candidateId, request.getStage(), request.getReason()));
    }

    @PostMapping("/{candidateId}/start-screening")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "START_SCREENING", resource = "CANDIDATE")
    public ResponseEntity<CandidateDetailResponse> startScreening(@PathVariable UUID candidateId) {
        return ResponseEntity.ok(candidateService.startScreening(candidateId));
    }

    @PostMapping("/{candidateId}/withdraw")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "WITHDRAW", resource = "CANDIDATE")
    public ResponseEntity<CandidateDetailResponse> withdrawCandidate(
            @PathVariable UUID candidateId,
            @Valid @RequestBody fptu.sba301.ats.dto.request.WithdrawCandidateRequest request) {
        return ResponseEntity.ok(candidateService.withdrawCandidate(candidateId, request.getReason()));
    }

    @PostMapping("/{candidateId}/applications")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "ADD_APPLICATION", resource = "CANDIDATE")
    public ResponseEntity<CandidateDetailResponse> addApplication(
            @PathVariable UUID candidateId,
            @Valid @RequestBody fptu.sba301.ats.dto.request.AddCandidateApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidateService.addApplicationForCandidate(candidateId, request.getJobId()));
    }

    @GetMapping("/{candidateId}/history")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<fptu.sba301.ats.dto.response.CandidateHistoryResponse>> getStageHistory(
            @PathVariable UUID candidateId
    ) {
        return ResponseEntity.ok(candidateService.getStageHistory(candidateId));
    }

    @PostMapping("/{candidateId}/interviews/schedule")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "SCHEDULE_INTERVIEW", resource = "CANDIDATE")
    public ResponseEntity<ScheduleCandidateInterviewsResponse> scheduleCandidateInterviews(
            @PathVariable UUID candidateId,
            @Valid @RequestBody ScheduleCandidateInterviewsRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidateService.scheduleCandidateInterviews(candidateId, request));
    }

    // ───────────── New: Single Candidate Add ─────────────

    /**
     * Add a single candidate.
     * Request: multipart/form-data
     * - request (application/json part): candidate fields
     * - documents (optional, multiple files): CV / supporting documents to upload
     * to Cloudinary
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "CREATE_CANDIDATE", resource = "CANDIDATE")
    public ResponseEntity<CandidateDetailResponse> createCandidate(
            @RequestPart("request") @Valid CreateCandidateRequest request,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        CandidateDetailResponse response = candidateService.createCandidate(
                request,
                documents != null ? documents : Collections.emptyList());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ───────────── New: CSV Bulk Import ─────────────

    /**
     * Bulk-import candidates from a CSV file.
     * Request: multipart/form-data
     * - csv (required): CSV file with header row
     * - cvFiles (optional, multiple files): PDF CV files whose names match the
     * 'cvFileName' column in CSV
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "IMPORT_CANDIDATES", resource = "CANDIDATE")
    public ResponseEntity<BulkImportResponse> importCandidates(
            @RequestPart("csv") MultipartFile csvFile,
            @RequestPart(value = "cvFiles", required = false) List<MultipartFile> cvFiles) {
        BulkImportResponse response = candidateService.importCandidatesFromCsv(
                csvFile,
                cvFiles != null ? cvFiles : Collections.emptyList());
        return ResponseEntity.ok(response);
    }
}
