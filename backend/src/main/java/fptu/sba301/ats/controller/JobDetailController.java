package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.response.JobApplicantResponse;
import fptu.sba301.ats.dto.response.JobDetailResponse;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.JobDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL + "/jobs")
@RequiredArgsConstructor
public class JobDetailController {

    private final JobDetailService jobDetailService;
    private final fptu.sba301.ats.security.CurrentUserProvider currentUserProvider;

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'SYSTEM_ADMIN', 'INTERVIEWER')")
    public ResponseEntity<JobDetailResponse> getJobDetail(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User currentUser = currentUserProvider.resolveUser(principal);
        return ResponseEntity.ok(jobDetailService.getJobDetail(currentUser, id));
    }

    @GetMapping("/{id}/applicants")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'SYSTEM_ADMIN', 'INTERVIEWER')")
    public ResponseEntity<List<JobApplicantResponse>> listJobApplicants(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User currentUser = currentUserProvider.resolveUser(principal);
        return ResponseEntity.ok(jobDetailService.listJobApplicants(currentUser, id));
    }
}
