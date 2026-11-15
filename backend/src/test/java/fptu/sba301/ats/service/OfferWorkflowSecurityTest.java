package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.CreateOfferRequest;
import fptu.sba301.ats.dto.request.OfferApprovalRequest;
import fptu.sba301.ats.dto.response.OfferResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.Offer;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.*;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.OfferApprovalRepository;
import fptu.sba301.ats.repository.OfferRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.impl.OfferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferWorkflowSecurityTest {

    @Mock
    private OfferRepository offerRepository;
    @Mock
    private OfferApprovalRepository approvalRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationStageTransitionService applicationStageTransitionService;

    @InjectMocks
    private OfferServiceImpl offerService;

    private User managerUser;
    private Offer offer;
    private Application application;

    @BeforeEach
    void setUp() {
        UUID managerId = UUID.randomUUID();
        managerUser = User.builder()
                .id(managerId)
                .email("hrmanager@ats.com")
                .role(Role.HR_MANAGER)
                .build();

        application = Application.builder()
                .id(UUID.randomUUID())
                .stage(ApplicationStage.OFFER)
                .build();

        offer = Offer.builder()
                .id(UUID.randomUUID())
                .application(application)
                .status(OfferStatus.PENDING_APPROVAL)
                .createdBy(managerId) // Same ID as manager to test separation of duties
                .build();
    }

    @Test
    void testApproveOrReject_CreatorApprovingOwnOffer_ThrowsForbidden() {
        when(userRepository.findByEmailAndDeletedFalse("hrmanager@ats.com")).thenReturn(Optional.of(managerUser));
        when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));

        OfferApprovalRequest req = OfferApprovalRequest.builder()
                .status(ApprovalStatus.APPROVED)
                .comment("Self-approving")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                offerService.approveOrReject(offer.getId(), req, "hrmanager@ats.com")
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void testApproveOrReject_DifferentApprover_ApprovedDoesNotPrematurelyHire() {
        UUID otherManagerId = UUID.randomUUID();
        User otherManager = User.builder()
                .id(otherManagerId)
                .email("other_manager@ats.com")
                .role(Role.HR_MANAGER)
                .build();

        when(userRepository.findByEmailAndDeletedFalse("other_manager@ats.com")).thenReturn(Optional.of(otherManager));
        when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
        when(approvalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OfferApprovalRequest req = OfferApprovalRequest.builder()
                .status(ApprovalStatus.APPROVED)
                .comment("Approved")
                .build();

        offerService.approveOrReject(offer.getId(), req, "other_manager@ats.com");

        assertEquals(OfferStatus.APPROVED, offer.getStatus());
        // Verify application was NOT transitioned to HIRED prematurely
        verify(applicationStageTransitionService, never()).transition(application, ApplicationStage.HIRED);
    }

    @Test
    void testAcceptOffer_TransitionsApplicationToHired() {
        offer.setStatus(OfferStatus.SENT);
        when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
        when(offerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        offerService.acceptOffer(offer.getId());

        assertEquals(OfferStatus.ACCEPTED, offer.getStatus());
        verify(applicationStageTransitionService, times(1)).transition(application, ApplicationStage.HIRED);
    }

    @Test
    void testDeclineOffer_TransitionsApplicationToRejectedWithReason() {
        offer.setStatus(OfferStatus.SENT);
        when(offerRepository.findById(offer.getId())).thenReturn(Optional.of(offer));
        when(offerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        offerService.declineOffer(offer.getId(), "Salary too low");

        assertEquals(OfferStatus.DECLINED, offer.getStatus());
        verify(applicationStageTransitionService, times(1)).transition(application, ApplicationStage.REJECTED, "Salary too low");
    }

    @Test
    void testCreateDraft_RequisitionNotApproved_ThrowsConflict() {
        Job unapprovedJob = Job.builder()
                .id(UUID.randomUUID())
                .title("Senior Backend Engineer")
                .status(JobStatus.PENDING_APPROVAL)
                .headcount(2)
                .build();

        Application interviewApp = Application.builder()
                .id(UUID.randomUUID())
                .stage(ApplicationStage.INTERVIEW)
                .status(ApplicationStatus.ACTIVE)
                .job(unapprovedJob)
                .build();

        UUID candidateId = UUID.randomUUID();
        when(applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE))
                .thenReturn(Optional.of(interviewApp));

        CreateOfferRequest req = CreateOfferRequest.builder()
                .candidateId(candidateId)
                .salary(new BigDecimal("35000000"))
                .positionTitle("Senior Backend Engineer")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                offerService.createDraft(req)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("not approved"));
    }

    @Test
    void testCreateDraft_HeadcountLimitReached_ThrowsConflict() {
        Job approvedJob = Job.builder()
                .id(UUID.randomUUID())
                .title("Product Manager")
                .status(JobStatus.APPROVED)
                .headcount(1)
                .build();

        Application interviewApp = Application.builder()
                .id(UUID.randomUUID())
                .stage(ApplicationStage.INTERVIEW)
                .status(ApplicationStatus.ACTIVE)
                .job(approvedJob)
                .build();

        UUID candidateId = UUID.randomUUID();
        when(applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE))
                .thenReturn(Optional.of(interviewApp));
        when(offerRepository.countByJobIdAndStatusIn(eq(approvedJob.getId()), any()))
                .thenReturn(1L); // 1 active offer exists, headcount is 1

        CreateOfferRequest req = CreateOfferRequest.builder()
                .candidateId(candidateId)
                .salary(new BigDecimal("40000000"))
                .positionTitle("Product Manager")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                offerService.createDraft(req)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        assertTrue(ex.getMessage().contains("headcount limit"));
    }

    @Test
    void testCreateDraft_ApprovedRequisitionWithinHeadcount_Success() {
        Job approvedJob = Job.builder()
                .id(UUID.randomUUID())
                .title("Frontend Developer")
                .status(JobStatus.APPROVED)
                .headcount(3)
                .build();

        Application interviewApp = Application.builder()
                .id(UUID.randomUUID())
                .stage(ApplicationStage.INTERVIEW)
                .status(ApplicationStatus.ACTIVE)
                .job(approvedJob)
                .build();

        UUID candidateId = UUID.randomUUID();
        when(applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE))
                .thenReturn(Optional.of(interviewApp));
        when(offerRepository.countByJobIdAndStatusIn(eq(approvedJob.getId()), any()))
                .thenReturn(1L); // 1 active offer, headcount 3 -> ok
        when(offerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateOfferRequest req = CreateOfferRequest.builder()
                .candidateId(candidateId)
                .salary(new BigDecimal("25000000"))
                .positionTitle("Frontend Developer")
                .build();

        OfferResponse response = offerService.createDraft(req);
        assertNotNull(response);
        assertEquals(OfferStatus.DRAFT, response.getStatus());
        assertEquals("Frontend Developer", response.getPositionTitle());
    }

    @Test
    void testCreateDraft_InvalidSalary_ThrowsBadRequest() {
        Application interviewApp = Application.builder()
                .id(UUID.randomUUID())
                .stage(ApplicationStage.INTERVIEW)
                .status(ApplicationStatus.ACTIVE)
                .build();

        UUID candidateId = UUID.randomUUID();
        when(applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE))
                .thenReturn(Optional.of(interviewApp));

        CreateOfferRequest req = CreateOfferRequest.builder()
                .candidateId(candidateId)
                .salary(new BigDecimal("-1000"))
                .positionTitle("Backend Engineer")
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () ->
                offerService.createDraft(req)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
