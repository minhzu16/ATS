package fptu.sba301.ats.config;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Offer;
import fptu.sba301.ats.entity.OfferApproval;
import fptu.sba301.ats.entity.RefreshToken;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApprovalStatus;
import fptu.sba301.ats.enums.OfferStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Seeds offers, offer approvals, and sample refresh tokens.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
class OfferTokenSeedHelper {

    private final EntityManager entityManager;
    private final SeedQueryHelper seedQueryHelper;

    List<Offer> seedOffers(List<Application> applications) {
        Application offerStageApplication = null;
        for (Application a : applications) {
            if (a.getStage() == ApplicationStage.OFFER) {
                offerStageApplication = a;
                break;
            }
        }

        if (seedQueryHelper.isEntityEmpty(Offer.class) && offerStageApplication != null) {
            log.info("Seeding Offers...");
            entityManager.persist(Offer.builder()
                    .application(offerStageApplication)
                    .salary(new BigDecimal("2500.00"))
                    .positionTitle("Senior Frontend Developer")
                    .status(OfferStatus.PENDING_APPROVAL)
                    .build());
            entityManager.flush();
            log.info("Offers seeded.");
        }

        return seedQueryHelper.findAll(Offer.class);
    }

    void seedOfferApprovals(List<Offer> offers, User admin) {
        if (seedQueryHelper.isEntityEmpty(OfferApproval.class) && !offers.isEmpty() && admin != null) {
            Offer pendingOffer = null;
            for (Offer o : offers) {
                if (o.getStatus() == OfferStatus.PENDING_APPROVAL) {
                    pendingOffer = o;
                    break;
                }
            }
            if (pendingOffer != null) {
                log.info("Seeding Offer Approvals...");
                entityManager.persist(OfferApproval.builder()
                        .offer(pendingOffer)
                        .approvedBy(admin)
                        .status(ApprovalStatus.APPROVED)
                        .comment("Compensation approved by System Admin")
                        .build());
                pendingOffer.setStatus(OfferStatus.APPROVED);
                Application approvedApp = pendingOffer.getApplication();
                if (approvedApp != null) {
                    approvedApp.setStage(ApplicationStage.HIRED);
                    entityManager.merge(approvedApp);
                }
                entityManager.merge(pendingOffer);
                entityManager.flush();
                log.info("Offer Approvals seeded.");
            }
        }
    }

    void seedRefreshTokens(User hrUser) {
        if (seedQueryHelper.isEntityEmpty(RefreshToken.class) && hrUser != null) {
            log.info("Seeding Refresh Tokens...");
            entityManager.persist(RefreshToken.builder()
                    .token(UUID.randomUUID() + ".seed-token")
                    .user(hrUser)
                    .expiryDate(LocalDateTime.now().plusDays(7))
                    .revoked(false)
                    .build());
            entityManager.flush();
            log.info("Refresh Tokens seeded.");
        }
    }
}
