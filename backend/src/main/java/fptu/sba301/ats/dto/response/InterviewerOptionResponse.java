package fptu.sba301.ats.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class InterviewerOptionResponse {
    private UUID id;
    private String fullName;
    private String email;
}
