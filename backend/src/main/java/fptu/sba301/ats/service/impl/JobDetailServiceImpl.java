package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.JobApplicantResponse;
import fptu.sba301.ats.dto.response.JobDetailResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.InterviewScoreRepository;
import fptu.sba301.ats.repository.JobDetailRepository;
import fptu.sba301.ats.service.JobDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobDetailServiceImpl implements JobDetailService {

    private final JobDetailRepository jobDetailRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewScoreRepository interviewScoreRepository;

    @Override
    @Transactional(readOnly = true)
    public JobDetailResponse getJobDetail(User currentUser, UUID jobId) {
        Job job = jobDetailRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", HttpStatus.NOT_FOUND));

        assertCanAccessJob(currentUser, job);

        return JobDetailResponse.builder()
                .jobId(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .departmentName(job.getDepartment() != null ? job.getDepartment().getName() : null)
                .hiringManagerId(job.getHiringManager() != null ? job.getHiringManager().getId() : null)
                .hiringManagerName(job.getHiringManager() != null ? job.getHiringManager().getFullName() : null)
                .status(job.getStatus())
                .headcount(job.getHeadcount())
                .applicants(mapApplicantsForJob(jobId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobApplicantResponse> listJobApplicants(User currentUser, UUID jobId) {
        Job job = jobDetailRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", HttpStatus.NOT_FOUND));

        assertCanAccessJob(currentUser, job);

        return mapApplicantsForJob(jobId);
    }

    private List<JobApplicantResponse> mapApplicantsForJob(UUID jobId) {
        List<Application> applications = applicationRepository.findByJobIdAndStatusWithCandidateFetched(
                jobId,
                ApplicationStatus.ACTIVE
        );
        if (applications.isEmpty()) {
            return List.of();
        }

        List<UUID> applicationIds = applications.stream().map(Application::getId).toList();
        Map<UUID, BigDecimal> ratingByApplicationId = new HashMap<>();
        if (!applicationIds.isEmpty()) {
            for (Object[] row : interviewScoreRepository.averageScoresByApplicationIds(applicationIds)) {
                UUID appId = (UUID) row[0];
                if (row[1] != null) {
                    double avg = ((Number) row[1]).doubleValue();
                    ratingByApplicationId.put(
                            appId,
                            BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP)
                    );
                }
            }
        }

        return applications.stream()
                .map(app -> {
                    var c = app.getCandidate();
                    if (c == null) {
                        throw new IllegalStateException("Application " + app.getId() + " has no candidate");
                    }
                    return JobApplicantResponse.builder()
                            .candidateId(c.getId())
                            .fullName(c.getFullName())
                            .email(c.getEmail())
                            .stage(app.getStage().name())
                            .rating(ratingByApplicationId.get(app.getId()))
                            .appliedAt(app.getAppliedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void assertCanAccessJob(User currentUser, Job job) {
        Role role = currentUser.getRole();
        if (role == Role.SYSTEM_ADMIN) {
            throw new AccessDeniedException("SYSTEM_ADMIN cannot access job detail");
        }

        if (role == Role.HR || role == Role.HR_MANAGER) {
            return;
        }

        if (role == Role.INTERVIEWER) {
            UUID userDepartmentId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
            UUID jobDepartmentId = job.getDepartment() != null ? job.getDepartment().getId() : null;
            boolean sameDepartment = userDepartmentId != null && userDepartmentId.equals(jobDepartmentId);
            boolean approved = job.getStatus() == JobStatus.APPROVED;
            if (!sameDepartment || !approved) {
                throw new AccessDeniedException("You do not have permission to access this job");
            }
            return;
        }

        throw new AccessDeniedException("Role is not allowed to access job detail");
    }
}
