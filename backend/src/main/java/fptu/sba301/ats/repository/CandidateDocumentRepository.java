package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.CandidateDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateDocumentRepository extends JpaRepository<CandidateDocument, UUID> {
    List<CandidateDocument> findByCandidateIdOrderByUploadedAtDesc(UUID candidateId);
}
