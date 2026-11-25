package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.CandidateDetailResponse;
import fptu.sba301.ats.dto.response.CandidateDocumentResponse;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.repository.projection.CandidateDocumentProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateHelpersTest {

    private final CandidateMapper mapper = new CandidateMapper();

    @Test
    @DisplayName("toCandidateDetailResponse maps Candidate entity and documents accurately")
    void toCandidateDetailResponse_fromEntity() {
        UUID candidateId = UUID.randomUUID();
        Candidate candidate = Candidate.builder()
                .id(candidateId)
                .fullName("Nguyen Van A")
                .email("nguyenvana@example.com")
                .phone("+84901234567")
                .currentCompany("Tech Corp")
                .location("Hanoi")
                .source("LinkedIn")
                .experienceYears(5)
                .summary("Senior Java developer")
                .build();

        UUID docId = UUID.randomUUID();
        CandidateDocumentResponse doc = CandidateDocumentResponse.builder()
                .documentId(docId)
                .fileName("cv.pdf")
                .fileUrl("https://cloudinary.com/cv.pdf")
                .fileType("pdf")
                .fileSizeBytes(1024L)
                .uploadedAt(LocalDateTime.now())
                .build();

        CandidateDetailResponse detail = mapper.toCandidateDetailResponse(candidate, List.of(doc));

        assertThat(detail.getCandidateId()).isEqualTo(candidateId);
        assertThat(detail.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(detail.getEmail()).isEqualTo("nguyenvana@example.com");
        assertThat(detail.getCurrentCompany()).isEqualTo("Tech Corp");
        assertThat(detail.getLocation()).isEqualTo("Hanoi");
        assertThat(detail.getExperienceYears()).isEqualTo(5);
        assertThat(detail.getDocuments()).hasSize(1);
        assertThat(detail.getDocuments().get(0).getFileName()).isEqualTo("cv.pdf");
    }

    @Test
    @DisplayName("toDocumentResponse maps projection to response")
    void toDocumentResponse_fromProjection() {
        UUID docId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CandidateDocumentProjection projection = new CandidateDocumentProjection() {
            @Override
            public String getDocumentId() {
                return docId.toString();
            }

            @Override
            public String getFileName() {
                return "resume.docx";
            }

            @Override
            public String getFileUrl() {
                return "https://res.cloudinary.com/ats/resume.docx";
            }

            @Override
            public String getFileType() {
                return "docx";
            }

            @Override
            public Long getFileSizeBytes() {
                return 2048L;
            }

            @Override
            public LocalDateTime getUploadedAt() {
                return now;
            }
        };

        CandidateDocumentResponse response = mapper.toDocumentResponse(projection);
        assertThat(response.getDocumentId()).isEqualTo(docId);
        assertThat(response.getFileName()).isEqualTo("resume.docx");
        assertThat(response.getFileUrl()).isEqualTo("https://res.cloudinary.com/ats/resume.docx");
        assertThat(response.getFileSizeBytes()).isEqualTo(2048L);
        assertThat(response.getUploadedAt()).isEqualTo(now);
    }
}
