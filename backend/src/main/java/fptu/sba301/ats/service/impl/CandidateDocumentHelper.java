package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.CandidateDocumentResponse;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.CandidateDocument;
import fptu.sba301.ats.repository.CandidateDocumentRepository;
import fptu.sba301.ats.repository.CandidateRepository;
import fptu.sba301.ats.service.CloudinaryService;
import fptu.sba301.ats.util.FileValidator;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class CandidateDocumentHelper {

    private static final String CLOUDINARY_FOLDER = "candidate_documents";
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public void uploadAndSaveDocuments(
            Candidate candidate,
            List<MultipartFile> files,
            CloudinaryService cloudinaryService,
            CandidateDocumentRepository candidateDocumentRepository
    ) {
        List<CandidateDocument> docs = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            FileValidator.validateDocument(file);
            Map<String, Object> result = cloudinaryService.uploadFile(file, CLOUDINARY_FOLDER);

            String fileUrl = (String) result.get("secure_url");
            String format = (String) result.get("format");
            Object bytesObj = result.get("bytes");
            Long fileSizeBytes = bytesObj != null ? Long.parseLong(bytesObj.toString()) : null;
            String originalFilename = file.getOriginalFilename();

            docs.add(CandidateDocument.builder()
                    .candidate(candidate)
                    .fileName(originalFilename != null ? originalFilename : "document")
                    .fileUrl(fileUrl)
                    .fileType(format)
                    .fileSizeBytes(fileSizeBytes)
                    .uploadedAt(LocalDateTime.now(APP_ZONE))
                    .build());
        }
        candidateDocumentRepository.saveAll(docs);
    }

    public List<CandidateDocumentResponse> findDocumentsByCandidateId(
            CandidateRepository candidateRepository,
            CandidateMapper mapper,
            UUID candidateId
    ) {
        return mapper.toDocumentResponses(
                candidateRepository.findDocumentsByCandidateId(candidateId.toString())
        );
    }
}
