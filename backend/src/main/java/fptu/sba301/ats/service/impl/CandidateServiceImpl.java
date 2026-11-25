package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.CreateCandidateRequest;
import fptu.sba301.ats.dto.request.ScheduleCandidateInterviewsRequest;
import fptu.sba301.ats.dto.response.BulkImportResponse;
import fptu.sba301.ats.dto.response.CandidateDetailResponse;
import fptu.sba301.ats.dto.response.CandidateDocumentResponse;
import fptu.sba301.ats.dto.response.CandidateHistoryResponse;
import fptu.sba301.ats.dto.response.CandidateListResponse;
import fptu.sba301.ats.dto.response.InterviewerOptionResponse;
import fptu.sba301.ats.dto.response.ScheduleCandidateInterviewsResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.CandidateStageHistory;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.CandidateDocumentRepository;
import fptu.sba301.ats.repository.CandidateRepository;
import fptu.sba301.ats.repository.CandidateStageHistoryRepository;
import fptu.sba301.ats.repository.InterviewParticipantRepository;
import fptu.sba301.ats.repository.InterviewRepository;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.repository.ScorecardTemplateRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.repository.projection.CandidateDetailProjection;
import fptu.sba301.ats.service.ApplicationStageTransitionService;
import fptu.sba301.ats.service.CandidateService;
import fptu.sba301.ats.service.CloudinaryService;
import fptu.sba301.ats.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;
    private final CandidateStageHistoryRepository candidateStageHistoryRepository;
    private final ApplicationStageTransitionService applicationStageTransitionService;
    private final CandidateDocumentRepository candidateDocumentRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final CloudinaryService cloudinaryService;
    private final ScorecardTemplateRepository scorecardTemplateRepository;
    private final EmailService emailService;

    private final CandidateMapper mapper = new CandidateMapper();
    private final CandidateDocumentHelper documentHelper = new CandidateDocumentHelper();
    private final CandidateInterviewScheduler interviewScheduler = new CandidateInterviewScheduler();
    private final CandidateCsvImportHelper csvImportHelper = new CandidateCsvImportHelper();

    @Override
    public List<CandidateListResponse> getCandidateList() {
        return mapper.toCandidateListResponses(candidateRepository.findAllCandidatesWithApplications());
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateDetailResponse getCandidateDetail(UUID candidateId) {
        // Pure read: no state-altering side effects
        return buildCandidateDetailResponse(candidateId);
    }

    @Override
    @Transactional
    public CandidateDetailResponse startScreening(UUID candidateId) {
        Application application = findLatestActiveApplication(candidateId);
        applicationStageTransitionService.transition(application, ApplicationStage.SCREENING);
        return buildCandidateDetailResponse(candidateId);
    }

    @Override
    @Transactional
    public CandidateDetailResponse updateCandidateStage(UUID candidateId, ApplicationStage targetStage) {
        return updateCandidateStage(candidateId, targetStage, null);
    }

    @Override
    @Transactional
    public CandidateDetailResponse updateCandidateStage(UUID candidateId, ApplicationStage targetStage, String reason) {
        Application application = findLatestActiveApplication(candidateId);
        applicationStageTransitionService.transition(application, targetStage, reason);
        return buildCandidateDetailResponse(candidateId);
    }

    @Override
    @Transactional
    public CandidateDetailResponse withdrawCandidate(UUID candidateId, String reason) {
        Application application = findLatestActiveApplication(candidateId);
        applicationStageTransitionService.withdraw(application, reason);
        return buildCandidateDetailResponse(candidateId);
    }

    @Override
    @Transactional
    public CandidateDetailResponse addApplicationForCandidate(UUID candidateId, UUID jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessException("Candidate not found", HttpStatus.NOT_FOUND));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", HttpStatus.NOT_FOUND));

        boolean alreadyApplied = applicationRepository.findAll().stream()
                .anyMatch(a -> a.getCandidate().getId().equals(candidateId) && a.getJob().getId().equals(jobId));

        if (alreadyApplied) {
            throw new BusinessException("Candidate has already applied for this job", HttpStatus.CONFLICT);
        }

        applicationRepository.save(Application.builder()
                .candidate(candidate)
                .job(job)
                .stage(ApplicationStage.APPLIED)
                .status(ApplicationStatus.ACTIVE)
                .build());

        return buildCandidateDetailResponse(candidateId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateHistoryResponse> getStageHistory(UUID candidateId) {
        Application application = applicationRepository
                .findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE)
                .orElse(null);

        if (application == null) {
            return new ArrayList<>();
        }

        List<CandidateStageHistory> histories = candidateStageHistoryRepository
                .findByApplication_IdOrderByCreatedAtDesc(application.getId());

        return mapper.toHistoryResponses(histories, userRepository);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewerOptionResponse> getInterviewerOptions() {
        return mapper.toInterviewerOptionResponses(
                userRepository.findByRoleAndDeletedFalse(Role.INTERVIEWER)
        );
    }

    @Override
    @Transactional
    public ScheduleCandidateInterviewsResponse scheduleCandidateInterviews(
            UUID candidateId,
            ScheduleCandidateInterviewsRequest request
    ) {
        Application application = findLatestActiveApplication(candidateId);
        return interviewScheduler.schedule(
                candidateId,
                application,
                request,
                applicationStageTransitionService,
                userRepository,
                interviewParticipantRepository,
                scorecardTemplateRepository,
                interviewRepository,
                emailService,
                mapper
        );
    }

    @Override
    @Transactional
    public CandidateDetailResponse createCandidate(CreateCandidateRequest request, List<MultipartFile> documents) {
        // 1. Validate duplicate email
        String normalizedEmail = StringUtils.hasText(request.getEmail()) ? request.getEmail().trim().toLowerCase() : null;
        if (normalizedEmail != null && candidateRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException(
                    "Email '" + request.getEmail() + "' already exists", HttpStatus.BAD_REQUEST);
        }

        // 2. Save candidate
        Candidate candidate = Candidate.builder()
                .fullName(request.getFullName())
                .email(normalizedEmail)
                .phone(request.getPhone())
                .currentCompany(request.getCurrentCompany())
                .source(request.getSource())
                .location(request.getLocation())
                .experienceYears(request.getExperienceYears())
                .summary(request.getSummary())
                .build();
        candidate = candidateRepository.save(candidate);

        // 3. Link to job (create Application) if jobId provided
        if (request.getJobId() != null) {
            Job job = jobRepository.findById(request.getJobId())
                    .orElseThrow(() -> new BusinessException(
                            "Job not found: " + request.getJobId(), HttpStatus.NOT_FOUND));
            applicationRepository.save(Application.builder()
                    .candidate(candidate)
                    .job(job)
                    .build());
        }

        // 4. Upload documents to Cloudinary
        List<CandidateDocumentResponse> documentResponses = new ArrayList<>();
        if (documents != null && !documents.isEmpty()) {
            documentHelper.uploadAndSaveDocuments(candidate, documents, cloudinaryService, candidateDocumentRepository);
            // Reload the saved documents for the response
            documentResponses = documentHelper.findDocumentsByCandidateId(candidateRepository, mapper, candidate.getId());
        }

        // 5. If a job was linked, use the SQL-backed builder (includes jobTitle, stage etc.)
        //    Otherwise build directly from the saved entity to avoid JOIN failure.
        if (request.getJobId() != null) {
            return buildCandidateDetailResponse(candidate.getId());
        }

        return mapper.toCandidateDetailResponse(candidate, documentResponses);
    }

    @Override
    @Transactional
    public BulkImportResponse importCandidatesFromCsv(MultipartFile csvFile, List<MultipartFile> cvFiles) {
        return csvImportHelper.importCandidatesFromCsv(
                csvFile,
                cvFiles,
                candidateRepository,
                jobRepository,
                applicationRepository,
                cloudinaryService,
                candidateDocumentRepository,
                documentHelper,
                mapper
        );
    }

    private CandidateDetailResponse buildCandidateDetailResponse(UUID candidateId) {
        CandidateDetailProjection detail = candidateRepository.findCandidateDetailById(candidateId.toString())
                .orElseThrow(() -> new BusinessException("Candidate not found", HttpStatus.NOT_FOUND));

        List<CandidateDocumentResponse> documents = documentHelper.findDocumentsByCandidateId(
                candidateRepository, mapper, candidateId);

        return mapper.toCandidateDetailResponse(detail, documents);
    }

    private Application findLatestActiveApplication(UUID candidateId) {
        return applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Active application not found", HttpStatus.NOT_FOUND));
    }

}
