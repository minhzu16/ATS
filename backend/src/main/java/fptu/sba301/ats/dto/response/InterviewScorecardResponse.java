package fptu.sba301.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewScorecardResponse {
    private UUID interviewId;
    private UUID participantUserId;
    private String participantName;
    private BigDecimal overallScore;
    private String feedback;
    private List<ScoreDetail> scores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreDetail {
        private UUID criterionId;
        private String criterionName;
        private BigDecimal weight;
        private Integer score;
        private String comment;
    }
}
