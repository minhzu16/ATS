package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.CreateScorecardTemplateRequest;
import fptu.sba301.ats.dto.response.ScorecardTemplateResponse;

import java.util.List;
import java.util.UUID;

public interface ScorecardTemplateService {
    ScorecardTemplateResponse create(CreateScorecardTemplateRequest request);
    ScorecardTemplateResponse update(UUID id, CreateScorecardTemplateRequest request);
    void delete(UUID id);
    ScorecardTemplateResponse getById(UUID id);
    List<ScorecardTemplateResponse> getAll();
    List<ScorecardTemplateResponse> getByDepartment(UUID departmentId);
    List<ScorecardTemplateResponse> getAllTemplates();
}
