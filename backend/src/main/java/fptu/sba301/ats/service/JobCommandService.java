package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.JobCreateRequest;
import fptu.sba301.ats.dto.request.JobUpdateRequest;
import fptu.sba301.ats.dto.response.JobDetailResponse;
import fptu.sba301.ats.dto.response.JobResponse;
import fptu.sba301.ats.entity.User;

import java.util.List;
import java.util.UUID;

public interface JobCommandService {

    JobResponse createJob(User currentUser, JobCreateRequest request);

    JobResponse updateJob(User currentUser, UUID jobId, JobUpdateRequest request);

    List<JobResponse> listPendingJobs(User currentUser);

    JobResponse approveJob(User currentUser, UUID jobId);

    JobResponse rejectJob(User currentUser, UUID jobId);

    JobResponse rejectJob(User currentUser, UUID jobId, String reason);

    JobResponse closeJob(User currentUser, UUID jobId);

    JobDetailResponse getJobForEdit(User currentUser, UUID jobId);
}
