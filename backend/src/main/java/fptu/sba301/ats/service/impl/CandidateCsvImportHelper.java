package fptu.sba301.ats.service.impl;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import fptu.sba301.ats.dto.request.CsvCandidateRow;
import fptu.sba301.ats.dto.response.BulkImportResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.CandidateDocumentRepository;
import fptu.sba301.ats.repository.CandidateRepository;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class CandidateCsvImportHelper {

    public BulkImportResponse importCandidatesFromCsv(
            MultipartFile csvFile,
            List<MultipartFile> cvFiles,
            CandidateRepository candidateRepository,
            JobRepository jobRepository,
            ApplicationRepository applicationRepository,
            CloudinaryService cloudinaryService,
            CandidateDocumentRepository candidateDocumentRepository,
            CandidateDocumentHelper documentHelper,
            CandidateMapper mapper
    ) {
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
                processCsvRow(
                        row,
                        rowNumber,
                        cvFileMap,
                        errors,
                        candidateRepository,
                        jobRepository,
                        applicationRepository,
                        cloudinaryService,
                        candidateDocumentRepository,
                        documentHelper
                );
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("Row " + rowNumber + ": " + e.getMessage());
            }
        }

        return mapper.toBulkImportResponse(rows.size(), successCount, failCount, errors);
    }

    private void processCsvRow(
            CsvCandidateRow row,
            int rowNumber,
            Map<String, MultipartFile> cvFileMap,
            List<String> errors,
            CandidateRepository candidateRepository,
            JobRepository jobRepository,
            ApplicationRepository applicationRepository,
            CloudinaryService cloudinaryService,
            CandidateDocumentRepository candidateDocumentRepository,
            CandidateDocumentHelper documentHelper
    ) {
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
                documentHelper.uploadAndSaveDocuments(
                        candidate,
                        List.of(cvFile),
                        cloudinaryService,
                        candidateDocumentRepository
                );
            }
        }
    }
}
