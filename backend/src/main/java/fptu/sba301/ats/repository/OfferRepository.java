package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Offer;
import fptu.sba301.ats.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfferRepository extends JpaRepository<Offer, UUID> {
    Optional<Offer> findByApplicationId(UUID applicationId);
    List<Offer> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(o) FROM Offer o WHERE o.application.job.id = :jobId AND o.status IN :statuses")
    long countByJobIdAndStatusIn(@Param("jobId") UUID jobId, @Param("statuses") Collection<OfferStatus> statuses);
}
