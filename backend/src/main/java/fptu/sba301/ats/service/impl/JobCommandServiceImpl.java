package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.JobCreateRequest;
import fptu.sba301.ats.dto.request.JobUpdateRequest;
import fptu.sba301.ats.dto.response.JobDetailResponse;
import fptu.sba301.ats.dto.response.JobResponse;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.JobDetailRepository;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.JobCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobCommandServiceImpl implements JobCommandService {

    private final JobRepository jobRepository;
    private final JobDetailRepository jobDetailRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public JobResponse createJob(User currentUser, JobCreateRequest request) {
        if (currentUser.getRole() != Role.HR) {
            throw new AccessDeniedException("Only HR can create jobs");
        }
        Department department = currentUser.getDepartment();
        if (department == null) {
            throw new BusinessException("Your account has no department assigned", HttpStatus.BAD_REQUEST);
        }

        User manager = userRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new BusinessException("Authenticated user not found", HttpStatus.UNAUTHORIZED));

        Job job = Job.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .department(manager.getDepartment())
                .hiringManager(manager)
                .status(JobStatus.PENDING_APPROVAL)
                .headcount(request.getHeadcount())
                .build();

        job = jobRepository.save(job);
        return toJobResponse(job);
    }

    @Override
    @Transactional
    public JobResponse updateJob(User currentUser, UUID jobId, JobUpdateRequest request) {
        if (currentUser.getRole() != Role.HR) {
            throw new AccessDeniedException("Only HR can update jobs");
        }
        Job job = jobDetailRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", HttpStatus.NOT_FOUND));

        UUID userDeptId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
        UUID jobDeptId = job.getDepartment() != null ? job.getDepartment().getId() : null;
        if (userDeptId == null || jobDeptId == null || !userDeptId.equals(jobDeptId)) {
            throw new AccessDeniedException("You can only edit jobs in your department");
        }

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setHeadcount(request.getHeadcount());

        if (job.getStatus() == JobStatus.APPROVED) {
            job.setStatus(JobStatus.PENDING_APPROVAL);
        }

        job = jobRepository.save(job);
        return toJobResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobResponse> listPendingJobs(User currentUser) {
        if (currentUser.getRole() != Role.HR_MANAGER) {
            throw new AccessDeniedException("Only HR_MANAGER can view pending jobs");
        }
        List<Job> jobs = jobRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("status"), JobStatus.PENDING_APPROVAL),
                Sort.by(Sort.Order.asc("title"))
        );
        return jobs.stream().map(this::toJobResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public JobResponse approveJob(User currentUser, UUID jobId) {
        if (currentUser.getRole() != Role.HR_MANAGER) {
            throw new AccessDeniedException("Only HR_MANAGER can approve jobs");
        }
        Job job = jobDetailRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", HttpStatus.NOT_FOUND));
        if (job.getStatus() != JobStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only jobs pending approval can be approved", HttpStatus.BAD_REQUEST);
        }
        job.setStatus(JobStatus.APPROVED);
        job = jobRepository.save(job);
        return toJobResponse(job);
    }

    @Override
    @Transactional
    public JobResponse rejectJob(User currentUser, UUID jobId) {
        return rejectJob(currentUser, jobId, "Job rejected by manager");
    }

    @Override
    @Transactional
    public JobResponse rejectJob(User currentUser, UUID jobId, String reason) {
        if (currentUser.getRole() != Role.HR_MANAGER && currentUser.getRole() != Role.SYSTEM_ADMIN) {
            throw new AccessDeniedException("Only HR_MANAGER or SYSTEM_ADMIN can reject jobs");
        }
        Job job = jobDetailRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", HttpStatus.NOT_FOUND));
        if (job.getStatus() != JobStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only jobs pending approval can be rejected", HttpStatus.BAD_REQUEST);
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("Rejection reason is required when rejecting a job", HttpStatus.BAD_REQUEST);
        }
        job.setStatus(JobStatus.REJECTED);
        job.setRejectionReason(reason.trim());
        job = jobRepository.save(job);
        return toJobResponse(job);
    }

    @Override
    @Transactional
    public JobResponse closeJob(User currentUser, UUID jobId) {
        if (currentUser.getRole() != Role.HR && currentUser.getRole() != Role.HR_MANAGER && currentUser.getRole() != Role.SYSTEM_ADMIN) {
            throw new AccessDeniedException("Not authorized to close jobs");
        }
        Job job = jobDetailRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", HttpStatus.NOT_FOUND));

        if (currentUser.getRole() == Role.HR) {
            UUID userDeptId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
            UUID jobDeptId = job.getDepartment() != null ? job.getDepartment().getId() : null;
            if (userDeptId == null || jobDeptId == null || !userDeptId.equals(jobDeptId)) {
                throw new AccessDeniedException("You can only close jobs in your department");
            }
        }

        job.setStatus(JobStatus.CLOSED);
        job = jobRepository.save(job);
        return toJobResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public JobDetailResponse getJobForEdit(User currentUser, UUID jobId) {
        if (currentUser.getRole() != Role.HR) {
            throw new AccessDeniedException("Only HR can load job for editing");
        }
        Job job = jobDetailRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("Job not found", HttpStatus.NOT_FOUND));

        UUID userDeptId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : null;
        UUID jobDeptId = job.getDepartment() != null ? job.getDepartment().getId() : null;
        if (userDeptId == null || jobDeptId == null || !userDeptId.equals(jobDeptId)) {
            throw new AccessDeniedException("You can only edit jobs in your department");
        }

        return JobDetailResponse.builder()
                .jobId(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .departmentName(job.getDepartment() != null ? job.getDepartment().getName() : null)
                .hiringManagerId(job.getHiringManager() != null ? job.getHiringManager().getId() : null)
                .hiringManagerName(job.getHiringManager() != null ? job.getHiringManager().getFullName() : null)
                .status(job.getStatus())
                .headcount(job.getHeadcount())
                .applicants(List.of())
                .build();
    }

    private JobResponse toJobResponse(Job job) {
        return JobResponse.builder()
                .jobId(job.getId())
                .departmentId(job.getDepartment() != null ? job.getDepartment().getId() : null)
                .departmentName(job.getDepartment() != null ? job.getDepartment().getName() : null)
                .title(job.getTitle())
                .status(job.getStatus())
                .build();
    }
}
