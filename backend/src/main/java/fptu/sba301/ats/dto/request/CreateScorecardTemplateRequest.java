package fptu.sba301.ats.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class CreateScorecardTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private UUID departmentId;

    @NotEmpty(message = "At least one criterion is required")
    @Valid
    private List<CriterionRequest> criteria;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CriterionRequest {
        @NotBlank(message = "Criterion name is required")
        private String name;

        private BigDecimal weight;
    }
}
