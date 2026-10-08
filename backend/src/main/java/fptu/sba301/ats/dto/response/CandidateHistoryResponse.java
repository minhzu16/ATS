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
public class CandidateHistoryResponse {
    private UUID id;
    private ApplicationStage fromStage;
    private ApplicationStage toStage;
    private String changedBy;
    private LocalDateTime changedAt;
    private String reason;
}
