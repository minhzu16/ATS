package fptu.sba301.ats.service.impl;

import com.opencsv.CSVWriter;
import fptu.sba301.ats.entity.AuditLog;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.repository.AuditLogRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    private String sanitize(String val) {
        if (val == null) return "";
        if (val.startsWith("=") || val.startsWith("+") || val.startsWith("-") || val.startsWith("@") || val.startsWith("\t") || val.startsWith("\r")) {
            return "'" + val;
        }
        return val;
    }

    @Override
    public void exportAuditLogsCsv(OutputStream outputStream) {
        log.info("Exporting Audit Logs to CSV");
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            List<AuditLog> logs = auditLogRepository.findAll();
            
            // Write BOM for Excel compatibility
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);

            String[] header = {"ID", "User ID", "Action", "Entity Type", "Entity ID", "Old Value", "New Value", "IP Address", "User Agent", "Created At"};
            writer.writeNext(header);

            for (AuditLog logRecord : logs) {
                String[] data = {
                        sanitize(String.valueOf(logRecord.getId())),
                        sanitize(logRecord.getUserId() != null ? logRecord.getUserId().toString() : ""),
                        sanitize(logRecord.getAction()),
                        sanitize(logRecord.getEntityType()),
                        sanitize(logRecord.getEntityId()),
                        sanitize(logRecord.getOldValue()),
                        sanitize(logRecord.getNewValue()),
                        sanitize(logRecord.getIpAddress()),
                        sanitize(logRecord.getUserAgent()),
                        sanitize(logRecord.getCreatedAt() != null ? logRecord.getCreatedAt().toString() : "")
                };
                writer.writeNext(data);
            }
            log.info("Finished writing Audit Logs to CSV");
        } catch (Exception e) {
            log.error("Error exporting Audit Logs to CSV", e);
            throw new RuntimeException("Error exporting Audit Logs to CSV", e);
        }
    }

    @Override
    public void exportAuditLogsExcel(OutputStream outputStream) {
        log.info("Exporting Audit Logs to Excel");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Audit Logs");
            List<AuditLog> logs = auditLogRepository.findAll();

            String[] header = {"ID", "User ID", "Action", "Entity Type", "Entity ID", "Old Value", "New Value", "IP Address", "User Agent", "Created At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(header[i]);
            }

            int rowNum = 1;
            for (AuditLog logRecord : logs) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(sanitize(logRecord.getId() != null ? String.valueOf(logRecord.getId()) : ""));
                row.createCell(1).setCellValue(sanitize(logRecord.getUserId() != null ? logRecord.getUserId().toString() : ""));
                row.createCell(2).setCellValue(sanitize(logRecord.getAction() != null ? logRecord.getAction() : ""));
                row.createCell(3).setCellValue(sanitize(logRecord.getEntityType() != null ? logRecord.getEntityType() : ""));
                row.createCell(4).setCellValue(sanitize(logRecord.getEntityId() != null ? logRecord.getEntityId() : ""));
                row.createCell(5).setCellValue(sanitize(logRecord.getOldValue() != null ? logRecord.getOldValue() : ""));
                row.createCell(6).setCellValue(sanitize(logRecord.getNewValue() != null ? logRecord.getNewValue() : ""));
                row.createCell(7).setCellValue(sanitize(logRecord.getIpAddress() != null ? logRecord.getIpAddress() : ""));
                row.createCell(8).setCellValue(sanitize(logRecord.getUserAgent() != null ? logRecord.getUserAgent() : ""));
                row.createCell(9).setCellValue(sanitize(logRecord.getCreatedAt() != null ? logRecord.getCreatedAt().toString() : ""));
            }

            log.info("Finished creating Audit Logs Excel workbook, writing to stream");
            workbook.write(outputStream);
            log.info("Finished writing Audit Logs to Excel outputStream");
        } catch (Exception e) {
            log.error("Error exporting Audit Logs to Excel", e);
            throw new RuntimeException("Error exporting Audit Logs to Excel", e);
        }
    }

    @Override
    public void exportUsersCsv(OutputStream outputStream) {
        log.info("Exporting Users to CSV");
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            List<User> users = userRepository.findAll();
            
            // Write BOM for Excel compatibility
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);

            String[] header = {"ID", "Email", "Full Name", "Role", "Department", "Active", "Locked", "Created At"};
            writer.writeNext(header);

            for (User user : users) {
                String[] data = {
                        sanitize(user.getId() != null ? user.getId().toString() : ""),
                        sanitize(user.getEmail()),
                        sanitize(user.getFullName()),
                        sanitize(user.getRole() != null ? user.getRole().name() : ""),
                        sanitize((user.getDepartment() != null && user.getDepartment().getName() != null) ? user.getDepartment().getName() : ""),
                        String.valueOf(user.isActive()),
                        String.valueOf(user.isAccountLocked()),
                        sanitize(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "")
                };
                writer.writeNext(data);
            }
            log.info("Finished writing Users to CSV");
        } catch (Exception e) {
            log.error("Error exporting Users to CSV", e);
            throw new RuntimeException("Error exporting Users to CSV", e);
        }
    }

    @Override
    public void exportUsersExcel(OutputStream outputStream) {
        log.info("Exporting Users to Excel");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");
            List<User> users = userRepository.findAll();

            String[] header = {"ID", "Email", "Full Name", "Role", "Department", "Active", "Locked", "Created At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(header[i]);
            }

            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(sanitize(user.getId() != null ? user.getId().toString() : ""));
                row.createCell(1).setCellValue(sanitize(user.getEmail() != null ? user.getEmail() : ""));
                row.createCell(2).setCellValue(sanitize(user.getFullName() != null ? user.getFullName() : ""));
                row.createCell(3).setCellValue(sanitize(user.getRole() != null ? user.getRole().name() : ""));
                row.createCell(4).setCellValue(sanitize((user.getDepartment() != null && user.getDepartment().getName() != null) ? user.getDepartment().getName() : ""));
                row.createCell(5).setCellValue(user.isActive() ? "Yes" : "No");
                row.createCell(6).setCellValue(user.isAccountLocked() ? "Yes" : "No");
                row.createCell(7).setCellValue(sanitize(user.getCreatedAt() != null ? user.getCreatedAt().toString() : ""));
            }

            log.info("Finished creating Users Excel workbook, writing to stream");
            workbook.write(outputStream);
            log.info("Finished writing Users to Excel outputStream");
        } catch (Exception e) {
            log.error("Error exporting Users to Excel", e);
            throw new RuntimeException("Error exporting Users to Excel", e);
        }
    }
}
