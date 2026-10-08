package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Job;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobDetailRepository extends JpaRepository<Job, UUID> {

    @EntityGraph(attributePaths = {"department", "hiringManager"})
    Optional<Job> findById(UUID id);
}
