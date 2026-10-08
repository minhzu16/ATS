package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.request.JobUpdateRequest;
import fptu.sba301.ats.dto.response.JobDetailResponse;
import fptu.sba301.ats.dto.response.JobResponse;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.JobCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL + "/jobs")
@RequiredArgsConstructor
public class JobCommandController {

    private final JobCommandService jobCommandService;
    private final fptu.sba301.ats.security.CurrentUserProvider currentUserProvider;

    /** Create is handled by {@link JobController#createJob} (same path/method) to keep a single POST mapping. */

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    @fptu.sba301.ats.annotation.LogAudit(action = "UPDATE_JOB", resource = "JOB")
    public ResponseEntity<JobResponse> updateJob(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") UUID id,
            @Valid @RequestBody JobUpdateRequest request
    ) {
        User user = currentUserProvider.resolveUser(principal);
        return ResponseEntity.ok(jobCommandService.updateJob(user, id, request));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('HR_MANAGER')")
    public ResponseEntity<List<JobResponse>> listPending(
            @AuthenticationPrincipal UserDetails principal
    ) {
        User user = currentUserProvider.resolveUser(principal);
        return ResponseEntity.ok(jobCommandService.listPendingJobs(user));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "APPROVE_JOB", resource = "JOB")
    public ResponseEntity<JobResponse> approve(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") UUID id
    ) {
        User user = currentUserProvider.resolveUser(principal);
        return ResponseEntity.ok(jobCommandService.approveJob(user, id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "REJECT_JOB", resource = "JOB")
    public ResponseEntity<JobResponse> reject(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") UUID id,
            @RequestBody(required = false) java.util.Map<String, String> body
    ) {
        User user = currentUserProvider.resolveUser(principal);
        String reason = (body != null) ? body.get("reason") : "Job rejected by manager";
        return ResponseEntity.ok(jobCommandService.rejectJob(user, id, reason));
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'SYSTEM_ADMIN')")
    @fptu.sba301.ats.annotation.LogAudit(action = "CLOSE_JOB", resource = "JOB")
    public ResponseEntity<JobResponse> closeJob(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") UUID id
    ) {
        User user = currentUserProvider.resolveUser(principal);
        return ResponseEntity.ok(jobCommandService.closeJob(user, id));
    }

    /**
     * HR-only: load job detail for the edit form (same department), without the APPROVED-only restriction
     * applied by {@link JobDetailController#getJobDetail}.
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<JobDetailResponse> getJobForEdit(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable("id") UUID id
    ) {
        User user = currentUserProvider.resolveUser(principal);
        return ResponseEntity.ok(jobCommandService.getJobForEdit(user, id));
    }
}
