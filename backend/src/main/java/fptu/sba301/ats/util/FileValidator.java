package fptu.sba301.ats.util;

import fptu.sba301.ats.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class FileValidator {

    private static final long MAX_DOCUMENT_SIZE = 10L * 1024 * 1024; // 10MB
    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024; // 5MB

    private static final List<String> ALLOWED_DOC_EXTENSIONS = List.of("pdf", "docx");
    private static final List<String> ALLOWED_AVATAR_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    private FileValidator() {}

    public static void validateDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Document file cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > MAX_DOCUMENT_SIZE) {
            throw new BusinessException("Document file size exceeds 10MB limit", HttpStatus.PAYLOAD_TOO_LARGE);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_DOC_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Only PDF and DOCX files are allowed for candidate documents", HttpStatus.BAD_REQUEST);
        }

        validateMagicBytes(file, extension);
    }

    public static void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Avatar file cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("Avatar file size exceeds 5MB limit", HttpStatus.PAYLOAD_TOO_LARGE);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_AVATAR_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Only JPG, PNG, and WEBP files are allowed for avatars", HttpStatus.BAD_REQUEST);
        }

        validateMagicBytes(file, extension);
    }

    private static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT).trim();
    }

    private static void validateMagicBytes(MultipartFile file, String extension) {
        byte[] header = new byte[12];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                throw new BusinessException("File header is corrupted or incomplete", HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            throw new BusinessException("Failed to read file content for validation", HttpStatus.BAD_REQUEST);
        }

        boolean valid = switch (extension) {
            case "pdf" -> header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46; // %PDF
            case "docx" -> header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04; // PK.. (ZIP)
            case "jpg", "jpeg" -> (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF; // JPEG
            case "png" -> (header[0] & 0xFF) == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47; // PNG
            case "webp" -> header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'; // RIFF
            default -> false;
        };

        if (!valid) {
            throw new BusinessException("File signature does not match declared extension ." + extension, HttpStatus.BAD_REQUEST);
        }
    }
}
