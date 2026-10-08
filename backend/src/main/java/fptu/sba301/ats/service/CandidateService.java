package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.CreateCandidateRequest;
import fptu.sba301.ats.dto.request.ScheduleCandidateInterviewsRequest;
import fptu.sba301.ats.dto.response.BulkImportResponse;
import fptu.sba301.ats.dto.response.CandidateDetailResponse;
import fptu.sba301.ats.dto.response.CandidateHistoryResponse;
import fptu.sba301.ats.dto.response.InterviewerOptionResponse;
import fptu.sba301.ats.dto.response.CandidateListResponse;
import fptu.sba301.ats.dto.response.ScheduleCandidateInterviewsResponse;
import fptu.sba301.ats.enums.ApplicationStage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CandidateService {
    List<CandidateListResponse> getCandidateList();
    CandidateDetailResponse getCandidateDetail(UUID candidateId);
    CandidateDetailResponse updateCandidateStage(UUID candidateId, ApplicationStage targetStage);
    CandidateDetailResponse updateCandidateStage(UUID candidateId, ApplicationStage targetStage, String reason);
    CandidateDetailResponse startScreening(UUID candidateId);
    CandidateDetailResponse withdrawCandidate(UUID candidateId, String reason);
    CandidateDetailResponse addApplicationForCandidate(UUID candidateId, UUID jobId);

    CandidateDetailResponse createCandidate(CreateCandidateRequest request, List<MultipartFile> documents);

    BulkImportResponse importCandidatesFromCsv(MultipartFile csvFile, List<MultipartFile> cvFiles);

    List<CandidateHistoryResponse> getStageHistory(UUID candidateId);

    List<InterviewerOptionResponse> getInterviewerOptions();

    ScheduleCandidateInterviewsResponse scheduleCandidateInterviews(UUID candidateId, ScheduleCandidateInterviewsRequest request);
}
