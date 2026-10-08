package fptu.sba301.ats.service;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.CandidateStageHistoryRepository;
import fptu.sba301.ats.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StageTransitionStateMachineTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private CandidateStageHistoryRepository candidateStageHistoryRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationStageTransitionService transitionService;

    private Application application;

    @BeforeEach
    void setUp() {
        Job job = Job.builder()
                .id(UUID.randomUUID())
                .title("Software Engineer")
                .headcount(2)
                .build();

        Candidate candidate = Candidate.builder()
                .id(UUID.randomUUID())
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        application = Application.builder()
                .id(UUID.randomUUID())
                .candidate(candidate)
                .job(job)
                .stage(ApplicationStage.APPLIED)
                .status(ApplicationStatus.ACTIVE)
                .build();
    }

    @Test
    void testValidTransition_AppliedToScreening_Success() {
        transitionService.transition(application, ApplicationStage.SCREENING);
        assertEquals(ApplicationStage.SCREENING, application.getStage());
        verify(applicationRepository, times(1)).save(application);
        verify(candidateStageHistoryRepository, times(1)).save(any());
    }

    @Test
    void testInvalidTransition_AppliedToOffer_ThrowsConflict() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transitionService.transition(application, ApplicationStage.OFFER)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void testInvalidTransition_RejectedToHired_ThrowsConflict() {
        application.setStage(ApplicationStage.REJECTED);
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transitionService.transition(application, ApplicationStage.HIRED)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void testRejectCandidate_WithoutReason_ThrowsBadRequest() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transitionService.transition(application, ApplicationStage.REJECTED, "   ")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testRejectCandidate_WithReason_Success() {
        transitionService.transition(application, ApplicationStage.REJECTED, "Skill gap in React");
        assertEquals(ApplicationStage.REJECTED, application.getStage());
        assertEquals(ApplicationStatus.REJECTED, application.getStatus());
    }

    @Test
    void testWithdraw_WhenAlreadyHired_ThrowsConflict() {
        application.setStage(ApplicationStage.HIRED);
        BusinessException ex = assertThrows(BusinessException.class, () ->
                transitionService.withdraw(application, "Candidate took other job")
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void testWithdraw_ActiveCandidate_Success() {
        transitionService.withdraw(application, "Personal reasons");
        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
        verify(candidateStageHistoryRepository, times(1)).save(any());
    }
}
