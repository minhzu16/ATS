package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {
    Optional<Offer> findByApplicationId(UUID applicationId);
    List<Offer> findAllByOrderByCreatedAtDesc();
}
