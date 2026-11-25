package fptu.sba301.ats.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fptu.sba301.ats.dto.response.AuditLogResponseDTO;
import fptu.sba301.ats.entity.AuditLog;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.repository.AuditLogRepository;
import fptu.sba301.ats.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void getAllLogsMapsUserMetadata() {
        UUID userId = UUID.randomUUID();
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setUserId(userId);
        log.setAction("CREATE");
        log.setEntityType("Job");
        log.setEntityId("123");

        User user = User.builder().id(userId).email("admin@ats.com").fullName("Admin User").build();
        when(auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(log)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Page<AuditLogResponseDTO> page = auditLogService.getAllLogs(PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
        assertEquals("admin@ats.com", page.getContent().get(0).getUserEmail());
    }

    @Test
    void getLogsByActionFiltersByAction() {
        AuditLog log = new AuditLog();
        log.setId(2L);
        log.setAction("DELETE");
        when(auditLogRepository.findByActionOrderByCreatedAtDesc("DELETE", PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(log)));

        Page<AuditLogResponseDTO> page = auditLogService.getLogsByAction("DELETE", PageRequest.of(0, 5));
        assertEquals(1, page.getTotalElements());
        assertEquals("DELETE", page.getContent().get(0).getAction());
    }

    @Test
    void logActionSerializesOldAndNewValues() {
        UUID userId = UUID.randomUUID();
        auditLogService.logAction(
                userId, "UPDATE", "Department", "d-1",
                Map.of("name", "Old"), Map.of("name", "New"),
                "127.0.0.1", "JUnit");

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
