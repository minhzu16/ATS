package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    Optional<Application> findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(UUID candidateId, ApplicationStatus status);
    List<Application> findByJobId(UUID jobId);
    long countByJob_IdAndStage(UUID jobId, ApplicationStage stage);

    @Query("""
            SELECT a FROM Application a
            JOIN FETCH a.candidate
            WHERE a.job.id = :jobId AND a.status = :status
            ORDER BY a.appliedAt DESC
            """)
    List<Application> findByJobIdAndStatusWithCandidateFetched(
            @Param("jobId") UUID jobId,
            @Param("status") ApplicationStatus status
    );
    long countByStatus(ApplicationStatus status);
    long countByStage(ApplicationStage stage);
    long countByCreatedAtBetween(Instant startDate, Instant endDate);

    @Query("SELECT j.department.name, COUNT(a) FROM Application a JOIN a.job j WHERE j.department IS NOT NULL GROUP BY j.department.name")
    List<Object[]> countApplicationsByDepartment();

    @Query("SELECT c.source, COUNT(a) FROM Application a JOIN a.candidate c WHERE c.source IS NOT NULL AND c.source <> '' GROUP BY c.source")
    List<Object[]> countApplicationsBySource();

    @Query("""
            SELECT COUNT(DISTINCT a.candidate.id) FROM Application a
            WHERE a.status = :appStatus
            AND (:deptId IS NULL OR a.job.department.id = :deptId)
            """)
    long countDistinctActiveCandidatesByDepartment(
            @Param("appStatus") ApplicationStatus appStatus,
            @Param("deptId") UUID deptId);

    @Query("""
            SELECT COUNT(a) FROM Application a
            WHERE a.stage = :stage AND a.status = :appStatus
            AND (:deptId IS NULL OR a.job.department.id = :deptId)
            """)
    long countByStageAndDepartment(
            @Param("stage") ApplicationStage stage,
            @Param("appStatus") ApplicationStatus appStatus,
            @Param("deptId") UUID deptId);

    @Query("""
            SELECT a FROM Application a
            JOIN FETCH a.candidate c
            JOIN FETCH a.job j
            LEFT JOIN FETCH j.department
            WHERE a.status = :appStatus
            AND (:deptId IS NULL OR j.department.id = :deptId)
            ORDER BY a.appliedAt DESC
            """)
    List<Application> findRecentApplicationsForDashboard(
            @Param("appStatus") ApplicationStatus appStatus,
            @Param("deptId") UUID deptId,
            Pageable pageable);
}
