package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.CreateOnboardingRequest;
import fptu.sba301.ats.dto.response.OnboardingChecklistResponse;

import java.util.List;
import java.util.UUID;

public interface OnboardingService {
    OnboardingChecklistResponse create(CreateOnboardingRequest request);
    List<OnboardingChecklistResponse> getAll();
    OnboardingChecklistResponse getById(UUID id);
    OnboardingChecklistResponse getByApplicationId(UUID applicationId);
    OnboardingChecklistResponse update(UUID id, CreateOnboardingRequest request);
    OnboardingChecklistResponse toggleTask(UUID checklistId, UUID taskId);
}
