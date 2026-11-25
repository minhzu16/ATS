package fptu.sba301.ats.service;

import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeederService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final DemoScorecardSeedHelper scorecardSeedHelper;
    private final DemoPipelineSeedHelper pipelineSeedHelper;

    @Transactional
    public String seedDemoData() {
        try {
            scorecardSeedHelper.seedSystemConfigsIfEmpty();
            if (jobRepository.count() > 0) {
                return "Demo data already exists.";
            }

            User hr = userRepository.findByEmailAndDeletedFalse("hr@ats.com")
                    .orElseThrow(() -> new RuntimeException(
                            "HR user not found. Please run /api/v1/seed-admin first."));

            var foundation = scorecardSeedHelper.seedDepartmentsAndScorecards(hr);
            pipelineSeedHelper.seedRecruitmentPipeline(
                    hr, foundation.itDept(), foundation.frontendCriteria());

            return "Prioritized demo data seeded successfully!";
        } catch (Exception e) {
            log.error("Error seeding demo data", e);
            throw new RuntimeException("Failed to seed demo data: " + e.getMessage());
        }
    }
}
