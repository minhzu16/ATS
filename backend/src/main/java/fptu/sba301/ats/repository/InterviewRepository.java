package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {
    List<Interview> findByApplicationId(UUID applicationId);
    @Query("""
        select distinct i
        from Interview i
        left join fetch i.participants p
        where i.id = :interviewId
    """)
    Optional<Interview> findByIdWithParticipants(@Param("interviewId") UUID interviewId);

    @Query("""
            SELECT COUNT(i) FROM Interview i
            JOIN i.application a
            JOIN a.job j
            WHERE i.scheduledAt >= :start AND i.scheduledAt < :end
            AND (:deptId IS NULL OR j.department.id = :deptId)
            """)
    long countScheduledBetweenAndDepartment(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("deptId") UUID deptId);

    @Query("""
            SELECT i FROM Interview i
            JOIN FETCH i.application a
            JOIN FETCH a.candidate c
            JOIN FETCH a.job j
            LEFT JOIN FETCH j.department
            WHERE i.scheduledAt >= :start AND i.scheduledAt < :end
            AND (:deptId IS NULL OR j.department.id = :deptId)
            ORDER BY i.scheduledAt ASC
            """)
    List<Interview> findScheduledBetweenAndDepartment(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("deptId") UUID deptId);
}
