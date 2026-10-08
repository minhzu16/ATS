package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.OnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, UUID> {
}
