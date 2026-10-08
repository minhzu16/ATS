package fptu.sba301.ats.dto.request;

import fptu.sba301.ats.enums.ApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferApprovalRequest {

    @NotNull(message = "Approval status is required")
    private ApprovalStatus status;

    private String comment;
}
