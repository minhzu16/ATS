package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.CandidateNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateNoteRepository extends JpaRepository<CandidateNote, UUID> {
    List<CandidateNote> findByApplication_Candidate_IdOrderByCreatedAtDesc(UUID candidateId);
    List<CandidateNote> findByApplication_IdOrderByCreatedAtDesc(UUID applicationId);
}
