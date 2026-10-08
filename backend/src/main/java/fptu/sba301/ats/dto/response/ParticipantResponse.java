package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private ParticipantRole role;
}
