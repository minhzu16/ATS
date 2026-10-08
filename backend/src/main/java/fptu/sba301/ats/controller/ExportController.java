package fptu.sba301.ats.controller;

import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.constant.AdminConstants;
import fptu.sba301.ats.constant.AppConstant;
import fptu.sba301.ats.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstant.BASE_URL + AdminConstants.EXPORT_URL)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/audit-logs/csv")
    @LogAudit(action = "EXPORT_AUDIT_LOGS_CSV", resource = "AUDIT_LOG")
    public void exportAuditLogsCsv(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"audit_logs.csv\"");
        exportService.exportAuditLogsCsv(response.getOutputStream());
    }

    @GetMapping("/audit-logs/excel")
    @LogAudit(action = "EXPORT_AUDIT_LOGS_EXCEL", resource = "AUDIT_LOG")
    public void exportAuditLogsExcel(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"audit_logs.xlsx\"");
        exportService.exportAuditLogsExcel(response.getOutputStream());
    }

    @GetMapping("/users/csv")
    @LogAudit(action = "EXPORT_USERS_CSV", resource = "USER")
    public void exportUsersCsv(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"users.csv\"");
        exportService.exportUsersCsv(response.getOutputStream());
    }

    @GetMapping("/users/excel")
    @LogAudit(action = "EXPORT_USERS_EXCEL", resource = "USER")
    public void exportUsersExcel(HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"users.xlsx\"");
        exportService.exportUsersExcel(response.getOutputStream());
    }
}
