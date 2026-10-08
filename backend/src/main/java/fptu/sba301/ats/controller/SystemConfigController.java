package fptu.sba301.ats.controller;

import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.constant.AdminConstants;
import fptu.sba301.ats.constant.AppConstant;
import fptu.sba301.ats.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import fptu.sba301.ats.dto.response.SystemConfigResponseDTO;

import java.util.Map;

@RestController
@RequestMapping(AppConstant.BASE_URL + AdminConstants.SYSTEM_CONFIG_URL)
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<SystemConfigResponseDTO>> getAllConfigs(Pageable pageable) {
        return ResponseEntity.ok(systemConfigService.getAllConfigs(pageable));
    }

    @PutMapping
    @LogAudit(action = "UPDATE_ALL", resource = "SystemConfig")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> updateConfigs(@RequestBody Map<String, String> configs) {
        systemConfigService.updateConfigs(configs);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @LogAudit(action = "UPDATE", resource = "SystemConfig")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SystemConfigResponseDTO> updateConfig(@RequestBody Map<String, String> payload) {
        String key = payload.get("configKey");
        String value = payload.get("value");
        return ResponseEntity.ok(systemConfigService.updateConfig(key, value));
    }
}
