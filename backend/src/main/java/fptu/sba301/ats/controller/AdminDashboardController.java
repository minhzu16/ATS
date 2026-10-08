package fptu.sba301.ats.controller;

import fptu.sba301.ats.constant.AdminConstants;
import fptu.sba301.ats.constant.AppConstant;
import fptu.sba301.ats.dto.response.AdminAnalyticsDTO;
import fptu.sba301.ats.dto.response.SystemHealthDTO;
import fptu.sba301.ats.dto.response.UserManagementStatsDTO;
import fptu.sba301.ats.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.BASE_URL + AdminConstants.DASHBOARD_URL)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * Get recruitment analytics
     * @param period "7days", "30days", or "quarter"
     * @return analytics data for dashboard
     */
    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsDTO> getAnalytics(
            @RequestParam(defaultValue = "30days") String period) {
        return ResponseEntity.ok(adminDashboardService.getAnalytics(period));
    }

    /**
     * Get system health status
     * @return system health metrics
     */
    @GetMapping("/health")
    public ResponseEntity<SystemHealthDTO> getSystemHealth() {
        return ResponseEntity.ok(adminDashboardService.getSystemHealth());
    }

    /**
     * Get user management statistics
     * @return user stats and activity metrics
     */
    @GetMapping("/users-stats")
    public ResponseEntity<UserManagementStatsDTO> getUserManagementStats() {
        return ResponseEntity.ok(adminDashboardService.getUserManagementStats());
    }
}
