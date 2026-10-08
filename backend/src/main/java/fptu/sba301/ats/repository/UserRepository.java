package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = {"department"})
    Optional<User> findByEmailAndDeletedFalse(String email);
    List<User> findByDeletedFalse();
    Page<User> findByDeletedFalse(Pageable pageable);
    Page<User> findByDeletedFalseAndDepartmentId(UUID departmentId, Pageable pageable);
    List<User> findByRoleAndDeletedFalse(fptu.sba301.ats.enums.Role role);
    Optional<User> findByResetTokenAndDeletedFalse(String resetToken);
    Optional<User> findByIdAndDeletedFalse(UUID id);
    boolean existsByEmailAndDeletedFalse(String email);
    Optional<User> findByActivationTokenAndDeletedFalse(String activationToken);
    long countByDepartment_IdAndDeletedFalse(UUID departmentId);
    long countByDeletedFalse();
    long countByCreatedAtBetween(Instant startDate, Instant endDate);
    long countByRoleAndDeletedFalse(fptu.sba301.ats.enums.Role role);
    long countByAccountLockedTrueAndDeletedFalse();
}

