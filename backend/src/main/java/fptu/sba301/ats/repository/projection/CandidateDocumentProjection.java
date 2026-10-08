package fptu.sba301.ats.repository.projection;

import java.time.LocalDateTime;

public interface CandidateDocumentProjection {
    String getDocumentId();
    String getFileName();
    String getFileUrl();
    String getFileType();
    Long getFileSizeBytes();
    LocalDateTime getUploadedAt();
}
