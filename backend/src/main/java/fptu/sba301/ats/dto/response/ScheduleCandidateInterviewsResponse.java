package fptu.sba301.ats.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ScheduleCandidateInterviewsResponse {
    private UUID candidateId;
    private UUID applicationId;
    private String stage;
    private Integer totalInterviewsCreated;
    private List<UUID> interviewIds;
}
