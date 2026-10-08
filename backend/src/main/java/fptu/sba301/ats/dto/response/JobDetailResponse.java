package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.JobStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class JobDetailResponse {
    private UUID jobId;
    private String title;
    private String description;
    private String departmentName;
    private UUID hiringManagerId;
    private String hiringManagerName;
    private JobStatus status;
    private Integer headcount;
    private List<JobApplicantResponse> applicants;
}
