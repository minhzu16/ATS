package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.ApprovalStatus;
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
public class OfferApprovalResponse {
    private UUID id;
    private UUID offerId;
    private String approvedByName;
    private ApprovalStatus status;
    private String comment;
    private LocalDateTime createdAt;
}
