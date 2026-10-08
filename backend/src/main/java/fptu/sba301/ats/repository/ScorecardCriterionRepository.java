package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.ScorecardCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScorecardCriterionRepository extends JpaRepository<ScorecardCriterion, UUID> {
    List<ScorecardCriterion> findByTemplateId(UUID templateId);
    void deleteByTemplateId(UUID templateId);
    Optional<ScorecardCriterion> findByName(String name);
}
