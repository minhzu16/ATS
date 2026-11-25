package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.JobCreateRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobCommandServiceImplTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private JobDetailRepository jobDetailRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobCommandServiceImpl jobCommandService;

    @Test
    void createJobSucceedsForHrWithDepartment() {
        Department dept = Department.builder().id(UUID.randomUUID()).name("IT").build();
        User hr = User.builder().id(UUID.randomUUID()).role(Role.HR).department(dept).fullName("HR User").build();
        JobCreateRequest req = new JobCreateRequest("Senior Java Dev", "Build APIs", 2);

        when(userRepository.findByIdAndDeletedFalse(hr.getId())).thenReturn(Optional.of(hr));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(UUID.randomUUID());
            return j;
        });

        JobResponse res = jobCommandService.createJob(hr, req);
        assertEquals("Senior Java Dev", res.getTitle());
        assertEquals(JobStatus.PENDING_APPROVAL, res.getStatus());
    }

    @Test
    void createJobRejectsNonHrUser() {
        User interviewer = User.builder().id(UUID.randomUUID()).role(Role.INTERVIEWER).build();
        JobCreateRequest req = new JobCreateRequest("QA", "Test", 1);

        assertThrows(AccessDeniedException.class, () -> jobCommandService.createJob(interviewer, req));
    }

    @Test
    void createJobRejectsHrWithoutDepartment() {
        User hr = User.builder().id(UUID.randomUUID()).role(Role.HR).department(null).build();
        JobCreateRequest req = new JobCreateRequest("QA", "Test", 1);

        BusinessException ex = assertThrows(BusinessException.class, () -> jobCommandService.createJob(hr, req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void approveJobTransitionsPendingJobToApproved() {
        UUID jobId = UUID.randomUUID();
        User manager = User.builder().id(UUID.randomUUID()).role(Role.HR_MANAGER).build();
        Job job = Job.builder().id(jobId).title("Dev").status(JobStatus.PENDING_APPROVAL).headcount(1).build();

        when(jobDetailRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

        JobResponse res = jobCommandService.approveJob(manager, jobId);
        assertEquals(JobStatus.APPROVED, res.getStatus());
    }

    @Test
    void approveJobRejectsAlreadyApprovedJob() {
        UUID jobId = UUID.randomUUID();
        User manager = User.builder().id(UUID.randomUUID()).role(Role.HR_MANAGER).build();
        Job job = Job.builder().id(jobId).title("Dev").status(JobStatus.APPROVED).headcount(1).build();

        when(jobDetailRepository.findById(jobId)).thenReturn(Optional.of(job));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> jobCommandService.approveJob(manager, jobId));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
