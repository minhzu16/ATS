package fptu.sba301.ats.dto.request;

import fptu.sba301.ats.enums.ApplicationStage;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidateStageUpdateRequest {

    @NotNull(message = "Stage is required")
    private ApplicationStage stage;

    private String reason;
}
