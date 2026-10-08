package fptu.sba301.ats.dto.request;

import fptu.sba301.ats.enums.NotificationType;
import fptu.sba301.ats.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String title;
    private String message;
    private NotificationType type;
    private Role role;
    private List<UUID> userIds;
}
