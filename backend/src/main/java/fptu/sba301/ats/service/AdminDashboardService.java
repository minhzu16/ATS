package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.AdminAnalyticsDTO;
import fptu.sba301.ats.dto.response.SystemHealthDTO;
import fptu.sba301.ats.dto.response.UserManagementStatsDTO;

public interface AdminDashboardService {
    /**
     * Get recruitment analytics for dashboard
     * @param period "7days", "30days", or "quarter"
     * @return admin analytics data
     */
    AdminAnalyticsDTO getAnalytics(String period);
    
    /**
     * Get system health status
     * @return system health data
     */
    SystemHealthDTO getSystemHealth();
    
    /**
     * Get user management statistics
     * @return user management stats
     */
    UserManagementStatsDTO getUserManagementStats();
}
