package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.DashboardRecentApplicationDTO;
import fptu.sba301.ats.dto.response.DashboardStatsDTO;
import fptu.sba301.ats.dto.response.DashboardTodaysInterviewDTO;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.InterviewRepository;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final ZoneId DASHBOARD_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter ISO_DT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(User currentUser) {
        Role role = currentUser.getRole();
        if (role != Role.HR && role != Role.HR_MANAGER && role != Role.INTERVIEWER
                && role != Role.SYSTEM_ADMIN) {
            throw new AccessDeniedException("Dashboard is not available for this role");
        }

        boolean needsDepartment = role == Role.HR || role == Role.INTERVIEWER;
        if (needsDepartment && currentUser.getDepartment() == null) {
            return emptyStats();
        }

        UUID deptId = (role == Role.HR_MANAGER || role == Role.SYSTEM_ADMIN)
                ? null
                : currentUser.getDepartment().getId();

        int activeJobs = deptId == null
                ? (int) jobRepository.countByStatus(JobStatus.APPROVED)
                : (int) jobRepository.countByDepartment_IdAndStatus(deptId, JobStatus.APPROVED);

        int newCandidates = (int) applicationRepository.countDistinctActiveCandidatesByDepartment(
                ApplicationStatus.ACTIVE, deptId);

        LocalDate today = LocalDate.now(DASHBOARD_ZONE);
        LocalDateTime dayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime dayEnd = dayStart.plusDays(1);

        int interviewsToday = (int) interviewRepository.countScheduledBetweenAndDepartment(
                dayStart, dayEnd, deptId);

        int offersSent = (int) applicationRepository.countByStageAndDepartment(
                ApplicationStage.OFFER, ApplicationStatus.ACTIVE, deptId);

        Map<String, Long> pipeline = new HashMap<>();
        for (ApplicationStage stage : List.of(
                ApplicationStage.APPLIED,
                ApplicationStage.SCREENING,
                ApplicationStage.INTERVIEW,
                ApplicationStage.OFFER,
                ApplicationStage.HIRED)) {
            pipeline.put(stage.name(), applicationRepository.countByStageAndDepartment(
                    stage, ApplicationStatus.ACTIVE, deptId));
        }

        List<Application> recent = applicationRepository.findRecentApplicationsForDashboard(
                ApplicationStatus.ACTIVE,
                deptId,
                PageRequest.of(0, 10));

        List<DashboardRecentApplicationDTO> recentDtos = recent.stream()
                .map(this::toRecentDto)
                .collect(Collectors.toList());

        List<Interview> todayList = interviewRepository.findScheduledBetweenAndDepartment(
                dayStart, dayEnd, deptId);

        List<DashboardTodaysInterviewDTO> todayDtos = todayList.stream()
                .map(this::toInterviewDto)
                .collect(Collectors.toList());

        return DashboardStatsDTO.builder()
                .activeJobs(activeJobs)
                .newCandidates(newCandidates)
                .interviewsToday(interviewsToday)
                .offersSent(offersSent)
                .hiringPipeline(pipeline)
                .recentApplications(recentDtos)
                .todaysInterviews(todayDtos)
                .build();
    }

    private DashboardStatsDTO emptyStats() {
        Map<String, Long> pipeline = new HashMap<>();
        for (ApplicationStage stage : List.of(
                ApplicationStage.APPLIED,
                ApplicationStage.SCREENING,
                ApplicationStage.INTERVIEW,
                ApplicationStage.OFFER,
                ApplicationStage.HIRED)) {
            pipeline.put(stage.name(), 0L);
        }
        return DashboardStatsDTO.builder()
                .activeJobs(0)
                .newCandidates(0)
                .interviewsToday(0)
                .offersSent(0)
                .hiringPipeline(pipeline)
                .recentApplications(List.of())
                .todaysInterviews(List.of())
                .build();
    }

    private DashboardRecentApplicationDTO toRecentDto(Application a) {
        String applied = a.getAppliedAt() != null ? a.getAppliedAt().format(ISO_DT) : null;
        return DashboardRecentApplicationDTO.builder()
                .applicationId(a.getId())
                .candidateName(a.getCandidate() != null ? a.getCandidate().getFullName() : "")
                .jobTitle(a.getJob() != null ? a.getJob().getTitle() : "")
                .stage(a.getStage() != null ? a.getStage().name() : "")
                .appliedAt(applied)
                .build();
    }

    private DashboardTodaysInterviewDTO toInterviewDto(Interview i) {
        Application app = i.getApplication();
        String cand = app != null && app.getCandidate() != null
                ? app.getCandidate().getFullName()
                : "";
        String job = app != null && app.getJob() != null ? app.getJob().getTitle() : "";
        String sched = i.getScheduledAt() != null ? i.getScheduledAt().format(ISO_DT) : null;
        return DashboardTodaysInterviewDTO.builder()
                .interviewId(i.getId())
                .candidateName(cand)
                .jobTitle(job)
                .scheduledAt(sched)
                .location(i.getLocation())
                .status(i.getStatus() != null ? i.getStatus().name() : "")
                .build();
    }

}
