package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.SystemConfigResponseDTO;
import fptu.sba301.ats.entity.SystemConfig;
import fptu.sba301.ats.event.SystemEvent;
import fptu.sba301.ats.repository.SystemConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemConfigServiceImplTest {

    @Mock
    private SystemConfigRepository systemConfigRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SystemConfigServiceImpl systemConfigService;

    @Test
    void getAllConfigsMapsToDto() {
        SystemConfig cfg = new SystemConfig();
        cfg.setKey("SITE_NAME");
        cfg.setValue("ATS");
        cfg.setUpdatedAt(Instant.now());

        when(systemConfigRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(cfg)));

        Page<SystemConfigResponseDTO> result = systemConfigService.getAllConfigs(PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());
        assertEquals("SITE_NAME", result.getContent().get(0).getConfigKey());
    }

    @Test
    void updateConfigsSavesEntriesAndPublishesEvent() {
        when(systemConfigRepository.findById("NOTIFICATIONS_ENABLED")).thenReturn(Optional.empty());
        when(systemConfigRepository.save(any(SystemConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        systemConfigService.updateConfigs(Map.of("NOTIFICATIONS_ENABLED", "true"));

        verify(systemConfigRepository).save(any(SystemConfig.class));
        verify(eventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void updateConfigsSkipsWhenEmpty() {
        systemConfigService.updateConfigs(Map.of());
        verify(systemConfigRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateConfigUpdatesSingleKey() {
        SystemConfig existing = new SystemConfig();
        existing.setKey("TIMEOUT");
        existing.setValue("30");
        when(systemConfigRepository.findById("TIMEOUT")).thenReturn(Optional.of(existing));
        when(systemConfigRepository.save(any(SystemConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        SystemConfigResponseDTO res = systemConfigService.updateConfig("TIMEOUT", "60");
        assertEquals("60", res.getValue());
    }

    @Test
    void getIntFallsBackOnInvalidNumber() {
        SystemConfig cfg = new SystemConfig();
        cfg.setKey("MAX_RETRIES");
        cfg.setValue("not-a-number");
        when(systemConfigRepository.findById("MAX_RETRIES")).thenReturn(Optional.of(cfg));

        assertEquals(3, systemConfigService.getInt("MAX_RETRIES", 3));
    }

    @Test
    void getBooleanParsesStoredValue() {
        SystemConfig cfg = new SystemConfig();
        cfg.setKey("FEATURE_X");
        cfg.setValue("true");
        when(systemConfigRepository.findById("FEATURE_X")).thenReturn(Optional.of(cfg));

        assertTrue(systemConfigService.getBoolean("FEATURE_X", false));
    }
}
