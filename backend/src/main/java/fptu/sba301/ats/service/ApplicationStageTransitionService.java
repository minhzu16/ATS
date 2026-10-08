package fptu.sba301.ats.service;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.CandidateStageHistory;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.CandidateStageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

import static fptu.sba301.ats.enums.ApplicationStage.*;

@Service
@RequiredArgsConstructor
public class ApplicationStageTransitionService {

    private final ApplicationRepository applicationRepository;
    private final CandidateStageHistoryRepository candidateStageHistoryRepository;
    private final fptu.sba301.ats.repository.JobRepository jobRepository;

    private static final Map<ApplicationStage, Set<ApplicationStage>> ALLOWED_TRANSITIONS = Map.of(
            APPLIED,   Set.of(SCREENING, REJECTED),
            SCREENING, Set.of(INTERVIEW, REJECTED),
            INTERVIEW, Set.of(OFFER, REJECTED),
            OFFER,     Set.of(HIRED, REJECTED),
            HIRED,     Set.of(),
            REJECTED,  Set.of()
    );

    @Transactional
    public void transition(Application application, ApplicationStage toStage) {
        transition(application, toStage, null);
    }

    @Transactional
    public void transition(Application application, ApplicationStage toStage, String reason) {
        ApplicationStage fromStage = application.getStage();
        if (fromStage == toStage) {
            return;
        }

        Set<ApplicationStage> allowed = ALLOWED_TRANSITIONS.getOrDefault(fromStage, Set.of());
        if (!allowed.contains(toStage)) {
            throw new BusinessException(
                    "Invalid state transition from " + fromStage + " to " + toStage,
                    HttpStatus.CONFLICT
            );
        }

        if (toStage == REJECTED) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new BusinessException("Rejection reason is required when rejecting a candidate", HttpStatus.BAD_REQUEST);
            }
            application.setStatus(ApplicationStatus.REJECTED);
        } else if (toStage == HIRED) {
            application.setStatus(ApplicationStatus.ACTIVE);
            if (application.getJob() != null) {
                fptu.sba301.ats.entity.Job job = application.getJob();
                long currentHired = applicationRepository.countByJob_IdAndStage(job.getId(), HIRED);
                // currentHired will become currentHired + 1 once this application is saved
                if (job.getHeadcount() != null && (currentHired + 1) >= job.getHeadcount()) {
                    job.setStatus(fptu.sba301.ats.enums.JobStatus.CLOSED);
                    jobRepository.save(job);
                }
            }
        }

        application.setStage(toStage);
        applicationRepository.save(application);

        candidateStageHistoryRepository.save(CandidateStageHistory.builder()
                .application(application)
                .fromStage(fromStage)
                .toStage(toStage)
                .reason(reason)
                .build());
    }

    @Transactional
    public void withdraw(Application application, String reason) {
        if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
            return;
        }
        if (application.getStage() == HIRED || application.getStage() == REJECTED) {
            throw new BusinessException("Cannot withdraw an application that is already " + application.getStage(), HttpStatus.CONFLICT);
        }

        ApplicationStage fromStage = application.getStage();
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);

        candidateStageHistoryRepository.save(CandidateStageHistory.builder()
                .application(application)
                .fromStage(fromStage)
                .toStage(fromStage)
                .reason(reason != null ? reason : "Candidate withdrew application")
                .build());
    }
}
