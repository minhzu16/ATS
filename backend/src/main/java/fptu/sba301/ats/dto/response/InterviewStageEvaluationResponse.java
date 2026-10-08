package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.InterviewType;
import fptu.sba301.ats.enums.InterviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewStageEvaluationResponse {
    private UUID interviewId;
    private InterviewType type;
    private InterviewStatus status;
    private BigDecimal score;
    private String interviewerName;
    private LocalDateTime scheduledAt;
    private String feedbackSnippet;
}
