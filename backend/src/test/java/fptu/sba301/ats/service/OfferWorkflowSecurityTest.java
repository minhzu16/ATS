package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.OfferApprovalRequest;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Offer;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApprovalStatus;
import fptu.sba301.ats.enums.OfferStatus;
import fptu.sba301.ats.enums.Role;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
}
