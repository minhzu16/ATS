package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.JobApplicantResponse;
import fptu.sba301.ats.dto.response.JobDetailResponse;
import fptu.sba301.ats.entity.User;

import java.util.List;
import java.util.UUID;

public interface JobDetailService {
    JobDetailResponse getJobDetail(User currentUser, UUID jobId);

    List<JobApplicantResponse> listJobApplicants(User currentUser, UUID jobId);
}
