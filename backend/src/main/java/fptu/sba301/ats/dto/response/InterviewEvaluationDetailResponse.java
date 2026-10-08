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
public class InterviewEvaluationDetailResponse {

    private UUID interviewId;
    private BigDecimal finalScore;
    private List<CriterionEvaluationResponse> criteria;
    private List<InterviewerEvaluationResponse> interviewers;
}
