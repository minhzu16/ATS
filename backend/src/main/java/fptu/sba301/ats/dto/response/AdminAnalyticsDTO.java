package fptu.sba301.ats.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class AdminAnalyticsDTO {
    private long totalApplications;
    private long totalUsers;
    private long newApplicationsThisMonth;
    private double conversionRate;
    private double timeToHireAverage;
    
    private Map<String, Long> hiringFunnel; // APPLIED, SCREENING, INTERVIEW, OFFER, HIRED
    
    private Map<String, Long> applicationsByDepartment;
    
    private List<JobPerformanceDTO> topJobs;
    
    private Map<String, Long> applicationsBySource;
    
    private Map<String, Long> rejectionReasons;
    
    private String period; // "7days", "30days", "quarter"

    @Data
    @Builder
    public static class JobPerformanceDTO {
        private String jobId;
        private String jobTitle;
        private long applications;
        private long hired;
        private double conversionRate;
    }
}
