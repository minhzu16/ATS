package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.AuditLogResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

import java.util.UUID;

public interface AuditLogService {
    Page<AuditLogResponseDTO> getAllLogs(Pageable pageable);
    Page<AuditLogResponseDTO> getLogsByAction(String action, Pageable pageable);

    void logAction(UUID userId, String action, String entityType, String entityId,
            Map<String, Object> oldValue, Map<String, Object> newValue,
            String ipAddress, String userAgent);
}
