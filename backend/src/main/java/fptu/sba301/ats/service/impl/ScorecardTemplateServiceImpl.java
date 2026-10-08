package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.CreateScorecardTemplateRequest;
import fptu.sba301.ats.dto.response.ScorecardCriterionResponse;
import fptu.sba301.ats.dto.response.ScorecardTemplateResponse;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.ScorecardCriterion;
import fptu.sba301.ats.entity.ScorecardTemplate;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.DepartmentRepository;
import fptu.sba301.ats.repository.ScorecardCriterionRepository;
import fptu.sba301.ats.repository.ScorecardTemplateRepository;
import fptu.sba301.ats.service.ScorecardTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScorecardTemplateServiceImpl implements ScorecardTemplateService {

    private final ScorecardTemplateRepository templateRepository;
    private final ScorecardCriterionRepository criterionRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public ScorecardTemplateResponse create(CreateScorecardTemplateRequest request) {
        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Department not found", HttpStatus.NOT_FOUND));
        }

        ScorecardTemplate template = ScorecardTemplate.builder()
                .name(request.getName())
                .department(department)
                .build();
        template = templateRepository.save(template);

        ScorecardTemplate savedTemplate = template;
        List<ScorecardCriterion> criteria = request.getCriteria().stream()
                .map(c -> ScorecardCriterion.builder()
                        .template(savedTemplate)
                        .name(c.getName())
                        .weight(c.getWeight() != null ? c.getWeight() : new BigDecimal("1.00"))
                        .build())
                .collect(Collectors.toList());
        criterionRepository.saveAll(criteria);

        return toResponse(savedTemplate, criteria);
    }

    @Override
    @Transactional
    public ScorecardTemplateResponse update(UUID id, CreateScorecardTemplateRequest request) {
        ScorecardTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template not found", HttpStatus.NOT_FOUND));

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BusinessException("Department not found", HttpStatus.NOT_FOUND));
            template.setDepartment(department);
        } else {
            template.setDepartment(null);
        }

        template.setName(request.getName());
        templateRepository.save(template);

        // Replace criteria
        criterionRepository.deleteByTemplateId(id);
        List<ScorecardCriterion> criteria = request.getCriteria().stream()
                .map(c -> ScorecardCriterion.builder()
                        .template(template)
                        .name(c.getName())
                        .weight(c.getWeight() != null ? c.getWeight() : new BigDecimal("1.00"))
                        .build())
                .collect(Collectors.toList());
        criterionRepository.saveAll(criteria);

        return toResponse(template, criteria);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ScorecardTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(template -> {
                    List<ScorecardCriterionResponse> criterionResponses =
                            criterionRepository.findByTemplateId(template.getId())
                                    .stream()
                                    .map(c -> ScorecardCriterionResponse.builder()
                                            .id(c.getId())
                                            .name(c.getName())
                                            .weight(c.getWeight())
                                            .build())
                                    .toList();

                    return ScorecardTemplateResponse.builder()
                            .id(template.getId())
                            .name(template.getName())
                            .departmentId(template.getDepartment() != null ? template.getDepartment().getId() : null)
                            .departmentName(template.getDepartment() != null ? template.getDepartment().getName() : null)
                            .criteria(criterionResponses)
                            .build();
                })
                .toList();
    }
    @Override
    @Transactional
    public void delete(UUID id) {
        if (!templateRepository.existsById(id)) {
            throw new BusinessException("Template not found", HttpStatus.NOT_FOUND);
        }
        criterionRepository.deleteByTemplateId(id);
        templateRepository.deleteById(id);
    }

    @Override
    public ScorecardTemplateResponse getById(UUID id) {
        ScorecardTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Template not found", HttpStatus.NOT_FOUND));
        List<ScorecardCriterion> criteria = criterionRepository.findByTemplateId(id);
        return toResponse(template, criteria);
    }

    @Override
    public List<ScorecardTemplateResponse> getAll() {
        return templateRepository.findAll().stream()
                .map(t -> toResponse(t, criterionRepository.findByTemplateId(t.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ScorecardTemplateResponse> getByDepartment(UUID departmentId) {
        return templateRepository.findByDepartmentId(departmentId).stream()
                .map(t -> toResponse(t, criterionRepository.findByTemplateId(t.getId())))
                .collect(Collectors.toList());
    }

    private ScorecardTemplateResponse toResponse(ScorecardTemplate template, List<ScorecardCriterion> criteria) {
        return ScorecardTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .departmentId(template.getDepartment() != null ? template.getDepartment().getId() : null)
                .departmentName(template.getDepartment() != null ? template.getDepartment().getName() : null)
                .criteria(criteria.stream()
                        .map(c -> ScorecardCriterionResponse.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .weight(c.getWeight())
                                .build())
                        .toList())
                .build();
    }
}
