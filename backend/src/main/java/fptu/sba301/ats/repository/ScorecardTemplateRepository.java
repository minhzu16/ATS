package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.ScorecardTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScorecardTemplateRepository extends JpaRepository<ScorecardTemplate, UUID> {
    List<ScorecardTemplate> findByDepartmentId(UUID departmentId);
}
