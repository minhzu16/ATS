package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.JobPageResponse;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobListServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobListServiceImpl jobListService;

    @Test
    void listJobsRejectsSystemAdmin() {
        User admin = User.builder().id(UUID.randomUUID()).role(Role.SYSTEM_ADMIN).build();
        assertThrows(AccessDeniedException.class, () -> jobListService.listJobsForUser(admin, 0, 20, null));
    }

    @Test
    void listJobsReturnsEmptyPageForInterviewerWithoutDepartment() {
        User interviewer = User.builder().id(UUID.randomUUID()).role(Role.INTERVIEWER).department(null).build();
        JobPageResponse res = jobListService.listJobsForUser(interviewer, 0, 10, "");
        assertEquals(0, res.getTotalElements());
        assertEquals(10, res.getSize());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listJobsReturnsMappedJobsForHr() {
        Department dept = Department.builder().id(UUID.randomUUID()).name("IT").build();
        User hr = User.builder().id(UUID.randomUUID()).role(Role.HR).department(dept).build();
        Job job = Job.builder().id(UUID.randomUUID()).title("Backend Dev").department(dept).status(JobStatus.APPROVED).build();

        when(jobRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(job)));

        JobPageResponse res = jobListService.listJobsForUser(hr, 0, 0, "Backend");
        assertEquals(1, res.getTotalElements());
        assertEquals("Backend Dev", res.getContent().get(0).getTitle());
    }

    @Test
    @SuppressWarnings("unchecked")
    void listJobsScopesInterviewerToOwnDepartment() {
        Department dept = Department.builder().id(UUID.randomUUID()).name("IT").build();
        User interviewer = User.builder().id(UUID.randomUUID()).role(Role.INTERVIEWER).department(dept).build();
        Job job = Job.builder().id(UUID.randomUUID()).title("Frontend Dev").department(dept).status(JobStatus.APPROVED).build();

        when(jobRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(job)));

        JobPageResponse res = jobListService.listJobsForUser(interviewer, -1, 500, null);
        assertEquals(1, res.getTotalElements());
        assertEquals("Frontend Dev", res.getContent().get(0).getTitle());
    }
}
