package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.ApplicationStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRankingResponse {
    private int rank;
    private UUID candidateId;
    private String candidateName;
    private UUID applicationId;
    private double overallScore;
    private Integer experienceYears;
    private LocalDateTime appliedAt;
    private ApplicationStage stage;
}
