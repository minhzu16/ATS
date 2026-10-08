package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.SystemConfigResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;

public interface SystemConfigService {
    Page<SystemConfigResponseDTO> getAllConfigs(Pageable pageable);

    void updateConfigs(Map<String, String> configs);

    SystemConfigResponseDTO updateConfig(String key, String value);
    
    String getString(String key, String defaultValue);
    int getInt(String key, int defaultValue);
    boolean getBoolean(String key, boolean defaultValue);
}

