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
public class ApplicationEvaluationResponse {
    private UUID applicationId;
    private String candidateName;
    private String jobTitle;
    private BigDecimal overallScore;
    private String recommendation;
    private int interviewsCompleted;
    private int totalInterviews;
    private List<InterviewStageEvaluationResponse> stages;
}
