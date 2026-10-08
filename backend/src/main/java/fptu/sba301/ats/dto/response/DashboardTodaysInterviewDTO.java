package fptu.sba301.ats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DashboardTodaysInterviewDTO {
    private UUID interviewId;
    private String candidateName;
    private String jobTitle;
    private String scheduledAt;
    private String location;
    private String status;
}
