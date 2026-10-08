package fptu.sba301.ats.controller;

import fptu.sba301.ats.constant.AdminConstants;
import fptu.sba301.ats.constant.AppConstant;
import fptu.sba301.ats.dto.response.AuditLogResponseDTO;
import fptu.sba301.ats.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping(AppConstant.BASE_URL + AdminConstants.AUDIT_LOG_URL)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponseDTO>> getAllLogs(Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAllLogs(pageable));
    }
    
    @GetMapping("/action")
    public ResponseEntity<Page<AuditLogResponseDTO>> getLogsByAction(@RequestParam String action, Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getLogsByAction(action, pageable));
    }
}
