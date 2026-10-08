package fptu.sba301.ats.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubmitFeedbackRequest {

    private UUID interviewId;
    private List<CriterionScoreRequest> scores;
    private Integer overallScore;
    private String feedback;
}
