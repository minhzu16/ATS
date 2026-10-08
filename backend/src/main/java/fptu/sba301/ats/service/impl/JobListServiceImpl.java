package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.JobPageResponse;
import fptu.sba301.ats.dto.response.JobResponse;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.service.JobListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobListServiceImpl implements JobListService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final JobRepository jobRepository;

    @Override
    @Transactional(readOnly = true)
    public JobPageResponse listJobsForUser(User currentUser, int page, int size, String keyword) {
        if (currentUser.getRole() == Role.SYSTEM_ADMIN) {
            throw new AccessDeniedException("SYSTEM_ADMIN cannot access the job list");
        }

        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        boolean filterDept;
        boolean filterStatus;
        java.util.UUID departmentId = null;
        JobStatus statusFilter = JobStatus.APPROVED;

        Role role = currentUser.getRole();
        if (role == Role.HR || role == Role.HR_MANAGER) {
            filterDept = false;
            filterStatus = false;
            statusFilter = null;
        } else if (role == Role.INTERVIEWER) {
            filterDept = true;
            filterStatus = true;
            if (currentUser.getDepartment() == null) {
                return emptyPage(safePage, safeSize);
            }
            departmentId = currentUser.getDepartment().getId();
        } else {
            throw new AccessDeniedException("Role is not allowed to access the job list");
        }

        Specification<Job> spec = JobRepository.forListSearch(
                departmentId,
                statusFilter,
                filterDept,
                filterStatus,
                keyword
        );
        Page<Job> result = jobRepository.findAll(spec, pageable);

        List<JobResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return JobPageResponse.builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .build();
    }

    private JobPageResponse emptyPage(int page, int size) {
        return JobPageResponse.builder()
                .content(List.of())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .build();
    }

    private JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .jobId(job.getId())
                .departmentId(job.getDepartment() != null ? job.getDepartment().getId() : null)
                .departmentName(job.getDepartment() != null ? job.getDepartment().getName() : null)
                .title(job.getTitle())
                .status(job.getStatus())
                .build();
    }
}
