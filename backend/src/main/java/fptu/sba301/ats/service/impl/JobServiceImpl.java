package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.JobResponse;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;

    @Override
    public List<JobResponse> getAllJobs() {
        return jobRepository.findByStatusOrderByTitleAsc(JobStatus.APPROVED)
                .stream()
                .map(job -> JobResponse.builder()
                        .jobId(job.getId())
                        .departmentId(job.getDepartment() != null ? job.getDepartment().getId() : null)
                        .departmentName(job.getDepartment() != null ? job.getDepartment().getName() : null)
                        .title(job.getTitle())
                        .status(job.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
