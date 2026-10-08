package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.JobResponse;

import java.util.List;

public interface JobService {
    List<JobResponse> getAllJobs();
}
