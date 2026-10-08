package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.CandidateEvaluationResponse;
import fptu.sba301.ats.dto.response.CandidateRankingResponse;
import fptu.sba301.ats.dto.response.InterviewScorecardResponse;
import fptu.sba301.ats.entity.*;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.*;
import fptu.sba301.ats.service.EvaluationService;
import fptu.sba301.ats.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewService interviewService;

    @Override
    public CandidateEvaluationResponse getEvaluationSummary(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("Application not found", HttpStatus.NOT_FOUND));

        List<Interview> interviews = interviewRepository.findByApplicationId(applicationId);

        List<CandidateEvaluationResponse.InterviewEvaluation> interviewEvals = interviews.stream()
                .map(interview -> {
                    List<InterviewScorecardResponse> scorecards =
                            interviewService.getAllScorecards(interview.getId());

                    double avgScore = scorecards.stream()
                            .filter(sc -> sc.getOverallScore() != null)
                            .mapToDouble(sc -> sc.getOverallScore().doubleValue())
                            .average()
                            .orElse(0.0);

                    return CandidateEvaluationResponse.InterviewEvaluation.builder()
                            .interviewId(interview.getId())
                            .scheduledAt(interview.getScheduledAt())
                            .type(interview.getType() != null ? interview.getType().name() : null)
                            .status(interview.getStatus() != null ? interview.getStatus().name() : null)
                            .averageScore(avgScore)
                            .interviewerCount(scorecards.size())
                            .scorecards(scorecards)
                            .build();
                })
                .collect(Collectors.toList());

        double overallScore = interviewEvals.stream()
                .mapToDouble(CandidateEvaluationResponse.InterviewEvaluation::getAverageScore)
                .average()
                .orElse(0.0);

        return CandidateEvaluationResponse.builder()
                .applicationId(applicationId)
                .candidateId(application.getCandidate() != null ? application.getCandidate().getId() : null)
                .candidateName(application.getCandidate() != null ? application.getCandidate().getFullName() : null)
                .jobTitle(application.getJob() != null ? application.getJob().getTitle() : null)
                .overallScore(overallScore)
                .interviews(interviewEvals)
                .build();
    }

    @Override
    public List<CandidateRankingResponse> getCandidateRanking(UUID jobId) {
        List<Application> applications = applicationRepository.findByJobId(jobId);

        // Compute evaluation for each application
        List<CandidateRankingResponse> rankings = applications.stream()
                .map(app -> {
                    CandidateEvaluationResponse eval = getEvaluationSummary(app.getId());
                    return CandidateRankingResponse.builder()
                            .candidateId(app.getCandidate() != null ? app.getCandidate().getId() : null)
                            .candidateName(app.getCandidate() != null ? app.getCandidate().getFullName() : null)
                            .applicationId(app.getId())
                            .overallScore(eval.getOverallScore())
                            .experienceYears(app.getCandidate() != null ? app.getCandidate().getExperienceYears() : null)
                            .appliedAt(app.getAppliedAt())
                            .stage(app.getStage())
                            .build();
                })
                .sorted(Comparator
                        .comparingDouble(CandidateRankingResponse::getOverallScore).reversed()
                        .thenComparing((a, b) -> {
                            int expA = a.getExperienceYears() != null ? a.getExperienceYears() : 0;
                            int expB = b.getExperienceYears() != null ? b.getExperienceYears() : 0;
                            return Integer.compare(expB, expA); // DESC
                        })
                        .thenComparing(a -> a.getAppliedAt() != null ? a.getAppliedAt() : java.time.LocalDateTime.MAX)
                        .thenComparing(a -> a.getCandidateName() != null ? a.getCandidateName() : ""))
                .collect(Collectors.toList());

        // Assign ranks
        AtomicInteger rank = new AtomicInteger(1);
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(rank.getAndIncrement());
        }

        return rankings;
    }
}
