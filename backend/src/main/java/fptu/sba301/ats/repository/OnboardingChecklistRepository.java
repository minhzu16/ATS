package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.OnboardingChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OnboardingChecklistRepository extends JpaRepository<OnboardingChecklist, UUID> {
    Optional<OnboardingChecklist> findByApplicationId(UUID applicationId);

    List<OnboardingChecklist> findAllByOrderByCreatedAtDesc();

    @Query("SELECT o FROM OnboardingChecklist o " +
           "LEFT JOIN FETCH o.application app " +
           "LEFT JOIN FETCH app.candidate " +
           "LEFT JOIN FETCH app.job " +
           "WHERE o.id = :id")
    Optional<OnboardingChecklist> findByIdWithDetails(@Param("id") UUID id);
}

