package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.event.SystemEvent;
import fptu.sba301.ats.entity.SystemConfig;
import fptu.sba301.ats.repository.SystemConfigRepository;
import fptu.sba301.ats.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import fptu.sba301.ats.dto.response.SystemConfigResponseDTO;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @SuppressWarnings("null")
    public Page<SystemConfigResponseDTO> getAllConfigs(Pageable pageable) {
        return systemConfigRepository.findAll(pageable)
                .map(this::toDTO);
    }

    @Override
    @Transactional
    @LogAudit(action = "UPDATE_ALL", resource = "SystemConfig")
    public void updateConfigs(Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }

        configs.forEach(this::updateSingleConfig);

        eventPublisher.publishEvent(new SystemEvent(this,
                "System Configuration Updated",
                "Global system settings have been modified.",
                "system",
                "/system-config"));
    }

    @Override
    @Transactional
    @LogAudit(action = "UPDATE", resource = "SystemConfig")
    public SystemConfigResponseDTO updateConfig(String key, String value) {
        SystemConfig config = updateSingleConfig(key, value);
        return toDTO(config);
    }

    private SystemConfig updateSingleConfig(String key, String value) {
        SystemConfig config = systemConfigRepository.findById(key)
                .orElseGet(() -> {
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setKey(key);
                    return newConfig;
                });
        config.setValue(value);
        config.setUpdatedAt(Instant.now());
        return systemConfigRepository.save(config);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getValue)
                .orElse(defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String val = getString(key, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String val = getString(key, null);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    private SystemConfigResponseDTO toDTO(SystemConfig config) {
        return SystemConfigResponseDTO.builder()
                .configKey(config.getKey())
                .value(config.getValue())
                .updatedAt(config.getUpdatedAt())
                .updatedBy(config.getUpdatedBy() != null ? config.getUpdatedBy().toString() : null)
                .build();
    }
}
