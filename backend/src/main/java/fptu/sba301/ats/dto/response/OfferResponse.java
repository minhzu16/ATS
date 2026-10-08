package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferResponse {
    private UUID id;
    private UUID applicationId;
    /** Current stage of the linked application (updated by the offer workflow). */
    private ApplicationStage applicationStage;
    private String candidateName;
    private String jobTitle;
    private BigDecimal salary;
    private String positionTitle;
    private LocalDate startDate;
    private String benefits;
    private String notes;
    private OfferStatus status;
    private LocalDateTime createdAt;
}
