package fptu.sba301.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CandidateDetailResponse {
    private UUID candidateId;
    private UUID applicationId;
    private String fullName;
    private String email;
    private String phone;
    private String currentCompany;
    private String jobTitle;
    private String stage;
    private String status;
    private BigDecimal rating;
    private LocalDateTime appliedAt;
    private String source;
    private String location;
    private Integer experienceYears;
    private String summary;
    private List<CandidateDocumentResponse> documents;
}
