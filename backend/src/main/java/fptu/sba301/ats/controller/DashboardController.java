package fptu.sba301.ats.controller;

import fptu.sba301.ats.constant.AdminConstants;
import fptu.sba301.ats.constant.AppConstant;
import fptu.sba301.ats.dto.response.DashboardStatsDTO;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstant.BASE_URL + AdminConstants.DASHBOARD_URL)
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'INTERVIEWER', 'SYSTEM_ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmailAndDeletedFalse(principal.getUsername())
                .orElseThrow(() -> new BusinessException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
        return ResponseEntity.ok(dashboardService.getDashboardStats(user));
    }
}
