package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.repository.projection.CandidateDetailProjection;
import fptu.sba301.ats.repository.projection.CandidateDocumentProjection;
import fptu.sba301.ats.repository.projection.CandidateListProjection;
import fptu.sba301.ats.repository.projection.JobApplicantProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    Optional<Candidate> findByEmailIgnoreCase(String email);

    @Query(value = """
        SELECT
            BIN_TO_UUID(c.id) AS candidateId,
            c.full_name AS fullName,
            c.email AS email,
            j.title AS jobTitle,
            a.stage AS stage,
            ROUND(AVG(isc.score), 1) AS rating,
            a.applied_at AS appliedAt
        FROM candidates c
        JOIN applications a ON c.id = a.candidate_id
        JOIN jobs j ON a.job_id = j.id
        LEFT JOIN interviews i ON a.id = i.application_id
        LEFT JOIN interview_scores isc ON i.id = isc.interview_id
        WHERE a.status = 'ACTIVE'
        GROUP BY c.id, c.full_name, c.email, j.title, a.stage, a.applied_at
        ORDER BY a.applied_at DESC
        """, nativeQuery = true)
    List<CandidateListProjection> findAllCandidatesWithApplications();

    @Query(value = """
        SELECT
            BIN_TO_UUID(c.id) AS candidateId,
            c.full_name AS fullName,
            c.email AS email,
            a.stage AS stage,
            ROUND(AVG(isc.score), 1) AS rating,
            a.applied_at AS appliedAt
        FROM candidates c
        JOIN applications a ON c.id = a.candidate_id
        LEFT JOIN interviews i ON a.id = i.application_id
        LEFT JOIN interview_scores isc ON i.id = isc.interview_id
        WHERE a.job_id = UUID_TO_BIN(:jobId)
          AND a.status = 'ACTIVE'
        GROUP BY a.id, c.id, c.full_name, c.email, a.stage, a.applied_at
        ORDER BY a.applied_at DESC
        """, nativeQuery = true)
    List<JobApplicantProjection> findApplicantsByJobId(@Param("jobId") String jobId);

    @Query(value = """
        SELECT
            BIN_TO_UUID(c.id) AS candidateId,
            BIN_TO_UUID(a.id) AS applicationId,
            c.full_name AS fullName,
            c.email AS email,
            c.phone AS phone,
            c.current_company AS currentCompany,
            j.title AS jobTitle,
            a.stage AS stage,
            a.status AS status,
            ROUND(AVG(isc.score), 1) AS rating,
            a.applied_at AS appliedAt,
            c.source AS source,
            c.location AS location,
            c.experience_years AS experienceYears,
            c.summary AS summary
        FROM candidates c
        JOIN applications a ON c.id = a.candidate_id
        JOIN jobs j ON a.job_id = j.id
        LEFT JOIN interviews i ON a.id = i.application_id
        LEFT JOIN interview_scores isc ON i.id = isc.interview_id
        WHERE c.id = UUID_TO_BIN(:candidateId)
          AND a.status = 'ACTIVE'
        GROUP BY c.id, a.id, c.full_name, c.email, c.phone, c.current_company,
                 j.title, a.stage, a.status, a.applied_at,
                 c.source, c.location, c.experience_years, c.summary
        ORDER BY a.applied_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<CandidateDetailProjection> findCandidateDetailById(@Param("candidateId") String candidateId);

    @Query(value = """
        SELECT
            BIN_TO_UUID(cd.id) AS documentId,
            cd.file_name AS fileName,
            cd.file_url AS fileUrl,
            cd.file_type AS fileType,
            cd.file_size_bytes AS fileSizeBytes,
            cd.uploaded_at AS uploadedAt
        FROM candidate_documents cd
        WHERE cd.candidate_id = UUID_TO_BIN(:candidateId)
        ORDER BY cd.uploaded_at DESC
        """, nativeQuery = true)
    List<CandidateDocumentProjection> findDocumentsByCandidateId(@Param("candidateId") String candidateId);
}
