package fptu.sba301.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateEvaluationResponse {

    private UUID applicationId;
    private UUID candidateId;
    private String candidateName;
    private String jobTitle;
    private Double overallScore;

    private List<InterviewEvaluation> interviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InterviewEvaluation {
        private UUID interviewId;
        private java.time.LocalDateTime scheduledAt;
        private String type;
        private String status;
        private Double averageScore;
        private int interviewerCount;
        private List<InterviewScorecardResponse> scorecards;
    }
}