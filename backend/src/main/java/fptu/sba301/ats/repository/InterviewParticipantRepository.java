package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.InterviewParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewParticipantRepository extends JpaRepository<InterviewParticipant, InterviewParticipant.InterviewParticipantId> {
    List<InterviewParticipant> findByIdInterviewId(UUID interviewId);
    Optional<InterviewParticipant> findByIdInterviewIdAndIdUserId(UUID interviewId, UUID userId);
    Optional<InterviewParticipant> findByInterviewIdAndUserId(UUID interviewId, UUID userId);
    List<InterviewParticipant> findByUserId(UUID userId);
}
