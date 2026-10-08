package fptu.sba301.ats.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class SystemConfigResponseDTO {
    private String configKey;
    private String value;
    private Instant updatedAt;
    private String updatedBy;
}
