package fptu.sba301.ats.dto.response;

import lombok.Data;
import lombok.Builder;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationResponseDTO {
    private String id;
    private String title;
    private String message;
    private boolean read;
    private String type;
    private Instant createdAt;
}
