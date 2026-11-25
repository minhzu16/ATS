package fptu.sba301.ats.service;

import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.entity.ScorecardTemplate;
import fptu.sba301.ats.entity.SystemConfig;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.repository.DepartmentRepository;
import fptu.sba301.ats.repository.ScorecardCriterionRepository;
import fptu.sba301.ats.repository.ScorecardTemplateRepository;
import fptu.sba301.ats.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoScorecardSeedHelper {

    private final SystemConfigRepository systemConfigRepository;
    private final DepartmentRepository departmentRepository;
    private final ScorecardTemplateRepository scorecardTemplateRepository;
    private final ScorecardCriterionRepository scorecardCriterionRepository;

    public record SeededFoundation(
            Department itDept,
            Department marketingDept,
            List<ScorecardCriterion> frontendCriteria) {}

    public void seedSystemConfigsIfEmpty() {
        if (systemConfigRepository.count() > 0) {
            return;
        }
        saveConfig("NOTIFICATIONS_ENABLED", "true");
        saveConfig("SESSION_TIMEOUT_MINUTES", "30");
    }

    private void saveConfig(String key, String value) {
        SystemConfig config = new SystemConfig();
        config.setKey(key);
        config.setValue(value);
        config.setUpdatedAt(Instant.now());
        systemConfigRepository.save(config);
    }

    public SeededFoundation seedDepartmentsAndScorecards(User hr) {
        Department itDept = Department.builder()
                .id(UUID.randomUUID())
                .name("Information Technology")
                .description("IT and Software Engineering")
                .build();
        itDept = departmentRepository.save(itDept);

        Department marketingDept = new Department();
        marketingDept.setName("Marketing");
        marketingDept.setCreatedBy(hr.getId());
        marketingDept = departmentRepository.save(marketingDept);

        ScorecardTemplate frontendTemplate = saveTemplate("Frontend Developer Evaluation", itDept, hr);
        List<ScorecardCriterion> frontendCriteria = List.of(
                buildCriterion(frontendTemplate, "React JS & TypeScript Knowledge", "5.00", hr),
                buildCriterion(frontendTemplate, "CSS/UX Understanding", "3.00", hr),
                buildCriterion(frontendTemplate, "Communication Skills", "2.00", hr));
        scorecardCriterionRepository.saveAll(frontendCriteria);

        ScorecardTemplate backendTemplate = saveTemplate("Backend Developer Evaluation", itDept, hr);
        scorecardCriterionRepository.saveAll(List.of(
                buildCriterion(backendTemplate, "Java/Spring Boot Expertise", "5.00", hr),
                buildCriterion(backendTemplate, "Database Design (SQL/NoSQL)", "4.00", hr),
                buildCriterion(backendTemplate, "API Design & RESTful", "3.00", hr)));

        ScorecardTemplate marketingTemplate = saveTemplate("Marketing Manager Evaluation", marketingDept, hr);
        scorecardCriterionRepository.saveAll(List.of(
                buildCriterion(marketingTemplate, "Digital Marketing Strategy", "5.00", hr),
                buildCriterion(marketingTemplate, "Data Analytics", "4.00", hr)));

        return new SeededFoundation(itDept, marketingDept, frontendCriteria);
    }

    private ScorecardTemplate saveTemplate(String name, Department department, User creator) {
        ScorecardTemplate template = new ScorecardTemplate();
        template.setName(name);
        template.setDepartment(department);
        template.setCreatedBy(creator.getId());
        return scorecardTemplateRepository.save(template);
    }

    private ScorecardCriterion buildCriterion(
            ScorecardTemplate template, String name, String weight, User creator) {
        ScorecardCriterion criterion = new ScorecardCriterion();
        criterion.setTemplate(template);
        criterion.setName(name);
        criterion.setWeight(new BigDecimal(weight));
        criterion.setCreatedBy(creator.getId());
        return criterion;
    }
}
