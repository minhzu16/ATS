package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.request.JobCreateRequest;
import fptu.sba301.ats.dto.response.JobResponse;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.JobCommandService;
import fptu.sba301.ats.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL + "/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobCommandService jobCommandService;
    private final fptu.sba301.ats.security.CurrentUserProvider currentUserProvider;

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    /**
     * Create job (HR only). Declared here so {@code POST /api/v1/jobs} is registered on this controller
     * alongside {@code GET /api/v1/jobs} (avoids 405 if another job controller bean is not loaded).
     */
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @fptu.sba301.ats.annotation.LogAudit(action = "CREATE_JOB", resource = "JOB")
    public ResponseEntity<JobResponse> createJob(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody JobCreateRequest request
    ) {
        User user = currentUserProvider.resolveUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobCommandService.createJob(user, request));
    }
}
