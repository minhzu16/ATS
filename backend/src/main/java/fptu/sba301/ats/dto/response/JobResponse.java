package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.JobStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class JobResponse {
    private UUID jobId;
    /** Present when returned from list/detail mappers that load the job's department. */
    private UUID departmentId;
    /** Human-readable department name for list/detail UIs. */
    private String departmentName;
    private String title;
    private JobStatus status;
}
