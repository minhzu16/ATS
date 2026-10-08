package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.OfferApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferApprovalRepository extends JpaRepository<OfferApproval, UUID> {
    List<OfferApproval> findByOfferIdOrderByCreatedAtDesc(UUID offerId);
}
