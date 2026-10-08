package fptu.sba301.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class CandidateDocumentResponse {
    private UUID documentId;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSizeBytes;
    private LocalDateTime uploadedAt;
}
