package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.InterviewScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewScoreRepository extends JpaRepository<InterviewScore, UUID> {
    List<InterviewScore> findByInterviewId(UUID interviewId);
    List<InterviewScore> findByInterview_IdAndInterviewer_Id(UUID interviewId, UUID userId);
    @Modifying
    @Query("""
        DELETE FROM InterviewScore s
        WHERE s.interview.id = :interviewId
        AND s.interviewer.id = :userId
    """)
    void deleteOld(UUID interviewId, UUID userId);

    @Query("""
            SELECT i.application.id, AVG(s.score)
            FROM InterviewScore s
            JOIN s.interview i
            WHERE i.application.id IN :applicationIds
            GROUP BY i.application.id
            """)
    List<Object[]> averageScoresByApplicationIds(@Param("applicationIds") List<UUID> applicationIds);
}
