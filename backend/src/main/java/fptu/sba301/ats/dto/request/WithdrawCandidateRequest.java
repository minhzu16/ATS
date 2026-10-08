package fptu.sba301.ats.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawCandidateRequest {
    @NotBlank(message = "Withdrawal reason is required")
    private String reason;
}
