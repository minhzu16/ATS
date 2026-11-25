package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.CreateScorecardTemplateRequest;
import fptu.sba301.ats.dto.response.ScorecardTemplateResponse;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.entity.ScorecardTemplate;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.DepartmentRepository;
import fptu.sba301.ats.repository.ScorecardCriterionRepository;
import fptu.sba301.ats.repository.ScorecardTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScorecardTemplateServiceImplTest {

    @Mock
    private ScorecardTemplateRepository templateRepository;
    @Mock
    private ScorecardCriterionRepository criterionRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ScorecardTemplateServiceImpl scorecardTemplateService;

    @Test
    void createTemplateWithDepartmentAndCriteria() {
        UUID deptId = UUID.randomUUID();
        Department dept = Department.builder().id(deptId).name("IT").build();
        CreateScorecardTemplateRequest req = CreateScorecardTemplateRequest.builder()
                .name("Java Backend")
                .departmentId(deptId)
                .criteria(List.of(
                        CreateScorecardTemplateRequest.CriterionRequest.builder()
                                .name("Spring Boot")
                                .weight(new BigDecimal("5.00"))
                                .build()))
                .build();

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));
        when(templateRepository.save(any(ScorecardTemplate.class))).thenAnswer(inv -> {
            ScorecardTemplate t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        ScorecardTemplateResponse res = scorecardTemplateService.create(req);
        assertEquals("Java Backend", res.getName());
        assertEquals("IT", res.getDepartmentName());
        assertEquals(1, res.getCriteria().size());
    }

    @Test
    void updateTemplateReplacesCriteria() {
        UUID id = UUID.randomUUID();
        ScorecardTemplate existing = ScorecardTemplate.builder().id(id).name("Old").build();
        CreateScorecardTemplateRequest req = CreateScorecardTemplateRequest.builder()
                .name("Updated")
                .criteria(List.of(
                        CreateScorecardTemplateRequest.CriterionRequest.builder().name("System Design").build()))
                .build();

        when(templateRepository.findById(id)).thenReturn(Optional.of(existing));

        ScorecardTemplateResponse res = scorecardTemplateService.update(id, req);
        assertEquals("Updated", res.getName());
        verify(criterionRepository).deleteByTemplateId(id);
        verify(criterionRepository).saveAll(any());
    }

    @Test
    void getAllTemplatesIncludesCriteria() {
        UUID templateId = UUID.randomUUID();
        ScorecardTemplate template = ScorecardTemplate.builder().id(templateId).name("General").build();
        ScorecardCriterion criterion = ScorecardCriterion.builder()
                .id(UUID.randomUUID())
                .name("Communication")
                .weight(new BigDecimal("2.00"))
                .build();

        when(templateRepository.findAll()).thenReturn(List.of(template));
        when(criterionRepository.findByTemplateId(templateId)).thenReturn(List.of(criterion));

        List<ScorecardTemplateResponse> all = scorecardTemplateService.getAllTemplates();
        assertEquals(1, all.size());
        assertEquals("Communication", all.get(0).getCriteria().get(0).getName());
    }

    @Test
    void deleteThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(templateRepository.existsById(id)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> scorecardTemplateService.delete(id));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }
}
