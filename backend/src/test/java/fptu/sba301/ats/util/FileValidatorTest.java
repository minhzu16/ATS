package fptu.sba301.ats.util;

import fptu.sba301.ats.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileValidatorTest {

    @Test
    void testValidateDocument_ValidPdf_Success() {
        byte[] pdfBytes = "%PDF-1.4 test content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfBytes
        );
        assertDoesNotThrow(() -> FileValidator.validateDocument(file));
    }

    @Test
    void testValidateDocument_ValidDocx_Success() {
        byte[] docxBytes = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x06, 0x00};
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes
        );
        assertDoesNotThrow(() -> FileValidator.validateDocument(file));
    }

    @Test
    void testValidateDocument_DisallowedExtension_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.sh", "text/plain", "#!/bin/bash echo bad".getBytes()
        );
        assertThrows(BusinessException.class, () -> FileValidator.validateDocument(file));
    }

    @Test
    void testValidateDocument_FakePdfExtension_MagicBytesMismatch_ThrowsException() {
        byte[] fakePdfBytes = new byte[]{0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00}; // Windows executable header (MZ)
        MockMultipartFile file = new MockMultipartFile(
                "file", "malicious.pdf", "application/pdf", fakePdfBytes
        );
        assertThrows(BusinessException.class, () -> FileValidator.validateDocument(file));
    }

    @Test
    void testValidateAvatar_ValidPng_Success() {
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", pngBytes
        );
        assertDoesNotThrow(() -> FileValidator.validateAvatar(file));
    }

    @Test
    void testValidateAvatar_InvalidType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.exe", "application/octet-stream", "evil".getBytes()
        );
        assertThrows(BusinessException.class, () -> FileValidator.validateAvatar(file));
    }
}
