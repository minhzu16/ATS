package fptu.sba301.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CandidateListResponse {
    private UUID candidateId;
    private String fullName;
    private String email;
    private String jobTitle;
    private String stage;
    private BigDecimal rating;
    private LocalDateTime appliedAt;
}
