package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.JobPageResponse;
import fptu.sba301.ats.entity.User;

public interface JobListService {
    JobPageResponse listJobsForUser(User currentUser, int page, int size, String keyword);
}
