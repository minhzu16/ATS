package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.CandidateEvaluationResponse;
import fptu.sba301.ats.dto.response.CandidateRankingResponse;

import java.util.List;
import java.util.UUID;

public interface EvaluationService {
    CandidateEvaluationResponse getEvaluationSummary(UUID applicationId);
    List<CandidateRankingResponse> getCandidateRanking(UUID jobId);
}
