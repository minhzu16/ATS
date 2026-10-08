package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.response.JobPageResponse;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.JobListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL + "/jobs")
@RequiredArgsConstructor
public class JobPagedController {

    private final JobListService jobListService;
    private final fptu.sba301.ats.security.CurrentUserProvider currentUserProvider;

    /**
     * Paginated job list with title search. Uses the same base path as {@link JobController}
     * with sub-path {@code /page} to avoid conflicting with {@code GET /jobs} (non-paged list).
     */
    @GetMapping("/page")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'SYSTEM_ADMIN', 'INTERVIEWER')")
    public ResponseEntity<JobPageResponse> listPaged(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        User user = currentUserProvider.resolveUser(principal);
        JobPageResponse body = jobListService.listJobsForUser(user, page, size, keyword);
        return ResponseEntity.ok(body);
    }
}
