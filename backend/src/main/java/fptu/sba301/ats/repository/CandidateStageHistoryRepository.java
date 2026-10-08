package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.CandidateStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateStageHistoryRepository extends JpaRepository<CandidateStageHistory, UUID> {
    List<CandidateStageHistory> findByApplication_IdOrderByCreatedAtDesc(UUID applicationId);

    @org.springframework.data.jpa.repository.Query("SELECT h.reason, COUNT(h) FROM CandidateStageHistory h WHERE h.reason IS NOT NULL AND h.toStage = 'REJECTED' GROUP BY h.reason")
    List<Object[]> countRejectionReasons();
}
