package fptu.sba301.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateNoteResponse {
    private UUID id;
    private UUID applicationId;
    private UUID candidateId;
    private String content;
    private UUID createdBy;
    private String authorName;
    private LocalDateTime createdAt;
}
