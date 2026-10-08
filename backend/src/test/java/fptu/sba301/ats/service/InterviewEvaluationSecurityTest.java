package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.CriterionScoreRequest;
import fptu.sba301.ats.dto.request.SubmitFeedbackRequest;
import fptu.sba301.ats.entity.*;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.*;
import fptu.sba301.ats.service.impl.InterviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewEvaluationSecurityTest {

    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private InterviewScoreRepository scoreRepository;
    @Mock
    private InterviewParticipantRepository participantRepository;
    @Mock
    private InterviewScoreRepository interviewScoreRepository;
    @Mock
    private ScorecardCriterionRepository criterionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private fptu.sba301.ats.service.EmailService emailService;

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private User interviewerA;
    private User interviewerB;
    private Interview interview;
    private ScorecardTemplate template;
    private ScorecardCriterion criterion1;

    @BeforeEach
    void setUp() {
        interviewerA = User.builder()
                .id(UUID.randomUUID())
                .email("interviewerA@ats.com")
                .role(Role.INTERVIEWER)
                .build();

        interviewerB = User.builder()
                .id(UUID.randomUUID())
                .email("interviewerB@ats.com")
                .role(Role.INTERVIEWER)
                .build();

        criterion1 = ScorecardCriterion.builder()
                .id(UUID.randomUUID())
                .name("Technical Knowledge")
                .weight(new BigDecimal("1.00"))
                .build();

        template = ScorecardTemplate.builder()
                .id(UUID.randomUUID())
                .name("Tech Screen")
                .build();

        interview = Interview.builder()
                .id(UUID.randomUUID())
                .status(InterviewStatus.SCHEDULED)
                .template(template)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void testSubmitFeedback_NonParticipant_ThrowsForbidden() {
        when(userRepository.findByEmailAndDeletedFalse("interviewerB@ats.com")).thenReturn(Optional.of(interviewerB));
        when(participantRepository.findByInterviewIdAndUserId(interview.getId(), interviewerB.getId()))
                .thenReturn(Optional.empty());

        SubmitFeedbackRequest request = new SubmitFeedbackRequest();
        request.setInterviewId(interview.getId());
        request.setFeedback("Great");
        request.setScores(List.of(new CriterionScoreRequest(criterion1.getId(), 4)));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                interviewService.submitFeedback(request, "interviewerB@ats.com")
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void testSubmitFeedback_CriterionNotInTemplate_ThrowsBadRequest() {
        InterviewParticipant participant = InterviewParticipant.builder()
                .interview(interview)
                .user(interviewerA)
                .role(ParticipantRole.INTERVIEWER)
                .build();

        when(userRepository.findByEmailAndDeletedFalse("interviewerA@ats.com")).thenReturn(Optional.of(interviewerA));
        when(participantRepository.findByInterviewIdAndUserId(interview.getId(), interviewerA.getId()))
                .thenReturn(Optional.of(participant));
        when(criterionRepository.findByTemplateId(template.getId())).thenReturn(List.of(criterion1));

        UUID rogueCriterionId = UUID.randomUUID();
        SubmitFeedbackRequest request = new SubmitFeedbackRequest();
        request.setInterviewId(interview.getId());
        request.setFeedback("Rogue criterion");
        request.setScores(List.of(new CriterionScoreRequest(rogueCriterionId, 4)));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                interviewService.submitFeedback(request, "interviewerA@ats.com")
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void testSubmitFeedback_SingleInterviewerSubmits_DoesNotMarkCompletedIfSecondInterviewerPending() {
        InterviewParticipant partA = InterviewParticipant.builder()
                .interview(interview)
                .user(interviewerA)
                .role(ParticipantRole.INTERVIEWER)
                .build();

        InterviewParticipant partB = InterviewParticipant.builder()
                .interview(interview)
                .user(interviewerB)
                .role(ParticipantRole.INTERVIEWER)
                .submittedAt(null)
                .overallScore(null)
                .build();

        when(userRepository.findByEmailAndDeletedFalse("interviewerA@ats.com")).thenReturn(Optional.of(interviewerA));
        when(participantRepository.findByInterviewIdAndUserId(interview.getId(), interviewerA.getId()))
                .thenReturn(Optional.of(partA));
        when(criterionRepository.findByTemplateId(template.getId())).thenReturn(List.of(criterion1));
        when(criterionRepository.findById(criterion1.getId())).thenReturn(Optional.of(criterion1));
        when(participantRepository.findByIdInterviewId(interview.getId())).thenReturn(List.of(partA, partB));

        SubmitFeedbackRequest request = new SubmitFeedbackRequest();
        request.setInterviewId(interview.getId());
        request.setFeedback("Done A");
        request.setScores(List.of(new CriterionScoreRequest(criterion1.getId(), 4)));

        interviewService.submitFeedback(request, "interviewerA@ats.com");

        // Verify status remains SCHEDULED because interviewer B hasn't submitted yet
        assertEquals(InterviewStatus.SCHEDULED, interview.getStatus());
    }
}
