package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.DashboardStatsDTO;
import fptu.sba301.ats.entity.User;

public interface DashboardService {
    DashboardStatsDTO getDashboardStats(User currentUser);
}
