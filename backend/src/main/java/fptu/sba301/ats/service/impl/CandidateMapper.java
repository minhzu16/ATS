package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.BulkImportResponse;
import fptu.sba301.ats.dto.response.CandidateDetailResponse;
import fptu.sba301.ats.dto.response.CandidateDocumentResponse;
import fptu.sba301.ats.dto.response.CandidateHistoryResponse;
import fptu.sba301.ats.dto.response.CandidateListResponse;
import fptu.sba301.ats.dto.response.InterviewerOptionResponse;
import fptu.sba301.ats.dto.response.ScheduleCandidateInterviewsResponse;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.CandidateStageHistory;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.repository.projection.CandidateDetailProjection;
import fptu.sba301.ats.repository.projection.CandidateDocumentProjection;
import fptu.sba301.ats.repository.projection.CandidateListProjection;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

class CandidateMapper {

    public CandidateListResponse toCandidateListResponse(CandidateListProjection row) {
        return CandidateListResponse.builder()
                .candidateId(UUID.fromString(row.getCandidateId()))
                .fullName(row.getFullName())
                .email(row.getEmail())
                .jobTitle(row.getJobTitle())
                .stage(row.getStage())
                .rating(row.getRating())
                .appliedAt(row.getAppliedAt())
                .build();
    }

    public List<CandidateListResponse> toCandidateListResponses(List<CandidateListProjection> rows) {
        return rows.stream()
                .map(this::toCandidateListResponse)
                .collect(Collectors.toList());
    }

    public CandidateDocumentResponse toDocumentResponse(CandidateDocumentProjection document) {
        return CandidateDocumentResponse.builder()
                .documentId(UUID.fromString(document.getDocumentId()))
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .fileType(document.getFileType())
                .fileSizeBytes(document.getFileSizeBytes())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    public List<CandidateDocumentResponse> toDocumentResponses(List<CandidateDocumentProjection> documents) {
        return documents.stream()
                .map(this::toDocumentResponse)
                .collect(Collectors.toList());
    }

    public CandidateDetailResponse toCandidateDetailResponse(
            CandidateDetailProjection detail,
            List<CandidateDocumentResponse> documents
    ) {
        return CandidateDetailResponse.builder()
                .candidateId(UUID.fromString(detail.getCandidateId()))
                .applicationId(detail.getApplicationId() != null ? UUID.fromString(detail.getApplicationId()) : null)
                .fullName(detail.getFullName())
                .email(detail.getEmail())
                .phone(detail.getPhone())
                .currentCompany(detail.getCurrentCompany())
                .jobTitle(detail.getJobTitle())
                .stage(detail.getStage())
                .status(detail.getStatus())
                .rating(detail.getRating())
                .appliedAt(detail.getAppliedAt())
                .source(detail.getSource())
                .location(detail.getLocation())
                .experienceYears(detail.getExperienceYears())
                .summary(detail.getSummary())
                .documents(documents)
                .build();
    }

    public CandidateDetailResponse toCandidateDetailResponse(
            Candidate candidate,
            List<CandidateDocumentResponse> documents
    ) {
        return CandidateDetailResponse.builder()
                .candidateId(candidate.getId())
                .fullName(candidate.getFullName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .currentCompany(candidate.getCurrentCompany())
                .source(candidate.getSource())
                .location(candidate.getLocation())
                .experienceYears(candidate.getExperienceYears())
                .summary(candidate.getSummary())
                .documents(documents)
                .build();
    }

    public CandidateHistoryResponse toHistoryResponse(CandidateStageHistory h, String changedBy) {
        return CandidateHistoryResponse.builder()
                .id(h.getId())
                .fromStage(h.getFromStage())
                .toStage(h.getToStage())
                .changedBy(changedBy)
                .changedAt(h.getCreatedAt())
                .reason(h.getReason())
                .build();
    }

    public List<CandidateHistoryResponse> toHistoryResponses(
            List<CandidateStageHistory> histories,
            UserRepository userRepository
    ) {
        return histories.stream().map(h -> {
            String changedBy = "System";
            if (h.getCreatedBy() != null) {
                changedBy = userRepository.findById(h.getCreatedBy())
                        .map(User::getFullName)
                        .orElse("Unknown User");
            }
            return toHistoryResponse(h, changedBy);
        }).collect(Collectors.toList());
    }

    public InterviewerOptionResponse toInterviewerOptionResponse(User user) {
        return InterviewerOptionResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    public List<InterviewerOptionResponse> toInterviewerOptionResponses(List<User> users) {
        return users.stream()
                .filter(User::isActive)
                .map(this::toInterviewerOptionResponse)
                .collect(Collectors.toList());
    }

    public ScheduleCandidateInterviewsResponse toScheduleResponse(
            UUID candidateId,
            UUID applicationId,
            String stage,
            List<UUID> interviewIds
    ) {
        return ScheduleCandidateInterviewsResponse.builder()
                .candidateId(candidateId)
                .applicationId(applicationId)
                .stage(stage)
                .totalInterviewsCreated(interviewIds.size())
                .interviewIds(interviewIds)
                .build();
    }

    public BulkImportResponse toBulkImportResponse(
            int totalRows,
            int successCount,
            int failCount,
            List<String> errors
    ) {
        return BulkImportResponse.builder()
                .totalRows(totalRows)
                .successCount(successCount)
                .failCount(failCount)
                .errors(errors)
                .build();
    }
}
