package fptu.sba301.ats.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CandidateListProjection {
    String getCandidateId();
    String getFullName();
    String getEmail();
    String getJobTitle();
    String getStage();
    BigDecimal getRating();
    LocalDateTime getAppliedAt();
}
