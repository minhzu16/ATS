package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    java.util.Optional<Department> findByName(String name);
    boolean existsByName(String name);
}
