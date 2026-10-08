package fptu.sba301.ats.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfferRequest {

    @NotNull(message = "Candidate ID is required")
    private UUID candidateId;

    @NotNull(message = "Salary is required")
    private BigDecimal salary;

    @NotBlank(message = "Position title is required")
    private String positionTitle;

    private LocalDate startDate;
    private String benefits;
    private String notes;
}
