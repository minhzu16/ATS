package fptu.sba301.ats.service;

import java.io.OutputStream;

public interface ExportService {
    void exportAuditLogsCsv(OutputStream outputStream);
    void exportAuditLogsExcel(OutputStream outputStream);
    void exportUsersCsv(OutputStream outputStream);
    void exportUsersExcel(OutputStream outputStream);
}
