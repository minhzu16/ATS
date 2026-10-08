package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.enums.JobStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {
    List<Job> findByStatusOrderByTitleAsc(JobStatus status);

    long countByStatus(JobStatus status);
    long countByDepartment_IdAndStatusNot(UUID departmentId, JobStatus status);
    long countByDepartment_IdAndStatus(UUID departmentId, JobStatus status);

    /**
     * Builds a {@link Specification} for the role-aware job list: optional department, optional status,
     * case-insensitive title match (wildcards {@code %} and {@code _} stripped from the keyword).
     */
    static Specification<Job> forListSearch(
            UUID departmentId,
            JobStatus requiredStatus,
            boolean filterByDepartment,
            boolean filterByStatus,
            String keyword
    ) {
        return (root, query, cb) -> {
            List<Predicate> list = new ArrayList<>();
            if (filterByDepartment && departmentId != null) {
                list.add(cb.equal(root.get("department").get("id"), departmentId));
            }
            if (filterByStatus && requiredStatus != null) {
                list.add(cb.equal(root.get("status"), requiredStatus));
            }
            if (StringUtils.hasText(keyword)) {
                String normalized = keyword.trim().toLowerCase().replace("%", "").replace("_", "");
                if (!normalized.isEmpty()) {
                    list.add(cb.like(cb.lower(root.get("title")), "%" + normalized + "%"));
                }
            }
            if (query != null && Job.class.equals(query.getResultType())) {
                query.orderBy(cb.asc(root.get("title")));
            }
            return cb.and(list.toArray(new Predicate[0]));
        };
    }
}
