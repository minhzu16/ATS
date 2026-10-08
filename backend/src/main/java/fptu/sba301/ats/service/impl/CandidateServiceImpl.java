package fptu.sba301.ats.service.impl;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import fptu.sba301.ats.dto.request.CreateCandidateRequest;
import fptu.sba301.ats.dto.request.CsvCandidateRow;
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
import fptu.sba301.ats.entity.CandidateDocument;
import fptu.sba301.ats.entity.CandidateStageHistory;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.ScorecardTemplate;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.enums.ParticipantRole;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private static final String CLOUDINARY_FOLDER = "candidate_documents";
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

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
    private final fptu.sba301.ats.service.EmailService emailService;

    // ─────────────────────────── Existing methods ───────────────────────────

    @Override
    public List<CandidateListResponse> getCandidateList() {
        return candidateRepository.findAllCandidatesWithApplications()
                .stream()
                .map(row -> CandidateListResponse.builder()
                        .candidateId(UUID.fromString(row.getCandidateId()))
                        .fullName(row.getFullName())
                        .email(row.getEmail())
                        .jobTitle(row.getJobTitle())
                        .stage(row.getStage())
                        .rating(row.getRating())
                        .appliedAt(row.getAppliedAt())
                        .build())
                .collect(Collectors.toList());
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

        return histories.stream().map(h -> {
            String changedBy = "System";
            if (h.getCreatedBy() != null) {
                changedBy = userRepository.findById(h.getCreatedBy())
                        .map(User::getFullName)
                        .orElse("Unknown User");
            }
            return CandidateHistoryResponse.builder()
                    .id(h.getId())
                    .fromStage(h.getFromStage())
                    .toStage(h.getToStage())
                    .changedBy(changedBy)
                    .changedAt(h.getCreatedAt())
                    .reason(h.getReason())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewerOptionResponse> getInterviewerOptions() {
        return userRepository.findByRoleAndDeletedFalse(Role.INTERVIEWER).stream()
                .filter(User::isActive)
                .map(user -> InterviewerOptionResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleCandidateInterviewsResponse scheduleCandidateInterviews(
            UUID candidateId,
            ScheduleCandidateInterviewsRequest request
    ) {
        Application application = findLatestActiveApplication(candidateId);

        if (application.getStage() != ApplicationStage.APPLIED
                && application.getStage() != ApplicationStage.SCREENING
                && application.getStage() != ApplicationStage.INTERVIEW) {
            throw new BusinessException(
                    "Only candidates in APPLIED, SCREENING, or INTERVIEW can be scheduled",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (application.getStage() != ApplicationStage.INTERVIEW) {
            applicationStageTransitionService.transition(application, ApplicationStage.INTERVIEW);
        }

        List<UUID> interviewIds = new ArrayList<>();

        for (ScheduleCandidateInterviewsRequest.InterviewSlot slot : request.getInterviews()) {
            if (slot.getScheduledAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException("Cannot schedule an interview in the past", HttpStatus.BAD_REQUEST);
            }

            Set<UUID> uniqueInterviewerIds = Set.copyOf(slot.getInterviewerIds());
            List<User> interviewers = userRepository.findAllById(uniqueInterviewerIds);

            if (interviewers.size() != uniqueInterviewerIds.size()) {
                throw new BusinessException("One or more interviewers not found", HttpStatus.BAD_REQUEST);
            }

            boolean hasInvalidRole = interviewers.stream()
                    .anyMatch(user -> user.getRole() != Role.INTERVIEWER || user.isDeleted());

            if (hasInvalidRole) {
                throw new BusinessException("All assigned users must be active INTERVIEWER", HttpStatus.BAD_REQUEST);
            }

            // Conflict detection: Check if any interviewer has another interview within 60 mins of slot start
            LocalDateTime slotStart = slot.getScheduledAt();
            LocalDateTime slotEnd = slotStart.plusMinutes(slot.getDurationMinutes() != null ? slot.getDurationMinutes() : 60);

            for (User interviewer : interviewers) {
                List<InterviewParticipant> existing = interviewParticipantRepository.findByUserId(interviewer.getId());
                for (InterviewParticipant ep : existing) {
                    Interview other = ep.getInterview();
                    if (other != null && other.getStatus() == InterviewStatus.SCHEDULED && other.getScheduledAt() != null) {
                        LocalDateTime otherStart = other.getScheduledAt();
                        LocalDateTime otherEnd = otherStart.plusMinutes(other.getDurationMinutes() != null ? other.getDurationMinutes() : 60);
                        if (slotStart.isBefore(otherEnd) && slotEnd.isAfter(otherStart)) {
                            throw new BusinessException(
                                    "Interviewer " + interviewer.getFullName() + " has a scheduling conflict at " + slotStart,
                                    HttpStatus.CONFLICT
                            );
                        }
                    }
                }
            }

            // Resolve scorecard template
            ScorecardTemplate template = null;
            if (slot.getTemplateId() != null) {
                template = scorecardTemplateRepository.findById(slot.getTemplateId())
                        .orElse(null);
            }
            if (template == null && application.getJob() != null && application.getJob().getDepartment() != null) {
                List<ScorecardTemplate> deptTemplates = scorecardTemplateRepository.findByDepartmentId(application.getJob().getDepartment().getId());
                if (!deptTemplates.isEmpty()) {
                    template = deptTemplates.get(0);
                }
            }
            if (template == null) {
                List<ScorecardTemplate> allTemplates = scorecardTemplateRepository.findAll();
                if (!allTemplates.isEmpty()) {
                    template = allTemplates.get(0);
                }
            }

            Interview interview = interviewRepository.save(Interview.builder()
                    .application(application)
                    .template(template)
                    .scheduledAt(slot.getScheduledAt())
                    .durationMinutes(slot.getDurationMinutes() != null ? slot.getDurationMinutes() : 60)
                    .type(slot.getType())
                    .location(slot.getLocation())
                    .meetingLink(slot.getMeetingLink())
                    .status(InterviewStatus.SCHEDULED)
                    .build());

            interviewIds.add(interview.getId());

            List<InterviewParticipant> participants = interviewers.stream()
                    .map(user -> InterviewParticipant.builder()
                            .id(new InterviewParticipant.InterviewParticipantId(interview.getId(), user.getId()))
                            .interview(interview)
                            .user(user)
                            .role(ParticipantRole.INTERVIEWER)
                            .build())
                    .collect(Collectors.toList());

            interviewParticipantRepository.saveAll(participants);

            // Send calendar invitations with .ics attachments
            Candidate candidate = application.getCandidate();
            String jobTitle = (application.getJob() != null) ? application.getJob().getTitle() : "Open Position";
            if (candidate != null && candidate.getEmail() != null && !candidate.getEmail().isBlank()) {
                emailService.sendInterviewInvitation(
                        candidate.getEmail(),
                        candidate.getFullName(),
                        candidate.getFullName(),
                        jobTitle,
                        slot.getScheduledAt(),
                        slot.getDurationMinutes(),
                        slot.getLocation(),
                        slot.getMeetingLink()
                );
            }
            for (User interviewer : interviewers) {
                if (interviewer.getEmail() != null && !interviewer.getEmail().isBlank()) {
                    emailService.sendInterviewInvitation(
                            interviewer.getEmail(),
                            interviewer.getFullName(),
                            candidate.getFullName(),
                            jobTitle,
                            slot.getScheduledAt(),
                            slot.getDurationMinutes(),
                            slot.getLocation(),
                            slot.getMeetingLink()
                    );
                }
            }
        }

        return ScheduleCandidateInterviewsResponse.builder()
                .candidateId(candidateId)
                .applicationId(application.getId())
                .stage(application.getStage().name())
                .totalInterviewsCreated(interviewIds.size())
                .interviewIds(interviewIds)
                .build();
    }

    // ─────────────────────────── New: Single Candidate Add ───────────────────────────

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
            uploadAndSaveDocuments(candidate, documents);
            // Reload the saved documents for the response
            documentResponses = candidateRepository
                    .findDocumentsByCandidateId(candidate.getId().toString())
                    .stream()
                    .map(d -> CandidateDocumentResponse.builder()
                            .documentId(UUID.fromString(d.getDocumentId()))
                            .fileName(d.getFileName())
                            .fileUrl(d.getFileUrl())
                            .fileType(d.getFileType())
                            .fileSizeBytes(d.getFileSizeBytes())
                            .uploadedAt(d.getUploadedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        // 5. If a job was linked, use the SQL-backed builder (includes jobTitle, stage etc.)
        //    Otherwise build directly from the saved entity to avoid JOIN failure.
        if (request.getJobId() != null) {
            return buildCandidateDetailResponse(candidate.getId());
        }

        return CandidateDetailResponse.builder()
                .candidateId(candidate.getId())
                .fullName(candidate.getFullName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .currentCompany(candidate.getCurrentCompany())
                .source(candidate.getSource())
                .location(candidate.getLocation())
                .experienceYears(candidate.getExperienceYears())
                .summary(candidate.getSummary())
                .documents(documentResponses)
                .build();
    }


    // ─────────────────────────── New: CSV Bulk Import ───────────────────────────

    @Override
    @Transactional
    public BulkImportResponse importCandidatesFromCsv(MultipartFile csvFile, List<MultipartFile> cvFiles) {
        // 1. Build map: filename → MultipartFile for quick lookup
        Map<String, MultipartFile> cvFileMap = new HashMap<>();
        if (cvFiles != null) {
            for (MultipartFile f : cvFiles) {
                if (f != null && StringUtils.hasText(f.getOriginalFilename())) {
                    cvFileMap.put(f.getOriginalFilename(), f);
                }
            }
        }

        // 2. Parse CSV
        List<CsvCandidateRow> rows;
        try (Reader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            CsvToBean<CsvCandidateRow> csvToBean = new CsvToBeanBuilder<CsvCandidateRow>(reader)
                    .withType(CsvCandidateRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();
            rows = csvToBean.parse();
        } catch (Exception e) {
            throw new BusinessException("Failed to parse CSV file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // 3. Process rows
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 2; // 1-indexed + header row
            CsvCandidateRow row = rows.get(i);
            try {
                processCsvRow(row, rowNumber, cvFileMap, errors);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("Row " + rowNumber + ": " + e.getMessage());
            }
        }

        return BulkImportResponse.builder()
                .totalRows(rows.size())
                .successCount(successCount)
                .failCount(failCount)
                .errors(errors)
                .build();
    }

    /** Process a single CSV row inside a try-catch; warnings (missing CV) are added to errors list but do not throw. */
    private void processCsvRow(CsvCandidateRow row, int rowNumber,
                               Map<String, MultipartFile> cvFileMap,
                               List<String> errors) {
        // Validate fullName
        if (!StringUtils.hasText(row.getFullName())) {
            throw new BusinessException("fullName is required", HttpStatus.BAD_REQUEST);
        }

        // Validate email uniqueness
        String normalizedEmail = StringUtils.hasText(row.getEmail()) ? row.getEmail().trim().toLowerCase() : null;
        if (normalizedEmail != null && candidateRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("email '" + row.getEmail() + "' already exists", HttpStatus.BAD_REQUEST);
        }

        // Save candidate
        Candidate candidate = Candidate.builder()
                .fullName(row.getFullName())
                .email(normalizedEmail)
                .phone(StringUtils.hasText(row.getPhone()) ? row.getPhone() : null)
                .currentCompany(StringUtils.hasText(row.getCurrentCompany()) ? row.getCurrentCompany() : null)
                .source(StringUtils.hasText(row.getSource()) ? row.getSource() : null)
                .location(StringUtils.hasText(row.getLocation()) ? row.getLocation() : null)
                .experienceYears(row.getExperienceYears())
                .summary(StringUtils.hasText(row.getSummary()) ? row.getSummary() : null)
                .build();
        candidate = candidateRepository.save(candidate);

        // Create Application if jobId provided
        if (StringUtils.hasText(row.getJobId())) {
            try {
                UUID jobUuid = UUID.fromString(row.getJobId().trim());
                Job job = jobRepository.findById(jobUuid)
                        .orElseThrow(() -> new RuntimeException("Job not found: " + row.getJobId()));
                Application application = Application.builder()
                        .candidate(candidate)
                        .job(job)
                        .build();
                applicationRepository.save(application);
            } catch (IllegalArgumentException e) {
                errors.add("Row " + rowNumber + ": [WARNING] invalid jobId '" + row.getJobId() + "' – skipped");
            }
        }

        // Upload CV if filename is specified
        if (StringUtils.hasText(row.getCvFileName())) {
            MultipartFile cvFile = cvFileMap.get(row.getCvFileName().trim());
            if (cvFile == null) {
                // Warning only – candidate is still created
                errors.add("Row " + rowNumber + ": [WARNING] missing CV file '" + row.getCvFileName()
                        + "' – candidate created without CV");
            } else {
                uploadAndSaveDocuments(candidate, List.of(cvFile));
            }
        }
    }

    // ─────────────────────────── Private helpers ───────────────────────────

    private void uploadAndSaveDocuments(Candidate candidate, List<MultipartFile> files) {
        List<CandidateDocument> docs = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            fptu.sba301.ats.util.FileValidator.validateDocument(file);
            Map<String, Object> result = cloudinaryService.uploadFile(file, CLOUDINARY_FOLDER);

            String fileUrl = (String) result.get("secure_url");
            String format = (String) result.get("format");
            Object bytesObj = result.get("bytes");
            Long fileSizeBytes = bytesObj != null ? Long.parseLong(bytesObj.toString()) : null;
            String originalFilename = file.getOriginalFilename();

            docs.add(CandidateDocument.builder()
                    .candidate(candidate)
                    .fileName(originalFilename != null ? originalFilename : "document")
                    .fileUrl(fileUrl)
                    .fileType(format)
                    .fileSizeBytes(fileSizeBytes)
                    .uploadedAt(LocalDateTime.now(APP_ZONE))
                    .build());
        }
        candidateDocumentRepository.saveAll(docs);
    }

    private CandidateDetailResponse buildCandidateDetailResponse(UUID candidateId) {
        CandidateDetailProjection detail = candidateRepository.findCandidateDetailById(candidateId.toString())
                .orElseThrow(() -> new BusinessException("Candidate not found", HttpStatus.NOT_FOUND));

        List<CandidateDocumentResponse> documents = candidateRepository.findDocumentsByCandidateId(candidateId.toString())
                .stream()
                .map(document -> CandidateDocumentResponse.builder()
                        .documentId(UUID.fromString(document.getDocumentId()))
                        .fileName(document.getFileName())
                        .fileUrl(document.getFileUrl())
                        .fileType(document.getFileType())
                        .fileSizeBytes(document.getFileSizeBytes())
                        .uploadedAt(document.getUploadedAt())
                        .build())
                .collect(Collectors.toList());

        return CandidateDetailResponse.builder()
                .candidateId(UUID.fromString(detail.getCandidateId()))
                .applicationId(detail.getApplicationId() != null ? UUID.fromString(detail.getApplicationId()) : null)
                .fullName(detail.getFullName())
                .email(detail.getEmail())
                .phone(detail.getPhone())
                .currentCompany(detail.getCurrentCompany())
                .jobTitle(detail.getJobTitle())
                .stage(detail.getStage())
                .status(detail.getStatus())
                .rating(detail.getRating())
                .appliedAt(detail.getAppliedAt())
                .source(detail.getSource())
                .location(detail.getLocation())
                .experienceYears(detail.getExperienceYears())
                .summary(detail.getSummary())
                .documents(documents)
                .build();
    }

    private Application findLatestActiveApplication(UUID candidateId) {
        return applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Active application not found", HttpStatus.NOT_FOUND));
    }

}
