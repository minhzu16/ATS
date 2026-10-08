package fptu.sba301.ats.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.Map;
import java.util.List;

@Data
@Builder
public class DashboardStatsDTO {
    private int activeJobs;
    private int newCandidates;
    private int interviewsToday;
    private int offersSent;
    private Map<String, Long> hiringPipeline;
    private List<DashboardRecentApplicationDTO> recentApplications;
    private List<DashboardTodaysInterviewDTO> todaysInterviews;
}
