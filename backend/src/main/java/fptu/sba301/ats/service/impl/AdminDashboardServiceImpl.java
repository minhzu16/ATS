package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.AdminAnalyticsDTO;
import fptu.sba301.ats.dto.response.SystemHealthDTO;
import fptu.sba301.ats.dto.response.UserManagementStatsDTO;
import fptu.sba301.ats.entity.Job;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.CandidateStageHistoryRepository;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final CandidateStageHistoryRepository candidateStageHistoryRepository;
    private final Runtime runtime = Runtime.getRuntime();

    @Override
    public AdminAnalyticsDTO getAnalytics(String period) {
        // Calculate date range based on period
        Instant endDate = Instant.now();
        Instant startDate = calculateStartDate(period, endDate);

        // Get total applications
        long totalApplications = applicationRepository.count();
        long newApplicationsThisMonth = applicationRepository
                .countByCreatedAtBetween(
                        Instant.now().minus(30, ChronoUnit.DAYS),
                        Instant.now()
                );

        // Build hiring funnel with real numbers
        Map<String, Long> hiringFunnel = new LinkedHashMap<>();
        hiringFunnel.put("APPLIED", applicationRepository.countByStage(ApplicationStage.APPLIED));
        hiringFunnel.put("SCREENING", applicationRepository.countByStage(ApplicationStage.SCREENING));
        hiringFunnel.put("INTERVIEW", applicationRepository.countByStage(ApplicationStage.INTERVIEW));
        hiringFunnel.put("OFFER", applicationRepository.countByStage(ApplicationStage.OFFER));
        hiringFunnel.put("HIRED", applicationRepository.countByStage(ApplicationStage.HIRED));

        // Calculate conversion rate (hired / applied)
        long applied = hiringFunnel.getOrDefault("APPLIED", 0L);
        long hired = hiringFunnel.getOrDefault("HIRED", 0L);
        double conversionRate = applied > 0 ? (hired * 100.0) / applied : 0.0;

        // Department breakdown from real DB data
        Map<String, Long> applicationsByDepartment = new HashMap<>();
        for (Object[] row : applicationRepository.countApplicationsByDepartment()) {
            if (row[0] != null && row[1] != null) {
                applicationsByDepartment.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        // Top jobs from real DB data
        List<Job> allJobs = jobRepository.findAll();
        List<AdminAnalyticsDTO.JobPerformanceDTO> topJobs = allJobs.stream()
                .limit(5)
                .map(job -> {
                    long appCount = applicationRepository.findByJobId(job.getId()).size();
                    long hiredCount = applicationRepository.countByJob_IdAndStage(job.getId(), ApplicationStage.HIRED);
                    double rate = appCount > 0 ? (hiredCount * 100.0) / appCount : 0.0;
                    return AdminAnalyticsDTO.JobPerformanceDTO.builder()
                            .jobId(job.getId().toString())
                            .jobTitle(job.getTitle())
                            .applications(appCount)
                            .hired(hiredCount)
                            .conversionRate(Math.round(rate * 10.0) / 10.0)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getApplications(), a.getApplications()))
                .collect(Collectors.toList());

        // Source analysis from real DB data
        Map<String, Long> applicationsBySource = new HashMap<>();
        for (Object[] row : applicationRepository.countApplicationsBySource()) {
            if (row[0] != null && row[1] != null) {
                applicationsBySource.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        // Rejection reasons from real history records
        Map<String, Long> rejectionReasons = new HashMap<>();
        for (Object[] row : candidateStageHistoryRepository.countRejectionReasons()) {
            if (row[0] != null && row[1] != null) {
                rejectionReasons.put((String) row[0], ((Number) row[1]).longValue());
            }
        }

        return AdminAnalyticsDTO.builder()
                .totalApplications(totalApplications)
                .totalUsers(userRepository.countByDeletedFalse())
                .newApplicationsThisMonth(newApplicationsThisMonth)
                .conversionRate(Math.round(conversionRate * 10.0) / 10.0)
                .timeToHireAverage(0.0)
                .hiringFunnel(hiringFunnel)
                .applicationsByDepartment(applicationsByDepartment)
                .topJobs(topJobs)
                .applicationsBySource(applicationsBySource)
                .rejectionReasons(rejectionReasons)
                .period(period)
                .build();
    }

    @Override
    public SystemHealthDTO getSystemHealth() {
        boolean databaseHealthy = true;
        String databaseStatus = "HEALTHY";
        try {
            userRepository.count();
        } catch (Exception e) {
            databaseHealthy = false;
            databaseStatus = "DOWN";
        }

        boolean applicationHealthy = databaseHealthy;
        String applicationStatus = databaseHealthy ? "UP" : "DEGRADED";

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = totalMemory > 0 ? (usedMemory * 100.0) / totalMemory : 0.0;

        // JVM uptime in milliseconds
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();

        int healthScore = databaseHealthy ? 100 : 50;

        return SystemHealthDTO.builder()
                .databaseHealthy(databaseHealthy)
                .databaseStatus(databaseStatus)
                .databaseConnections(1)
                .databaseQueries(0)
                .applicationHealthy(applicationHealthy)
                .applicationStatus(applicationStatus)
                .cpuUsage(0.0)
                .memoryUsage(Math.round(memoryUsage * 10.0) / 10.0)
                .uptime(uptimeMs)
                .errorsLast24Hours(0)
                .errorsLast7Days(0)
                .errorRate(0.0)
                .p95ResponseTime(0.0)
                .p99ResponseTime(0.0)
                .averageResponseTime(0.0)
                .requestsPerSecond(0)
                .healthScore(healthScore)
                .lastChecked(Instant.now())
                .build();
    }

    @Override
    public UserManagementStatsDTO getUserManagementStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByDeletedFalse();
        long inactiveUsers = totalUsers - activeUsers;

        long newUsersThisMonth = userRepository.countByCreatedAtBetween(
                Instant.now().minus(30, ChronoUnit.DAYS),
                Instant.now()
        );

        long adminCount = userRepository.countByRoleAndDeletedFalse(Role.SYSTEM_ADMIN);
        long hrManagerCount = userRepository.countByRoleAndDeletedFalse(Role.HR_MANAGER);
        long recruiterCount = userRepository.countByRoleAndDeletedFalse(Role.HR);
        long interviewerCount = userRepository.countByRoleAndDeletedFalse(Role.INTERVIEWER);
        long accountsLocked = userRepository.countByAccountLockedTrueAndDeletedFalse();

        return UserManagementStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .adminCount(adminCount)
                .hrManagerCount(hrManagerCount)
                .recruiterCount(recruiterCount)
                .interviewerCount(interviewerCount)
                .averageLoginFrequency(0.0)
                .usersInactiveMoreThan30Days(0)
                .usersInactiveMoreThan60Days(0)
                .failedLoginAttempts(0)
                .accountsLocked(accountsLocked)
                .passwordExpiringSoon(0)
                .build();
    }

    private Instant calculateStartDate(String period, Instant endDate) {
        return switch (period) {
            case "7days" -> endDate.minus(7, ChronoUnit.DAYS);
            case "30days" -> endDate.minus(30, ChronoUnit.DAYS);
            case "quarter" -> endDate.minus(90, ChronoUnit.DAYS);
            default -> endDate.minus(30, ChronoUnit.DAYS);
        };
    }
}
