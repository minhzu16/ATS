package fptu.sba301.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewerEvaluationResponse {
    private UUID interviewerId;
    private String interviewerName;
    private String feedback;
    private BigDecimal overallScore;
}
