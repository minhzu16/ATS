package fptu.sba301.ats.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface JobApplicantProjection {
    String getCandidateId();

    String getFullName();

    String getEmail();

    String getStage();

    BigDecimal getRating();

    LocalDateTime getAppliedAt();
}
