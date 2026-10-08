package fptu.sba301.ats.dto.response;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserManagementStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long newUsersThisMonth;
    
    private long adminCount;
    private long hrManagerCount;
    private long recruiterCount;
    private long interviewerCount;
    
    private double averageLoginFrequency; // times per week
    private long usersInactiveMoreThan30Days;
    private long usersInactiveMoreThan60Days;
    
    private long failedLoginAttempts;
    private long accountsLocked;
    private long passwordExpiringSoon;
}
