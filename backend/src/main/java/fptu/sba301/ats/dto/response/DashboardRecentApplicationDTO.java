package fptu.sba301.ats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DashboardRecentApplicationDTO {
    private UUID applicationId;
    private String candidateName;
    private String jobTitle;
    private String stage;
    private String appliedAt;
}
