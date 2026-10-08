package fptu.sba301.ats.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CandidateDetailProjection {
    String getCandidateId();
    String getApplicationId();
    String getFullName();
    String getEmail();
    String getPhone();
    String getCurrentCompany();
    String getJobTitle();
    String getStage();
    String getStatus();
    BigDecimal getRating();
    LocalDateTime getAppliedAt();
    String getSource();
    String getLocation();
    Integer getExperienceYears();
    String getSummary();
}
