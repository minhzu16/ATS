package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.AuditLogResponseDTO;
import fptu.sba301.ats.entity.AuditLog;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.repository.AuditLogRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("null")
    public Page<AuditLogResponseDTO> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDTO);
    }

    @Override
    @SuppressWarnings("null")
    public Page<AuditLogResponseDTO> getLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void logAction(UUID userId, String action, String entityType, String entityId,
            Map<String, Object> oldValue, Map<String, Object> newValue,
            String ipAddress, String userAgent) {
        AuditLog log = new AuditLog();
        if (userId != null) {
            log.setUserId(userId);
        }
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        try {
            log.setOldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null);
            log.setNewValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null);
        } catch (Exception e) {
            // handle exception
        }
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);

        auditLogRepository.save(log);
    }

    private AuditLogResponseDTO mapToDTO(AuditLog log) {
        User user = null;
        if (log.getUserId() != null) {
            user = userRepository.findById(log.getUserId()).orElse(null);
        }
        return AuditLogResponseDTO.builder()
                .id(log.getId())
                .userId(user != null ? user.getId() : log.getUserId())
                .userEmail(user != null ? user.getEmail() : null)
                .userFullName(user != null ? user.getFullName() : null)
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
